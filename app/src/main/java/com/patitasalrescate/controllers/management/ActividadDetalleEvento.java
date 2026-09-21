package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.patitasalrescate.R;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.utils.PatitasSessionManager;

public class ActividadDetalleEvento extends AppCompatActivity {

    private ImageView imgDetalle;
    private TextView txtNombre, txtFecha, txtDescripcion;
    private Button btnMapa;
    private FloatingActionButton fabEditar;
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
        fabEditar = findViewById(R.id.fab_editar_evento);

        configToolbar();

        evento = (Evento) getIntent().getSerializableExtra("evento_key");

        if (evento != null) {
            cargarDatos();
            configurarSegunRol();
        } else {
            Toast.makeText(this, "Error al cargar el evento", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarDetalleEvento);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // Título se maneja en CollapsingToolbarLayout
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void cargarDatos() {
        txtNombre.setText(evento.getNombre());
        txtFecha.setText(evento.getFecha());
        txtDescripcion.setText(evento.getDescripcion());

        if (evento.getFotoUrl() != null && !evento.getFotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(evento.getFotoUrl())
                    .centerCrop()
                    .into(imgDetalle);
        } else {
            imgDetalle.setImageResource(R.drawable.eventos);
        }

        btnMapa.setOnClickListener(v -> verEnMapa());
    }

    private void configurarSegunRol() {
        // Por ahora lo dejamos visible para que puedas probar el diseño y la edición
        fabEditar.setVisibility(View.VISIBLE);
        fabEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActividadRegistrarEvento.class);
            intent.putExtra("evento_editar_key", evento);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos por si se editó
        if (evento != null) {
            com.patitasalrescate.data_access.DAOEvento dao = new com.patitasalrescate.data_access.DAOEvento(this);
            Evento actualizado = dao.obtenerPorId(evento.getIdEvento());
            if (actualizado != null) {
                evento = actualizado;
                cargarDatos();
            }
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