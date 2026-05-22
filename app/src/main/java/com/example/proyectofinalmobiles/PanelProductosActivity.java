package com.example.proyectofinalmobiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class PanelProductosActivity extends AppCompatActivity {

    private RecyclerView rvProductosVendedor;
    private ProductoAdapter adaptador;
    private List<Producto> listaProductos;
    private FloatingActionButton btnAgregar;
    private ImageButton btnRegresar;
    private String tokenJwt = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_productos);

        SharedPreferences preferences = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        tokenJwt = preferences.getString("TOKEN_JWT", "");

        if (tokenJwt.isEmpty()) {
            tokenJwt = preferences.getString("token", "");
        }
        if (tokenJwt.isEmpty()) {
            tokenJwt = preferences.getString("tokenJwt", "");
        }

        Log.d("API_AUTH_TEST", "TOKEN RECUPERADO: [" + tokenJwt + "]");

        inicializarVistas();

        listaProductos = new ArrayList<>();
        rvProductosVendedor.setLayoutManager(new LinearLayoutManager(this));

        adaptador = new ProductoAdapter(listaProductos, new ProductoAdapter.OnProductoClickListener() {
            @Override
            public void onProductoClick(Producto producto) {
            }

            @Override
            public void onEditarClick(Producto producto) {
                Intent intent = new Intent(PanelProductosActivity.this, AgregarProductoActivity.class);
                intent.putExtra("EDICION_MODO", true);
                intent.putExtra("PRODUCTO_ID", producto.getId());
                intent.putExtra("PRODUCTO_NOMBRE", producto.getNombre());
                intent.putExtra("PRODUCTO_DESCRIPCION", producto.getDescripcion());
                intent.putExtra("PRODUCTO_PRECIO", producto.getPrecio());
                intent.putExtra("PRODUCTO_CANTIDAD", producto.getCantidad());
                intent.putExtra("PRODUCTO_CATEGORIA", producto.getCategoria());
                intent.putExtra("PRODUCTO_IMAGEN_URL", producto.getImagenUrl());
                startActivity(intent);
            }

            @Override
            public void onDisponibilidadClick(Producto producto, boolean nuevoEstado) {
                cambiarEstadoDisponibilidadAPI(producto, nuevoEstado);
            }
        });

        rvProductosVendedor.setAdapter(adaptador);

        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(PanelProductosActivity.this, AgregarProductoActivity.class);
            intent.putExtra("EDICION_MODO", false);
            startActivity(intent);
        });

        btnRegresar.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProductosDesdeAPI();
    }

    private void inicializarVistas() {
        rvProductosVendedor = findViewById(R.id.rvProductosVendedor);
        btnAgregar = findViewById(R.id.btnAgregarProductoFlotante);
        btnRegresar = findViewById(R.id.btnVolverPanelVendedor);
    }

    private void cargarProductosDesdeAPI() {
        String url = "http://10.0.2.2:8080/api/productos/mis-productos";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(com.android.volley.Request.Method.GET, url, null,
                response -> {
                    listaProductos.clear();
                    try {
                        Log.d("API_RESPONSE", "JSON recibido: " + response.toString());

                        if (response.length() == 0) {
                            Toast.makeText(this, "No tienes productos registrados aún", Toast.LENGTH_SHORT).show();
                        }

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Producto prod = new Producto();

                            prod.setId(obj.getLong("id"));
                            prod.setNombre(obj.getString("nombre"));
                            prod.setDescripcion(obj.getString("descripcion"));
                            prod.setPrecio(obj.getDouble("precio"));
                            prod.setCategoria(obj.optString("categoria", ""));
                            prod.setCantidad(obj.optInt("cantidad", 0));
                            prod.setDisponible(obj.optBoolean("disponible", true));

                            if (obj.has("imagen_url")) {
                                prod.setImagenUrl(obj.optString("imagen_url", ""));
                            } else {
                                prod.setImagenUrl(obj.optString("imagenUrl", ""));
                            }

                            listaProductos.add(prod);
                        }
                        adaptador.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("API_ERROR", "Error de parseo de campos: " + e.getMessage());
                        Toast.makeText(this, "Error al procesar los datos del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Error en petición GET: " + error.toString());
                    Toast.makeText(this, "Error de red al traer los productos", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + tokenJwt);
                return headers;
            }
        };

        queue.add(request);
    }

    private void cambiarEstadoDisponibilidadAPI(Producto producto, boolean nuevoEstado) {
        if (!nuevoEstado) {
            // =========================================================================
            // DESHABILITAR: Usamos el PATCH original que funciona perfectamente
            // =========================================================================
            String url = "http://10.0.2.2:8080/api/productos/eliminar/" + producto.getId();
            RequestQueue queue = Volley.newRequestQueue(this);

            StringRequest request = new StringRequest(com.android.volley.Request.Method.PATCH, url,
                    response -> {
                        producto.setDisponible(false);
                        adaptador.notifyDataSetChanged();
                        Toast.makeText(this, "Producto deshabilitado correctamente", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        Log.e("API_ERROR", "Error en PATCH: " + error.toString());
                        Toast.makeText(this, "No se pudo deshabilitar el producto", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + tokenJwt);
                    return headers;
                }
            };
            queue.add(request);

        } else {
            // =========================================================================
            // ACTIVAR: Usamos OkHttp replicando el formulario de edición idéntico
            // =========================================================================
            String urlPut = "http://10.0.2.2:8080/api/productos/actualizar/" + producto.getId();

            JSONObject jsonProducto = new JSONObject();
            try {
                jsonProducto.put("id", producto.getId());
                jsonProducto.put("nombre", producto.getNombre());
                jsonProducto.put("descripcion", producto.getDescripcion());
                jsonProducto.put("precio", producto.getPrecio());
                jsonProducto.put("cantidad", producto.getCantidad());

                String cat = producto.getCategoria();
                jsonProducto.put("categoria", (cat != null && !cat.isEmpty()) ? cat.toUpperCase() : "ANTOJITOS");
                jsonProducto.put("imagenUrl", producto.getImagenUrl());
                jsonProducto.put("disponible", true);

            } catch (JSONException e) {
                Log.e("API_OKHTTP_ACTIVATE", "Error armando JSON: " + e.getMessage());
            }

            OkHttpClient client = new OkHttpClient();

            RequestBody productoPartBody = RequestBody.create(
                    jsonProducto.toString(),
                    MediaType.parse("text/plain; charset=utf-8")
            );

            RequestBody archivoVacioPartBody = RequestBody.create(new byte[0], MediaType.parse("image/jpeg"));

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("producto", null, productoPartBody)
                    .addFormDataPart("archivo", "", archivoVacioPartBody)
                    .build();

            Request request = new Request.Builder()
                    .url(urlPut)
                    .header("Authorization", "Bearer " + tokenJwt)
                    .put(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("API_OKHTTP_ACTIVATE", "Fallo de red: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(PanelProductosActivity.this,
                            "Error de red", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            producto.setDisponible(true);
                            adaptador.notifyDataSetChanged();
                            Toast.makeText(PanelProductosActivity.this,
                                    "¡Cambio enviado con éxito!", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        int codigo = response.code();
                        Log.e("API_OKHTTP_ACTIVATE", "Código Servidor: " + codigo);
                        runOnUiThread(() -> Toast.makeText(PanelProductosActivity.this,
                                "Error del servidor (" + codigo + ")", Toast.LENGTH_LONG).show());
                    }
                }
            });
        }
    }
}