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

        // Recibir el nombre del usuario para personalizar el mensaje
        String nombre = getIntent().getStringExtra("USUARIO_NOMBRE");
        TextView tvBienvenido = findViewById(R.id.tvBienvenido);
        if (nombre != null) {
            tvBienvenido.setText("¡BIENVENIDO " + nombre.toUpperCase() + "!");
        }

        // Timer de 3 segundos para pasar a la siguiente pantalla
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(ActividadRegistroExitoso.this, ActividadIniciarSesion.class);
            // Limpiar el stack de actividades para que no pueda regresar al registro
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 2000); // 2 segundos
    }

}
