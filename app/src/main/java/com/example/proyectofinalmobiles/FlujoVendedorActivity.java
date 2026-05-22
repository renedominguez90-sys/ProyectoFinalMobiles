package com.example.proyectofinalmobiles;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

public class FlujoVendedorActivity extends AppCompatActivity {

    private TextView tvBienvenidaVendedor, tvNombreNegocioHeader;
    private MaterialCardView cardUbicacion, cardProductos, cardPerfilNegocio, cardPedidos;
    private MaterialButton btnCerrarSesionVendedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flujo_vendedor);

        inicializarVistas();
        recuperarDatosIntent();

        // ==========================================
        // FUNCIONES DE NAVEGACIÓN (CLICS DEL MENÚ)
        // ==========================================

        // 1. Botón: Mi Ubicación (Revisar y cambiar PIN en Google Maps)
        cardUbicacion.setOnClickListener(v -> {
            Toast.makeText(this, "Cargando mapa de tu negocio...", Toast.LENGTH_SHORT).show();
            // TODO: Cuando crees el Activity del mapa, descomenta las líneas de abajo:
            // Intent intent = new Intent(FlujoVendedorActivity.this, MapaVendedorActivity.class);
            // startActivity(intent);
        });

        // 2. Botón: Mis Productos (Catálogo CRUD para agregar/cambiar precios)
        cardProductos.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo catálogo de productos...", Toast.LENGTH_SHORT).show();
            // Abre tu layout 'activity_panel_productos.xml' a través de su clase Java
            Intent intent = new Intent(FlujoVendedorActivity.this, PanelProductosActivity.class);
            startActivity(intent);
        });

        // 3. Botón: Perfil / Descripción del Negocio
        cardPerfilNegocio.setOnClickListener(v -> {
            Toast.makeText(this, "Cargando métricas y descripción...", Toast.LENGTH_SHORT).show();
            // Abre tu layout 'activity_metricas_vendedor.xml' a través de su clase Java
            Intent intent = new Intent(FlujoVendedorActivity.this, MetricasVendedorActivity.class);
            startActivity(intent);
        });

        // 4. Botón: Pedidos Recibidos
        cardPedidos.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo historial de pedidos...", Toast.LENGTH_SHORT).show();
            // Abre tu layout 'activity_historial_pedidos.xml' o 'activity_realizar_pedido.xml'
            Intent intent = new Intent(FlujoVendedorActivity.this, RealizarPedidoActivity.class);
            startActivity(intent);
        });

        // 5. Botón: Cerrar Sesión
        btnCerrarSesionVendedor.setOnClickListener(v -> {
            Intent intent = new Intent(FlujoVendedorActivity.this, LoginActivity.class);
            // FLAG_ACTIVITY_CLEAR_TASK limpia el historial para que no pueda volver atrás al presionar el botón físico
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void inicializarVistas() {
        tvBienvenidaVendedor = findViewById(R.id.tvBienvenidaVendedor);
        tvNombreNegocioHeader = findViewById(R.id.tvNombreNegocioHeader);
        cardUbicacion = findViewById(R.id.cardUbicacion);
        cardProductos = findViewById(R.id.cardProductos);
        cardPerfilNegocio = findViewById(R.id.cardPerfilNegocio);
        cardPedidos = findViewById(R.id.cardPedidos);
        btnCerrarSesionVendedor = findViewById(R.id.btnCerrarSesionVendedor);
    }

    private void recuperarDatosIntent() {
        String nombreUsuario = getIntent().getStringExtra("USUARIO_NOMBRE");
        String nombreNegocio = getIntent().getStringExtra("NEGOCIO_NOMBRE");

        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            tvBienvenidaVendedor.setText("¡Hola, " + nombreUsuario + "!");
        }
        if (nombreNegocio != null && !nombreNegocio.isEmpty()) {
            tvNombreNegocioHeader.setText(nombreNegocio);
        }
    }
}