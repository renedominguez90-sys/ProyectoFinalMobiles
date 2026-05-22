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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // 1. Recuperación multi-llave segura del Token JWT
        SharedPreferences preferences = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        tokenJwt = preferences.getString("TOKEN_JWT", "");

        if (tokenJwt.isEmpty()) {
            tokenJwt = preferences.getString("token", "");
        }
        if (tokenJwt.isEmpty()) {
            tokenJwt = preferences.getString("tokenJwt", "");
        }

        // Monitoreo del flujo de autenticación en Logcat
        Log.d("API_AUTH_TEST", "TOKEN RECUPERADO: [" + tokenJwt + "]");

        inicializarVistas();

        listaProductos = new ArrayList<>();
        rvProductosVendedor.setLayoutManager(new LinearLayoutManager(this));

        // Configuración del adaptador con los métodos de la interfaz
        adaptador = new ProductoAdapter(listaProductos, new ProductoAdapter.OnProductoClickListener() {
            @Override
            public void onProductoClick(Producto producto) {
                // Clic opcional en la tarjeta
            }

            @Override
            public void onEditarClick(Producto producto) {
                Intent intent = new Intent(PanelProductosActivity.this, AgregarProductoActivity.class);
                intent.putExtra("EDICION_MODO", true);
                intent.putExtra("PRODUCTO_ID", producto.getId());
                intent.putExtra("PRODUCTO_NOMBRE", producto.getNombre());
//                intent.putExtra("PRODUCTO_DESCRIPCION", producto.getSecondary() != null ? producto.getSecondary() : producto.getDescripcion());
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
        // CORRECCIÓN: Apunta al endpoint específico del vendedor programado en tu controlador
        String url = "http://10.0.2.2:8080/api/productos/mis-productos";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
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
        // CORRECCIÓN: Apunta al mapping @PatchMapping("/eliminar/{id}") de tu backend
        String url = "http://10.0.2.2:8080/api/productos/eliminar/" + producto.getId();
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.PATCH, url,
                response -> {
                    // Modifica el estado visual en la lista tras la respuesta exitosa
                    producto.setDisponible(nuevoEstado);
                    adaptador.notifyDataSetChanged();
                    Toast.makeText(this, "Estado de disponibilidad actualizado", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("API_ERROR", "Error en petición PATCH: " + error.toString());
                    Toast.makeText(this, "No se pudo cambiar la disponibilidad", Toast.LENGTH_SHORT).show();
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
}