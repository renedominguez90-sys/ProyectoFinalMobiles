package com.example.proyectofinalmobiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Developed by redleader
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences preferences = getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE);

            // --- LÍNEA DE PRUEBA: BORRAMOS EL TOKEN PARA FORZAR EL LOGIN ---
            preferences.edit().clear().apply();
            // ---------------------------------------------------------------

            String token = preferences.getString("TOKEN_JWT", "");

            if (token.isEmpty()) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, PanelProductosActivity.class));
            }
            finish();
        }, 2500);
    }
}