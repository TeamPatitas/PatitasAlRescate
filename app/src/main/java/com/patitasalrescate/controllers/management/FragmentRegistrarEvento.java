package com.patitasalrescate.controllers.management;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.CreateEventRequest;
import com.patitasalrescate.data.remote.dto.EventResponse;
import com.patitasalrescate.utils.ApiApp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentRegistrarEvento extends Fragment {

    private EditText edtNombre, edtFecha, edtDescripcion;
    private TextView txtUbicacionSeleccionada;
    private ImageView imgPreview;
    private Button btnGuardar, btnMapa;
    private Uri fotoSeleccionada;
    private Double latitudSeleccionada;
    private Double longitudSeleccionada;
    private ActivityResultLauncher<String> elegirFoto;
    private ActivityResultLauncher<Intent> selectorUbicacion;
    private Calendar fechaSeleccionada;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_registrar_evento, container, false);

        edtNombre = view.findViewById(R.id.edt_nombre_evento);
        edtFecha = view.findViewById(R.id.edt_fecha_evento);
        edtDescripcion = view.findViewById(R.id.edt_descripcion_evento);
        txtUbicacionSeleccionada = view.findViewById(R.id.txt_ubicacion_seleccionada);
        imgPreview = view.findViewById(R.id.img_preview_evento);
        ImageButton btnFoto = view.findViewById(R.id.btn_foto_evento);
        btnMapa = view.findViewById(R.id.btn_mapa_evento);
        btnGuardar = view.findViewById(R.id.btn_guardar_evento);

        selectorUbicacion = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null && isAdded()) {
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
            if (uri == null || !isAdded()) return;
            String mime = null;
            try {
                mime = requireContext().getContentResolver().getType(uri);
            } catch (Exception ignored) { }
            if (!"image/jpeg".equalsIgnoreCase(mime) && !"image/png".equalsIgnoreCase(mime)
                    && !"image/webp".equalsIgnoreCase(mime)) {
                Toast.makeText(requireContext(), "Solo se permiten imágenes jpeg, png o webp",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            fotoSeleccionada = uri;
            Glide.with(this).load(uri).centerCrop().into(imgPreview);
        });

        view.findViewById(R.id.frame_foto_evento).setOnClickListener(v -> elegirFoto.launch("image/*"));
        btnFoto.setOnClickListener(v -> elegirFoto.launch("image/*"));

        edtFecha.setOnClickListener(v -> mostrarCalendario());
        btnMapa.setOnClickListener(v -> abrirSelectorUbicacion());
        btnGuardar.setOnClickListener(v -> guardar());

        return view;
    }

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
        datePicker.show(getParentFragmentManager(), "fecha_evento");
    }

    private void mostrarReloj(int anio, int mes, int dia) {
        if (!isAdded()) return;
        Calendar base = fechaSeleccionada != null ? fechaSeleccionada : Calendar.getInstance();
        boolean es24h = android.text.format.DateFormat.is24HourFormat(requireContext());
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
        timePicker.show(getParentFragmentManager(), "hora_evento");
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

    private void abrirSelectorUbicacion() {
        if (!isAdded()) return;
        Intent intent = new Intent(requireContext(), SelectLocation.class);
        if (latitudSeleccionada != null && longitudSeleccionada != null) {
            intent.putExtra(SelectLocation.EXTRA_LATITUD, latitudSeleccionada);
            intent.putExtra(SelectLocation.EXTRA_LONGITUD, longitudSeleccionada);
        }
        selectorUbicacion.launch(intent);
    }

    private void guardar() {
        if (!isAdded() || !ApiApp.exigirOnline(requireContext())) return;
        String nombre = edtNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            edtNombre.setError("Ingresa un nombre");
            return;
        }
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
        if (latitudSeleccionada != null && longitudSeleccionada != null) {
            data.location = new com.patitasalrescate.data.remote.dto.Location(
                    latitudSeleccionada, longitudSeleccionada);
        }
        data.isActive = true;
        try {
            if (fotoSeleccionada != null) {
                data.image = ApiApp.upload(requireContext(), fotoSeleccionada);
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        btnGuardar.setEnabled(false);
        ApiApp.client().events.createEvent(data).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (!isAdded()) return;
                btnGuardar.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Evento guardado", Toast.LENGTH_SHORT).show();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                } else {
                    Toast.makeText(requireContext(), "Error al guardar (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable error) {
                if (!isAdded()) return;
                btnGuardar.setEnabled(true);
                Toast.makeText(requireContext(), "Sin conexión con eventos", Toast.LENGTH_LONG).show();
            }
        });
    }
}
