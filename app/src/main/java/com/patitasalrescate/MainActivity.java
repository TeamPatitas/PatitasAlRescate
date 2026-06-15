package com.patitasalrescate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.patitasalrescate.controllers.auth.ActividadIngresar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, ActividadIngresar.class);
                startActivity(intent);
                finish();
            }
        }, 3000);

        ImageView logo_bienvenida = findViewById(R.id.rj_logo_patitas);
        TextView texto_bienvenida = findViewById(R.id.rj_text_cargando);
        logo_bienvenida.setAlpha(0f);
        logo_bienvenida.setScaleX(0.3f);
        logo_bienvenida.setScaleY(0.3f);
        texto_bienvenida.setAlpha(0f);

        logo_bienvenida.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1500)
                .start();
        texto_bienvenida.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(500)
                .start();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_screen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}