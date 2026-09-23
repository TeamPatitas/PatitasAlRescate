package com.patitasalrescate.controllers.management;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.CreateEventRequest;
import com.patitasalrescate.data.remote.dto.EventResponse;
import com.patitasalrescate.data.remote.dto.UpdateEventRequest;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.utils.ApiApp;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadRegistrarEvento extends AppCompatActivity {
    private EditText edtNombre, edtFecha, edtDescripcion, edtFoto, edtLat, edtLong;
    private Button btnGuardar;
    private Evento eventoExistente;
    private Uri fotoSeleccionada;
    private ActivityResultLauncher<String> elegirFoto;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.ly_registrar_evento);
        edtNombre = findViewById(R.id.edt_nombre_evento);
        edtFecha = findViewById(R.id.edt_fecha_evento);
        edtDescripcion = findViewById(R.id.edt_descripcion_evento);
        edtFoto = findViewById(R.id.edt_foto_evento);
        edtLat = findViewById(R.id.edt_latitud_evento);
        edtLong = findViewById(R.id.edt_longitud_evento);
        btnGuardar = findViewById(R.id.btn_guardar_evento);
        eventoExistente = (Evento) getIntent().getSerializableExtra("evento_editar_key");
        Toolbar toolbar = findViewById(R.id.toolbarRegistrarEvento);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(eventoExistente == null ? "Registrar Evento" : "Editar Evento");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        elegirFoto = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            fotoSeleccionada = uri;
            if (uri != null) edtFoto.setText("Imagen seleccionada");
        });
        edtFoto.setFocusable(false);
        edtFoto.setOnClickListener(v -> elegirFoto.launch("image/*"));
        if (eventoExistente != null) {
            edtNombre.setText(eventoExistente.getNombre());
            String fechaActual = eventoExistente.getFecha();
            edtFecha.setText(fechaActual == null ? "" : fechaActual.replace('T', ' ').replaceAll("(\\d{2}:\\d{2}).*", "$1"));
            edtDescripcion.setText(eventoExistente.getDescripcion());
            edtLat.setText(String.valueOf(eventoExistente.getLatitud()));
            edtLong.setText(String.valueOf(eventoExistente.getLongitud()));
            btnGuardar.setText("ACTUALIZAR EVENTO");
        }
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        String nombre = edtNombre.getText().toString().trim();
        if (nombre.isEmpty()) { edtNombre.setError("Ingresa un nombre"); return; }
        String fecha;
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            input.setLenient(false);
            Date date = input.parse(edtFecha.getText().toString().trim());
            if (date == null) throw new ParseException("Fecha vacía", 0);
            fecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(date);
        } catch (ParseException e) {
            edtFecha.setError("Usa AAAA-MM-DD HH:MM");
            return;
        }
        String lat = edtLat.getText().toString().trim();
        String lon = edtLong.getText().toString().trim();
        try {
            if (!lat.isEmpty()) Double.parseDouble(lat);
            if (!lon.isEmpty()) Double.parseDouble(lon);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
            return;
        }
        CreateEventRequest data = new CreateEventRequest();
        data.name = nombre;
        data.eventDate = fecha;
        data.description = edtDescripcion.getText().toString().trim();
        data.latitude = lat.isEmpty() ? null : lat;
        data.longitude = lon.isEmpty() ? null : lon;
        data.isActive = true;
        try { if (fotoSeleccionada != null) data.image = ApiApp.upload(this, fotoSeleccionada); }
        catch (IOException e) { Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show(); return; }
        btnGuardar.setEnabled(false);
        Call<EventResponse> call;
        if (eventoExistente == null) call = ApiApp.client().events.createEvent(data);
        else {
            UpdateEventRequest update = new UpdateEventRequest();
            update.name = data.name;
            update.eventDate = data.eventDate;
            update.description = data.description;
            update.latitude = data.latitude;
            update.longitude = data.longitude;
            update.image = data.image;
            call = ApiApp.client().events.updateEvent(eventoExistente.getIdEvento(), update);
        }
        call.enqueue(new Callback<EventResponse>() {
            @Override public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                btnGuardar.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ActividadRegistrarEvento.this, "Evento guardado", Toast.LENGTH_SHORT).show();
                    finish();
                } else Toast.makeText(ActividadRegistrarEvento.this, "Error al guardar (" + response.code() + ")", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<EventResponse> call, Throwable error) {
                if (isFinishing() || isDestroyed()) return;
                btnGuardar.setEnabled(true);
                Toast.makeText(ActividadRegistrarEvento.this, "Sin conexión con eventos", Toast.LENGTH_LONG).show();
            }
        });
    }
}
