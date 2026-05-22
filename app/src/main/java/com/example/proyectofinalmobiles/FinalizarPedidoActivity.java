package com.example.proyectofinalmobiles;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FinalizarPedidoActivity extends AppCompatActivity {
    private Button btnConfirmarPedido, btnCancelarPedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalizar_pedido);

        try {

            btnConfirmarPedido = findViewById(R.id.btnConfirmarPedido);
            btnCancelarPedido = findViewById(R.id.btnCancelarPedido);

            btnConfirmarPedido.setOnClickListener(v -> {

                Toast.makeText(this,
                        "Pedido confirmado",
                        Toast.LENGTH_LONG).show();

                finish();
            });

            btnCancelarPedido.setOnClickListener(v -> {

                Toast.makeText(this,
                        "Pedido cancelado",
                        Toast.LENGTH_LONG).show();

                finish();
            });

        } catch (Exception e) {

            Toast.makeText(this,
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
