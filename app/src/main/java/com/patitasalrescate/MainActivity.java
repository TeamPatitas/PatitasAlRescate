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
import com.patitasalrescate.controllers.feed.ActividadFeedAdoptante;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.PatitasSessionManager;

public class MainActivity extends com.patitasalrescate.controllers.base.BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.patitasalrescate.utils.ApiApp.init(this);
        setContentView(R.layout.ly_splash_screen);

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

        new Handler().postDelayed(() -> {
            PatitasSessionManager session = PatitasSessionManager.getInstance(this);
            if (session.hasValidToken()) {
                String token = session.getAuthToken();
                ApiApp.client().session.setToken(token);

                // Al reabrir, el modo siempre vuelve a usuario (adoptante): evita que
                // fragmentos como "Mascotas" arranquen en modo refugio por sesión vieja.
                session.createSession(session.getUserId(), session.getUserName(),
                        "ADOPTANTE", session.getShelterId(), session.canManageShelter());

                // Al reabrir siempre se entra al feed del usuario (adoptante).
                Intent intent = new Intent(this, ActividadFeedAdoptante.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, ActividadIngresar.class);
                startActivity(intent);
            }
            finish();
        }, 2000);
    }
}