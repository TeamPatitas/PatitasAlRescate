package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.patitasalrescate.R;
import com.patitasalrescate.controllers.base.BaseActivity;

public class UserPublication extends BaseActivity {

    private static final String TELEFONO_SOPORTE = "51943292519";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_user_publication);

        Toolbar toolbar = findViewById(R.id.toolbarUserPublication);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        Button btnWhatsapp = findViewById(R.id.btnWhatsappDesarrolladores);
        btnWhatsapp.setOnClickListener(v -> abrirWhatsapp());
    }

    private void abrirWhatsapp() {
        String mensaje = "¡Hola! Quiero publicar una mascota en adopción en Patitas al Rescate 🐾";
        String url = "https://wa.me/" + TELEFONO_SOPORTE + "?text=" + Uri.encode(mensaje);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp no disponible en este dispositivo", Toast.LENGTH_SHORT).show();
        }
    }
}
