package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadRegistrarEvento extends com.patitasalrescate.controllers.base.BaseActivity {
    private static final String TAG = "RegistrarEvento";
    private static final String[] MIME_FOTOS = {"image/jpeg", "image/png", "image/webp"};

    private EditText edtNombre, edtFecha, edtDescripcion, edtUbicacion;
    private ImageView imgPreview;
    private Button btnGuardar, btnMapa;
    private Evento eventoExistente;
    private Uri fotoSeleccionada;
    private ActivityResultLauncher<String> elegirFoto;
    private Calendar fechaSeleccionada;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.ly_registrar_evento);
        edtNombre = findViewById(R.id.edt_nombre_evento);
        edtFecha = findViewById(R.id.edt_fecha_evento);
        edtDescripcion = findViewById(R.id.edt_descripcion_evento);
        edtUbicacion = findViewById(R.id.edt_ubicacion_evento);
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
        btnMapa.setOnClickListener(v -> abrirGoogleMaps());

        if (eventoExistente != null) {
            edtNombre.setText(eventoExistente.getNombre());
            fechaSeleccionada = isoACalendario(eventoExistente.getFecha());
            edtFecha.setText(fechaBonita(fechaSeleccionada));
            edtDescripcion.setText(eventoExistente.getDescripcion());
            if (eventoExistente.getLatitud() != 0 || eventoExistente.getLongitud() != 0) {
                edtUbicacion.setText(eventoExistente.getLatitud() + ", " + eventoExistente.getLongitud());
            }
            if (eventoExistente.getFotoUrl() != null && !eventoExistente.getFotoUrl().isEmpty()) {
                Glide.with(this).load(eventoExistente.getFotoUrl()).centerCrop().into(imgPreview);
            }
            btnGuardar.setText("ACTUALIZAR EVENTO");
        }
        btnGuardar.setOnClickListener(v -> guardar());
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
            // MaterialDatePicker entrega millis UTC: se reconstruye el día en zona local.
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

    /** Abre Google Maps para buscar el lugar; luego se pegan las coordenadas o el enlace aquí. */
    private void abrirGoogleMaps() {
        String texto = edtUbicacion.getText().toString().trim();
        Uri uriMapa = texto.isEmpty()
                ? Uri.parse("geo:0,0?q=Cajamarca, Perú")
                : Uri.parse("geo:0,0?q=" + Uri.encode(texto));
        Intent intent = new Intent(Intent.ACTION_VIEW, uriMapa);
        intent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(intent);
            Toast.makeText(this, "Busca el lugar, compártelo y pega las coordenadas o el enlace aquí",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uriMapa));
            } catch (Exception ex) {
                Toast.makeText(this, "No hay aplicación de mapas instalada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean esEnlaceMaps(String texto) {
        if (texto == null) return false;
        String t = texto.trim().toLowerCase(java.util.Locale.US);
        return t.startsWith("http") || t.contains("goo.gl") || t.contains("google.")
                || t.contains("maps.app.goo.gl");
    }

    /**
     * Sigue la redirección del enlace corto (ej. maps.app.goo.gl/…) y extrae las
     * coordenadas de la URL final. Rellena el campo; hay que guardar de nuevo.
     */
    private void resolverEnlaceGoogleMaps(String texto) {
        String url = texto.trim();
        if (!url.toLowerCase(java.util.Locale.US).startsWith("http")) url = "https://" + url;
        final String finalUrl = url;
        Toast.makeText(this, "Resolviendo enlace…", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "resolviendo " + finalUrl);
        new Thread(() -> {
            try {
                okhttp3.OkHttpClient cliente = new okhttp3.OkHttpClient.Builder()
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                        .build();
                okhttp3.Request req = new okhttp3.Request.Builder()
                        .url(finalUrl)
                        .header("User-Agent", "Mozilla/5.0")
                        .build();
                String destino;
                try (okhttp3.Response resp = cliente.newCall(req).execute()) {
                    if (!resp.isSuccessful()) throw new java.io.IOException("HTTP " + resp.code());
                    destino = resp.request().url().toString();
                }
                Log.d(TAG, "enlace resuelto: " + destino);
                String[] coords = extraerCoordsUrl(destino);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (coords != null) {
                        edtUbicacion.setText(coords[0] + ", " + coords[1]);
                        edtUbicacion.setError(null);
                        Toast.makeText(this, "Ubicación detectada, revisa y guarda de nuevo",
                                Toast.LENGTH_LONG).show();
                    } else {
                        edtUbicacion.setError("No se hallaron coordenadas en el enlace");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "no se pudo resolver el enlace: " + e, e);
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(this, "No se pudo abrir el enlace, revisa tu conexión",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    /** Extrae {lat, lng} de una URL de Maps: @lat,lng · !3dlat!4dlng · ll=/q=/query=. */
    private String[] extraerCoordsUrl(String url) {
        if (url == null) return null;
        Matcher arroba = Pattern.compile("@(-?\\d+(\\.\\d+)?),(-?\\d+(\\.\\d+)?)").matcher(url);
        if (arroba.find()) return validarCoords(arroba.group(1), arroba.group(3));
        Matcher lugar = Pattern.compile("!3d(-?\\d+(\\.\\d+)?)!4d(-?\\d+(\\.\\d+)?)").matcher(url);
        if (lugar.find()) return validarCoords(lugar.group(1), lugar.group(3));
        Matcher param = Pattern.compile("[?&](?:ll|q|query)=(-?\\d+(\\.\\d+)?),(-?\\d+(\\.\\d+)?)")
                .matcher(url);
        if (param.find()) return validarCoords(param.group(1), param.group(3));
        return null;
    }

    private String[] validarCoords(String latTexto, String lngTexto) {
        try {
            double lat = Double.parseDouble(latTexto);
            double lng = Double.parseDouble(lngTexto);
            if (lat < -90 || lat > 90 || lng < -180 || lng > 180) return null;
            return new String[]{String.valueOf(lat), String.valueOf(lng)};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Acepta "lat, lng" o enlaces de Maps con "@lat,lng". Devuelve {lat, lng} o null si vacío.
     * Lanza IllegalArgumentException si hay texto inválido.
     */
    private String[] parsearUbicacion(String texto) {
        if (texto == null || texto.trim().isEmpty()) return null;
        Matcher matcher = Pattern.compile("(-?\\d+(\\.\\d+)?)\\s*,\\s*(-?\\d+(\\.\\d+)?)")
                .matcher(texto.trim());
        if (!matcher.find()) throw new IllegalArgumentException("Ubicación inválida");
        double lat = Double.parseDouble(matcher.group(1));
        double lng = Double.parseDouble(matcher.group(3));
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Coordenadas fuera de rango");
        }
        return new String[]{String.valueOf(lat), String.valueOf(lng)};
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
        String textoUbi = edtUbicacion.getText().toString().trim();
        if (esEnlaceMaps(textoUbi)) {
            // Se resuelve en segundo plano y rellena el campo; guardar de nuevo después.
            resolverEnlaceGoogleMaps(textoUbi);
            return;
        }
        String[] coords;
        try {
            coords = parsearUbicacion(textoUbi);
        } catch (IllegalArgumentException e) {
            edtUbicacion.setError(e.getMessage() + ": usa -7.15, -78.51");
            return;
        }
        String lat = coords == null ? null : coords[0];
        String lon = coords == null ? null : coords[1];
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
