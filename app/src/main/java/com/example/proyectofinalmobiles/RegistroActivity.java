package com.example.proyectofinalmobiles;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistroActivity extends AppCompatActivity {

    private ImageButton btnRegresarLogin;
    private SwitchMaterial switchTipoUsuario;
    private TextInputEditText etNombre, etCorreoRegistro, etPasswordRegistro, etDireccion, etNegocio;
    private MaterialButton btnSeleccionarUbicacion, btnRegistrarse;
    private LinearLayout layoutVendedor;

    // Coordenadas fijas temporales para simular el mapa
    private double latitudSeleccionada = 0.0;
    private double longitudSeleccionada = 0.0;
    private boolean ubicacionFijada = false;

    // URL de tu API local (Recuerda que 10.0.2.2 apunta al localhost de tu PC desde el emulador)
    private static final String URL_BASE_REGISTRO = "http://10.0.2.2:8080/api/usuarios/registro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        inicializarVistas();

        btnRegresarLogin.setOnClickListener(v -> finish());

        switchTipoUsuario.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutVendedor.setVisibility(View.VISIBLE);
            } else {
                layoutVendedor.setVisibility(View.GONE);
                etNegocio.setText("");
            }
        });

        btnSeleccionarUbicacion.setOnClickListener(v -> {
            // Simulación de mapa (En el siguiente paso integraremos Google Maps)
            latitudSeleccionada = 13.6744;
            longitudSeleccionada = -89.2797;
            ubicacionFijada = true;

            btnSeleccionarUbicacion.setText("📍 Ubicación Fijada con Éxito");
            Toast.makeText(this, "Coordenadas capturadas correctamente", Toast.LENGTH_SHORT).show();
        });

        btnRegistrarse.setOnClickListener(v -> validarYProcesar());
    }

    private void inicializarVistas() {
        btnRegresarLogin = findViewById(R.id.btnRegresarLogin);
        switchTipoUsuario = findViewById(R.id.switchTipoUsuario);
        etNombre = findViewById(R.id.etNombre);
        etCorreoRegistro = findViewById(R.id.etCorreoRegistro);
        etPasswordRegistro = findViewById(R.id.etPasswordRegistro);
        etDireccion = findViewById(R.id.etDireccion);
        etNegocio = findViewById(R.id.etNegocio);
        btnSeleccionarUbicacion = findViewById(R.id.btnSeleccionarUbicacion);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        layoutVendedor = findViewById(R.id.layoutVendedor);
    }

    private void validarYProcesar() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreoRegistro.getText().toString().trim();
        String password = etPasswordRegistro.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String rol = switchTipoUsuario.isChecked() ? "VENDEDOR" : "VECINO";
        String negocio = etNegocio.getText().toString().trim();

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (switchTipoUsuario.isChecked() && negocio.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa el nombre de tu negocio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ubicacionFijada) {
            Toast.makeText(this, "Debes fijar tu ubicación en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construir el JSON para el @RequestBody que espera tu Spring Boot
        JSONObject jsonUsuario = new JSONObject();
        try {
            jsonUsuario.put("nombre", nombre);
            jsonUsuario.put("email", correo); // Asegúrate de que coincida con el atributo de tu modelo (email o correo)
            jsonUsuario.put("password", password);
            jsonUsuario.put("direccionTexto", direccion);
            jsonUsuario.put("rol", rol);

            if (rol.equals("VENDEDOR")) {
                jsonUsuario.put("nombreNegocio", negocio); // Ajustar según el nombre del campo en tu modelo Usuario
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Consumir el Backend mediante Volley
        enviarDatosAlBackend(jsonUsuario, nombre, correo, rol, direccion);
    }

    private void enviarDatosAlBackend(JSONObject jsonBody, String nombre, String correo, String rol, String direccion) {
        // Concatenamos los @RequestParam a la URL como lo pide tu Controlador de Spring
        String urlCompleta = URL_BASE_REGISTRO + "?latitud=" + latitudSeleccionada + "&longitud=" + longitudSeleccionada;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                urlCompleta,
                jsonBody,
                response -> {
                    // SI TODO SALE BIEN: Disparamos las alertas solicitadas
                    reproducirSonidoExito();
                    mostrarAlertaExito(nombre, correo, rol, direccion);
                },
                error -> {
                    // SI ALGO FALLA (Error de red, base de datos abajo, etc.)
                    Toast.makeText(this, "Error al registrar en el servidor: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        queue.add(request);
    }

    private void reproducirSonidoExito() {
        try {
            Uri notificacion = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notificacion);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlertaExito(String nombre, String correo, String rol, String direccion) {
        StringBuilder resumen = new StringBuilder();
        resumen.append("👤 Usuario: ").append(nombre).append("\n");
        resumen.append("📧 Correo: ").append(correo).append("\n");
        resumen.append("🛡️ Rol: ").append(rol).append("\n");
        resumen.append("🏠 Dirección: ").append(direccion).append("\n");
        resumen.append("📍 Lat: ").append(latitudSeleccionada).append(" | Lon: ").append(longitudSeleccionada);

        if (rol.equals("VENDEDOR")) {
            resumen.append("\n🏪 Negocio: ").append(etNegocio.getText().toString().trim());
        }

        new AlertDialog.Builder(this)
                .setTitle("¡Registro Exitoso!")
                .setMessage("El usuario se ha registrado correctamente en el sistema.\n\n" + resumen.toString())
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    // Retorna al login al confirmar de forma limpia
                    finish();
                })
                .show();
    }
}