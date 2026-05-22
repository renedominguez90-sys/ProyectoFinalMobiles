package com.example.proyectofinalmobiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etCorreo, etPassword;
    private TextInputLayout layoutCorreo, layoutPassword;
    private Button btnLogin, btnRegistro;

    // URL local para el endpoint de Login en tu Spring Boot
    private static final String URL_LOGIN = "http://10.0.2.2:8080/api/usuarios/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarVistas();

        // Evento para enviar credenciales a la API
        btnLogin.setOnClickListener(v -> validarYAutenticar());

        // Evento para ir a la pantalla de registro
        btnRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistroActivity.class);
            startActivity(intent);
        });
    }

    private void inicializarVistas() {
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        layoutCorreo = findViewById(R.id.layoutCorreo);
        layoutPassword = findViewById(R.id.layoutPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistro = findViewById(R.id.btnRegistro);
    }

    private void validarYAutenticar() {
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        layoutCorreo.setError(null);
        layoutPassword.setError(null);

        if (correo.isEmpty()) {
            layoutCorreo.setError("Ingrese un correo");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            layoutCorreo.setError("Correo inválido");
            return;
        }

        if (password.isEmpty()) {
            layoutPassword.setError("Ingrese una contraseña");
            return;
        }

        // Si la validación local pasa, consumimos la API
        realizarLoginAPI(correo, password);
    }

    private void realizarLoginAPI(String email, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Creamos el JSON coincidiendo exactamente con tu LoginRequest de Spring Boot
        JSONObject jsonLoginRequest = new JSONObject();
        try {
            jsonLoginRequest.put("email", email);
            jsonLoginRequest.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL_LOGIN,
                jsonLoginRequest,
                response -> {
                    try {
                        // Procesamos la respuesta mapeada a tu LoginResponse de Spring Boot
                        String token = response.getString("token");
                        String emailResp = response.getString("email");
                        String rol = response.getString("rol");
                        String nombre = response.getString("nombre");

                        // ====================================================================
                        // CAMBIO CLAVE: Guardamos el Token JWT en SharedPreferences (Disco)
                        // ====================================================================
                        SharedPreferences preferences = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("TOKEN_JWT", token);
                        editor.apply();
                        // ====================================================================

                        Toast.makeText(LoginActivity.this, "¡Bienvenido " + nombre + "!", Toast.LENGTH_SHORT).show();

                        // Guardamos o validamos el rol para la redirección
                        if (rol.equalsIgnoreCase("VENDEDOR")) {
                            Intent intent = new Intent(LoginActivity.this, FlujoVendedorActivity.class);

                            // Pasamos los datos que el Dashboard del vendedor necesita pintar en su Header
                            intent.putExtra("USUARIO_NOMBRE", nombre);

                            // Si tu LoginResponse incluye el nombre del negocio, extráelo aquí.
                            // Si no viene en el LoginResponse, pasamos un genérico o el nombre del usuario por ahora.
                            String negocio = response.has("nombreNegocio") ? response.getString("nombreNegocio") : "Mi Tienda Local";
                            intent.putExtra("NEGOCIO_NOMBRE", negocio);

                            startActivity(intent);
                            finish(); // Destruye el Login para que no pueda volver atrás al presionar "regresar"
                        } else {
                            // TODO: Flujo para rol VECINO / COMPRADOR en el futuro
                            Toast.makeText(LoginActivity.this, "Acceso Vecino (Próximamente)", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Error al procesar datos del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Manejo de errores (Credenciales incorrectas o servidor caído)
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        Toast.makeText(LoginActivity.this, "Credenciales Incorrectas", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error de conexión con el servidor", Toast.LENGTH_LONG).show();
                    }
                }
        );

        queue.add(jsonObjectRequest);
    }
}