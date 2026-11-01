package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.CreateEventRequest;
import com.patitasalrescate.data.remote.dto.EventResponse;
import com.patitasalrescate.data.remote.dto.UpdateEventRequest;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.utils.ApiApp;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadRegistrarEvento extends com.patitasalrescate.controllers.base.BaseActivity {

    private EditText edtNombre, edtFecha, edtDescripcion;
    private TextView txtUbicacionSeleccionada;
    private ImageView imgPreview;
    private Button btnGuardar, btnMapa;
    private Evento eventoExistente;
    private Uri fotoSeleccionada;
    private Double latitudSeleccionada;
    private Double longitudSeleccionada;
    private ActivityResultLauncher<String> elegirFoto;
    private ActivityResultLauncher<Intent> selectorUbicacion;
    private Calendar fechaSeleccionada;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.ly_registrar_evento);
        edtNombre = findViewById(R.id.edt_nombre_evento);
        edtFecha = findViewById(R.id.edt_fecha_evento);
        edtDescripcion = findViewById(R.id.edt_descripcion_evento);
        txtUbicacionSeleccionada = findViewById(R.id.txt_ubicacion_seleccionada);
        imgPreview = findViewById(R.id.img_preview_evento);
        ImageButton btnFoto = findViewById(R.id.btn_foto_evento);
        btnMapa = findViewById(R.id.btn_mapa_evento);
        btnGuardar = findViewById(R.id.btn_guardar_evento);
        eventoExistente = (Evento) getIntent().getSerializableExtra("evento_editar_key");
        Toolbar toolbar = findViewById(R.id.toolbarRegistrarEvento);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(eventoExistente == null ? "Registrar Evento" : "Editar Evento");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        selectorUbicacion = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        double lat = result.getData().getDoubleExtra(SelectLocation.EXTRA_LATITUD, 0);
                        double lng = result.getData().getDoubleExtra(SelectLocation.EXTRA_LONGITUD, 0);
                        String dir = result.getData().getStringExtra(SelectLocation.EXTRA_DIRECCION);
                        latitudSeleccionada = lat;
                        longitudSeleccionada = lng;
                        if (txtUbicacionSeleccionada != null) {
                            String texto = (dir != null && !dir.trim().isEmpty())
                                    ? dir.trim()
                                    : String.format(Locale.US, "%.5f, %.5f", lat, lng);
                            txtUbicacionSeleccionada.setText(texto);
                            txtUbicacionSeleccionada.setVisibility(View.VISIBLE);
                        }
                    }
                });

        elegirFoto = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) return;
            String mime = null;
            try {
                mime = getContentResolver().getType(uri);
            } catch (Exception ignored) { }
            if (!"image/jpeg".equalsIgnoreCase(mime) && !"image/png".equalsIgnoreCase(mime)
                    && !"image/webp".equalsIgnoreCase(mime)) {
                Toast.makeText(this, "Solo se permiten imágenes jpeg, png o webp",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            fotoSeleccionada = uri;
            Glide.with(this).load(uri).centerCrop().into(imgPreview);
        });
        findViewById(R.id.frame_foto_evento).setOnClickListener(v -> elegirFoto.launch("image/*"));
        btnFoto.setOnClickListener(v -> elegirFoto.launch("image/*"));

        edtFecha.setOnClickListener(v -> mostrarCalendario());
        btnMapa.setOnClickListener(v -> abrirSelectorUbicacion());

        if (eventoExistente != null) {
            edtNombre.setText(eventoExistente.getNombre());
            fechaSeleccionada = isoACalendario(eventoExistente.getFecha());
            edtFecha.setText(fechaBonita(fechaSeleccionada));
            edtDescripcion.setText(eventoExistente.getDescripcion());
            if (eventoExistente.getLatitud() != 0 || eventoExistente.getLongitud() != 0) {
                latitudSeleccionada = eventoExistente.getLatitud();
                longitudSeleccionada = eventoExistente.getLongitud();
                resolverYMostrarDireccion(latitudSeleccionada, longitudSeleccionada);
            }
            if (eventoExistente.getFotoUrl() != null && !eventoExistente.getFotoUrl().isEmpty()) {
                Glide.with(this).load(eventoExistente.getFotoUrl()).centerCrop().into(imgPreview);
            }
            btnGuardar.setText("ACTUALIZAR EVENTO");
        }
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void resolverYMostrarDireccion(double lat, double lng) {
        final String respaldo = String.format(Locale.US, "%.5f, %.5f", lat, lng);
        if (txtUbicacionSeleccionada != null) {
            txtUbicacionSeleccionada.setText(respaldo);
            txtUbicacionSeleccionada.setVisibility(View.VISIBLE);
        }
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(ActividadRegistrarEvento.this, new Locale("es", "PE"));
                List<Address> dirs = geocoder.getFromLocation(lat, lng, 1);
                if (dirs != null && !dirs.isEmpty() && dirs.get(0).getAddressLine(0) != null) {
                    final String linea = dirs.get(0).getAddressLine(0).trim();
                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed() && txtUbicacionSeleccionada != null) {
                            txtUbicacionSeleccionada.setText(linea);
                        }
                    });
                }
            } catch (Exception ignored) { }
        }).start();
    }

    /** Calendario Material seguido de reloj; muestra "6, Ago 2026 10:30". */
    private void mostrarCalendario() {
        Calendar base = fechaSeleccionada != null ? (Calendar) fechaSeleccionada.clone()
                : Calendar.getInstance();
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Elige la fecha")
                .setSelection(base.getTimeInMillis())
                .setTheme(R.style.ThemeOverlay_Patitas_CalendarLight)
                .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            utc.setTimeInMillis(selection);
            mostrarReloj(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH),
                    utc.get(Calendar.DAY_OF_MONTH));
        });
        datePicker.show(getSupportFragmentManager(), "fecha_evento");
    }

    private void mostrarReloj(int anio, int mes, int dia) {
        Calendar base = fechaSeleccionada != null ? fechaSeleccionada : Calendar.getInstance();
        boolean es24h = android.text.format.DateFormat.is24HourFormat(this);
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTheme(R.style.ThemeOverlay_Patitas_TimePickerLight)
                .setTimeFormat(es24h ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H)
                .setHour(base.get(Calendar.HOUR_OF_DAY))
                .setMinute(base.get(Calendar.MINUTE))
                .setTitleText("Elige la hora")
                .build();
        timePicker.addOnPositiveButtonClickListener(v -> {
            fechaSeleccionada = Calendar.getInstance();
            fechaSeleccionada.set(anio, mes, dia, timePicker.getHour(), timePicker.getMinute(), 0);
            fechaSeleccionada.set(Calendar.MILLISECOND, 0);
            edtFecha.setText(fechaBonita(fechaSeleccionada));
            edtFecha.setError(null);
        });
        timePicker.show(getSupportFragmentManager(), "hora_evento");
    }

    private String fechaBonita(Calendar cal) {
        if (cal == null) return "";
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        return cal.get(Calendar.DAY_OF_MONTH) + ", " + meses[cal.get(Calendar.MONTH)]
                + " " + cal.get(Calendar.YEAR) + " "
                + String.format(Locale.US, "%02d:%02d",
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    private Calendar isoACalendario(String iso) {
        if (iso == null || iso.trim().length() < 10) return null;
        String[] formatos = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"};
        for (String formato : formatos) {
            try {
                SimpleDateFormat entrada = new SimpleDateFormat(formato, Locale.US);
                entrada.setLenient(false);
                java.util.Date fecha = entrada.parse(iso.trim());
                if (fecha == null) continue;
                Calendar cal = Calendar.getInstance();
                cal.setTime(fecha);
                return cal;
            } catch (Exception ignored) { }
        }
        return null;
    }

    private void abrirSelectorUbicacion() {
        Intent intent = new Intent(this, SelectLocation.class);
        if (latitudSeleccionada != null && longitudSeleccionada != null) {
            intent.putExtra(SelectLocation.EXTRA_LATITUD, latitudSeleccionada);
            intent.putExtra(SelectLocation.EXTRA_LONGITUD, longitudSeleccionada);
        }
        selectorUbicacion.launch(intent);
    }

    private void guardar() {
        if (!ApiApp.exigirOnline(this)) return;
        String nombre = edtNombre.getText().toString().trim();
        if (nombre.isEmpty()) { edtNombre.setError("Ingresa un nombre"); return; }
        if (fechaSeleccionada == null) {
            edtFecha.setError("Elige fecha y hora");
            mostrarCalendario();
            return;
        }
        String fecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                .format(fechaSeleccionada.getTime());
        String lat = latitudSeleccionada == null ? null : String.valueOf(latitudSeleccionada);
        String lon = longitudSeleccionada == null ? null : String.valueOf(longitudSeleccionada);
        CreateEventRequest data = new CreateEventRequest();
        data.name = nombre;
        data.eventDate = fecha;
        data.description = edtDescripcion.getText().toString().trim();
        data.latitude = lat;
        data.longitude = lon;
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
