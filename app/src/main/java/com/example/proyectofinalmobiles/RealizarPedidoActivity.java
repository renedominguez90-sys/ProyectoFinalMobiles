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
import com.android.volley.toolbox.Volley;
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
public class RealizarPedidoActivity extends AppCompatActivity {

    private RecyclerView recyclerProductos;
    private ProductoAdapter adapter;
    private List<Producto> listaProductos;
    private ImageButton btnFinalizarPedido;
    private String tokenJwt = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realizar_pedido);

        SharedPreferences preferences = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);
        tokenJwt = preferences.getString("TOKEN_JWT", "");

        recyclerProductos = findViewById(R.id.recyclerProductos);
        btnFinalizarPedido = findViewById(R.id.btnFinalizarPedido);

        listaProductos = new ArrayList<>();
        recyclerProductos.setLayoutManager(new LinearLayoutManager(this));

        // Ajustado para cumplir con la firma del nuevo ProductoAdapter de forma segura
        adapter = new ProductoAdapter(listaProductos, new ProductoAdapter.OnProductoClickListener() {
            @Override
            public void onProductoClick(Producto producto) {
                // Lógica de añadir al carrito que desees implementar
                Toast.makeText(RealizarPedidoActivity.this, "Seleccionado: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditarClick(Producto producto) {
                // Vacío: El cliente no edita productos
            }

            @Override
            public void onDisponibilidadClick(Producto producto, boolean nuevoEstado) {
                // Vacío: El cliente no cambia estados de almacén
            }
        });

        recyclerProductos.setAdapter(adapter);

        btnFinalizarPedido.setOnClickListener(v -> {
            Intent intent = new Intent(RealizarPedidoActivity.this, FinalizarPedidoActivity.class);
            startActivity(intent);
        });

        cargarProductos();
    }

    private void cargarProductos() {
        String url = "http://10.0.2.2:8080/api/productos";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    listaProductos.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Producto prod = new Producto();
                            prod.setId(obj.getLong("id"));
                            prod.setNombre(obj.getString("nombre"));
                            prod.setDescripcion(obj.getString("descripcion"));
                            prod.setPrecio(obj.getDouble("precio"));
                            prod.setImagenUrl(obj.optString("imagenUrl", ""));
                            prod.setCategoria(obj.optString("categoria", ""));
                            prod.setCantidad(obj.optInt("cantidad", 0));
                            prod.setDisponible(obj.optBoolean("disponible", true));

                            // Al cliente solo le mostramos los que sí están disponibles
                            if (prod.isDisponible()) {
                                listaProductos.add(prod);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("API_ERROR", "Error parsing: " + e.getMessage());
                    }
                },
                error -> Toast.makeText(this, "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show()
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