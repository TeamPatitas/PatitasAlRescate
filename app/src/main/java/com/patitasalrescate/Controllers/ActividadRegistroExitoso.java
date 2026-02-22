package com.patitasalrescate.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.patitasalrescate.R;

public class ActividadRegistroExitoso extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_patita_verificada);

        String nombre = getIntent().getStringExtra("USUARIO_NOMBRE");
        TextView tvBienvenido = findViewById(R.id.tvBienvenido);
        if (nombre != null) {
            tvBienvenido.setText("¡BIENVENIDO " + nombre.toUpperCase() + "!");
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(ActividadRegistroExitoso.this, ActividadIniciarSesion.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }
}