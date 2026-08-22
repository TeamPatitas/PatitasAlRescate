package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.model.Evento;

public class ActividadDetalleEvento extends AppCompatActivity {

    private ImageView imgDetalle;
    private TextView txtNombre, txtFecha, txtDescripcion;
    private ImageButton btnMapa;
    private Evento evento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_detalle_evento);

        imgDetalle = findViewById(R.id.img_detalle_evento);
        txtNombre = findViewById(R.id.txt_detalle_nombre_evento);
        txtFecha = findViewById(R.id.txt_detalle_fecha_evento);
        txtDescripcion = findViewById(R.id.txt_detalle_descripcion_evento);
        btnMapa = findViewById(R.id.btn_ver_mapa_evento);

        evento = (Evento) getIntent().getSerializableExtra("evento_key");

        if (evento != null) {
            txtNombre.setText(evento.getNombre());
            txtFecha.setText(evento.getFecha());
            txtDescripcion.setText(evento.getDescripcion());

            if (evento.getFotoUrl() != null && !evento.getFotoUrl().isEmpty()) {
                Glide.with(this)
                        .load(evento.getFotoUrl())
                        .centerCrop()
                        .into(imgDetalle);
            }

            btnMapa.setOnClickListener(v -> verEnMapa());
        } else {
            Toast.makeText(this, "Error al cargar el evento", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void verEnMapa() {
        if (evento.getLatitud() != 0 || evento.getLongitud() != 0) {
            String label = evento.getNombre();
            String uriBegin = "geo:" + evento.getLatitud() + "," + evento.getLongitud();
            String query = evento.getLatitud() + "," + evento.getLongitud() + "(" + label + ")";
            String encodedQuery = Uri.encode(query);
            String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
            Uri uri = Uri.parse(uriString);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(intent);
            } catch (Exception e) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ex) {
                    Toast.makeText(this, "No hay aplicación de mapas instalada", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Ubicación no disponible para este evento", Toast.LENGTH_SHORT).show();
        }
    }
}