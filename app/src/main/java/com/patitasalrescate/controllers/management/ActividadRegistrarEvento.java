package com.patitasalrescate.controllers.management;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.patitasalrescate.R;
import com.patitasalrescate.data_access.DAOEvento;
import com.patitasalrescate.model.Evento;

import java.util.UUID;

public class ActividadRegistrarEvento extends AppCompatActivity {

    private EditText edtNombre, edtFecha, edtDescripcion, edtFoto, edtLat, edtLong;
    private Button btnGuardar;
    private DAOEvento daoEvento;
    private Evento eventoExistente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_registrar_evento);

        daoEvento = new DAOEvento(this);

        initViews();
        configToolbar();

        eventoExistente = (Evento) getIntent().getSerializableExtra("evento_editar_key");
        if (eventoExistente != null) {
            prellenarDatos();
        }

        btnGuardar.setOnClickListener(v -> guardarEvento());
    }

    private void initViews() {
        edtNombre = findViewById(R.id.edt_nombre_evento);
        edtFecha = findViewById(R.id.edt_fecha_evento);
        edtDescripcion = findViewById(R.id.edt_descripcion_evento);
        edtFoto = findViewById(R.id.edt_foto_evento);
        edtLat = findViewById(R.id.edt_latitud_evento);
        edtLong = findViewById(R.id.edt_longitud_evento);
        btnGuardar = findViewById(R.id.btn_guardar_evento);
    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarRegistrarEvento);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(eventoExistente != null ? "Editar Evento" : "Registrar Evento");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void prellenarDatos() {
        edtNombre.setText(eventoExistente.getNombre());
        edtFecha.setText(eventoExistente.getFecha());
        edtDescripcion.setText(eventoExistente.getDescripcion());
        edtFoto.setText(eventoExistente.getFotoUrl());
        edtLat.setText(String.valueOf(eventoExistente.getLatitud()));
        edtLong.setText(String.valueOf(eventoExistente.getLongitud()));
        btnGuardar.setText("ACTUALIZAR EVENTO");
    }

    private void guardarEvento() {
        String nombre = edtNombre.getText().toString().trim();
        String fecha = edtFecha.getText().toString().trim();
        String descripcion = edtDescripcion.getText().toString().trim();
        String foto = edtFoto.getText().toString().trim();
        String latStr = edtLat.getText().toString().trim();
        String longStr = edtLong.getText().toString().trim();

        if (nombre.isEmpty() || fecha.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = 0, lon = 0;
        try {
            if (!latStr.isEmpty()) lat = Double.parseDouble(latStr);
            if (!longStr.isEmpty()) lon = Double.parseDouble(longStr);
        } catch (Exception e) {
            Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventoExistente == null) {
            Evento nuevo = new Evento(UUID.randomUUID().toString(), nombre, fecha, descripcion, foto, lat, lon);
            daoEvento.insertar(nuevo);
            Toast.makeText(this, "Evento registrado con éxito", Toast.LENGTH_SHORT).show();
        } else {
            eventoExistente.setNombre(nombre);
            eventoExistente.setFecha(fecha);
            eventoExistente.setDescripcion(descripcion);
            eventoExistente.setFotoUrl(foto);
            eventoExistente.setLatitud(lat);
            eventoExistente.setLongitud(lon);
            daoEvento.actualizar(eventoExistente);
            Toast.makeText(this, "Evento actualizado con éxito", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}