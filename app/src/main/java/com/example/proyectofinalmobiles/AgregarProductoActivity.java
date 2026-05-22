package com.example.proyectofinalmobiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Developed by redleader
 */
public class AgregarProductoActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etDescripcion, etPrecio, etCantidad;
    private AutoCompleteTextView autoCompleteCategoria;
    private ImageView ivPreview;
    private MaterialButton btnSeleccionarImg, btnGuardar;
    private ImageButton btnRegresar;

    private String tokenJwt = "";
    private Uri uriImagenSeleccionada = null;

    private boolean esModoEdicion = false;
    private Long productoIdEditar = 0L;
    private String imagenUrlActual = "";

    private final String URL_API_CREAR = "http://10.0.2.2:8080/api/productos/crear";

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uriImagen -> {
                if (uriImagen != null) {
                    try {
                        uriImagenSeleccionada = uriImagen;
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriImagen);
                        ivPreview.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar el archivo físico de imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);

        SharedPreferences preferences = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        tokenJwt = preferences.getString("TOKEN_JWT", "");

        inicializarComponentes();
        cargarCategoriasEnum();

        comprobarModoEdicion();

        btnSeleccionarImg.setOnClickListener(v -> abrirGaleria());
        btnRegresar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> procesarValidacion());
    }

    private void inicializarComponentes() {
        etNombre = findViewById(R.id.etNombreProducto);
        etDescripcion = findViewById(R.id.etDescripcionProducto);
        etPrecio = findViewById(R.id.etPrecioProducto);
        etCantidad = findViewById(R.id.etCantidadProducto);
        autoCompleteCategoria = findViewById(R.id.autoCompleteCategoria);
        ivPreview = findViewById(R.id.ivPreviewProducto);
        btnSeleccionarImg = findViewById(R.id.btnSeleccionarImagen);
        btnGuardar = findViewById(R.id.btnGuardarProducto);
        btnRegresar = findViewById(R.id.btnRegresarPanel);
    }

    private void cargarCategoriasEnum() {
        String[] javaCategoriasBackend = {
                "PANADERIA", "CARNES", "FRUTAS_VERDURAS", "ABARROTES",
                "BEBIDAS", "LIMPIEZA", "CUIDADO_PERSONAL", "ANTOJITOS",
                "POSTRES", "LACTEOS"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, javaCategoriasBackend);
        autoCompleteCategoria.setAdapter(adapter);
    }

    private void comprobarModoEdicion() {
        Intent intentOrigen = getIntent();
        if (intentOrigen != null && intentOrigen.getBooleanExtra("EDICION_MODO", false)) {
            esModoEdicion = true;

            btnGuardar.setText("Actualizar Producto");

            productoIdEditar = intentOrigen.getLongExtra("PRODUCTO_ID", 0L);
            imagenUrlActual = intentOrigen.getStringExtra("PRODUCTO_IMAGEN_URL");

            etNombre.setText(intentOrigen.getStringExtra("PRODUCTO_NOMBRE"));
            etDescripcion.setText(intentOrigen.getStringExtra("PRODUCTO_DESCRIPCION"));

            double precio = intentOrigen.getDoubleExtra("PRODUCTO_PRECIO", 0.0);
            etPrecio.setText(String.valueOf(precio)); // CORREGIDO: etPrecioProducto eliminado

            int cantidad = intentOrigen.getIntExtra("PRODUCTO_CANTIDAD", 0);
            etCantidad.setText(String.valueOf(cantidad));

            String cat = intentOrigen.getStringExtra("PRODUCTO_CATEGORIA");
            autoCompleteCategoria.setText(cat, false);

            if (imagenUrlActual != null && !imagenUrlActual.trim().isEmpty()) {
                String urlFotoServidor = "http://10.0.2.2:8080/uploads/" + imagenUrlActual.trim();
                Picasso.get()
                        .load(urlFotoServidor)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(ivPreview);
            }
        }
    }

    private void abrirGaleria() {
        galleryLauncher.launch("image/*");
    }

    private void procesarValidacion() {
        String nombre = etNombre.getText().toString().trim();
        String desc = etDescripcion.getText().toString().trim();
        String precio = etPrecio.getText().toString().trim();
        String cant = etCantidad.getText().toString().trim();
        String catSeleccionada = autoCompleteCategoria.getText().toString();

        if (nombre.isEmpty() || precio.isEmpty() || cant.isEmpty()) {
            Toast.makeText(this, "Campos requeridos vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uriImagenSeleccionada == null && !esModoEdicion) {
            Toast.makeText(this, "Por favor, selecciona una imagen para el producto", Toast.LENGTH_SHORT).show();
            return;
        }

        guardarRegistroAPI(nombre, desc, Double.parseDouble(precio), Integer.parseInt(cant), catSeleccionada);
    }

    private void guardarRegistroAPI(String nom, String desc, double prec, int cant, String cat) {
        JSONObject jsonProducto = new JSONObject();
        try {
            if (esModoEdicion) {
                jsonProducto.put("id", productoIdEditar);
                jsonProducto.put("imagenUrl", imagenUrlActual);
            }
            jsonProducto.put("nombre", nom);
            jsonProducto.put("descripcion", desc);
            jsonProducto.put("precio", prec);
            jsonProducto.put("cantidad", cant);
            jsonProducto.put("categoria", cat.toUpperCase());
            jsonProducto.put("disponible", true);
        } catch (JSONException e) {
            Log.e("JSON_ERROR", "Error armando los parámetros del producto: " + e.getMessage());
        }

        byte[] imagenBytes = null;
        if (uriImagenSeleccionada != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uriImagenSeleccionada);
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int len;
                while (inputStream != null && (len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                imagenBytes = byteBuffer.toByteArray();
            } catch (IOException e) {
                Log.e("IMAGE_ERROR", "Error transformando imagen a bytes: " + e.getMessage());
                Toast.makeText(this, "Error al procesar los datos de la imagen", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody productoPartBody = RequestBody.create(
                jsonProducto.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("producto", null, productoPartBody);

        if (imagenBytes != null) {
            RequestBody archivoPartBody = RequestBody.create(imagenBytes, MediaType.parse("image/jpeg"));
            builder.addFormDataPart("archivo", "producto_upload.jpg", archivoPartBody);
        }

        MultipartBody requestBody = builder.build();

        String urlDestino = esModoEdicion ? "http://10.0.2.2:8080/api/productos/" + productoIdEditar : URL_API_CREAR;
        String metodoHttp = esModoEdicion ? "PUT" : "POST";

        Request request = new Request.Builder()
                .url(urlDestino)
                .header("Authorization", "Bearer " + tokenJwt)
                .method(metodoHttp, requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Fallo total de red o conexión: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(AgregarProductoActivity.this,
                        "Error de red: No se pudo conectar al servidor", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        String msg = esModoEdicion ? "¡Producto actualizado con éxito!" : "¡Producto guardado con éxito!";
                        Toast.makeText(AgregarProductoActivity.this, msg, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    int codigoEstado = response.code();
                    String cuerpoError = response.body() != null ? response.body().string() : "Sin cuerpo de respuesta";

                    Log.e("API_ERROR", "Código de estado HTTP: " + codigoEstado);
                    Log.e("API_ERROR", "Respuesta detallada del backend: " + cuerpoError);

                    runOnUiThread(() -> Toast.makeText(AgregarProductoActivity.this,
                            "Error (" + codigoEstado + "): Estructura rechazada por el backend", Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}