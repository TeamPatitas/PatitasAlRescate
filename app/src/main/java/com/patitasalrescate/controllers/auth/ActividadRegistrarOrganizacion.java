package com.patitasalrescate.controllers.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.management.SelectLocation;
import com.patitasalrescate.data.remote.dto.CreateShelterRequest;
import com.patitasalrescate.data.remote.dto.ShelterResponse;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.PatitasSessionManager;
import java.io.IOException;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Creates a shelter for the signed-in user. Approval remains a separate dev action. */
public class ActividadRegistrarOrganizacion extends com.patitasalrescate.controllers.base.BaseActivity {
    private EditText nombre, direccion, telefono;
    private SwitchMaterial switchMostrarDireccion;
    private Button guardar;
    private Uri foto;
    private Double latitudSeleccionada = null;
    private Double longitudSeleccionada = null;
    private ActivityResultLauncher<Intent> launcherUbicacion;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.ly_registrar_organizacion);
        Toolbar toolbar = findViewById(R.id.toolbarRegistrarOrganizacion);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (!ApiApp.client().session.isAuthenticated()) {
            Toast.makeText(this, "Inicia sesión antes de registrar un refugio", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        nombre = findViewById(R.id.rj_text_org_nombre);
        direccion = findViewById(R.id.rj_text_org_direccion);
        telefono = findViewById(R.id.rj_text_org_telefono);
        switchMostrarDireccion = findViewById(R.id.rj_switch_mostrar_direccion);
        guardar = findViewById(R.id.rj_button_registrar_organizacion);

        int colorTexto = getColor(R.color.texto_busqueda);
        int colorHint = getColor(R.color.textoSecundario);
        nombre.setTextColor(colorTexto);
        nombre.setHintTextColor(colorHint);
        direccion.setTextColor(colorTexto);
        direccion.setHintTextColor(colorHint);
        telefono.setTextColor(colorTexto);
        telefono.setHintTextColor(colorHint);

        launcherUbicacion = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        latitudSeleccionada = result.getData().getDoubleExtra(SelectLocation.EXTRA_LATITUD, -7.1617);
                        longitudSeleccionada = result.getData().getDoubleExtra(SelectLocation.EXTRA_LONGITUD, -78.5128);
                        String dir = result.getData().getStringExtra(SelectLocation.EXTRA_DIRECCION);
                        if (dir != null && !dir.trim().isEmpty()) {
                            direccion.setText(dir.trim());
                        } else {
                            direccion.setText(String.format(Locale.US, "%.5f, %.5f", latitudSeleccionada, longitudSeleccionada));
                        }
                        direccion.setTextColor(colorTexto);
                        direccion.setError(null);
                    }
                });

        View.OnClickListener abrirSelectorMapa = v -> {
            Intent intent = new Intent(ActividadRegistrarOrganizacion.this, SelectLocation.class);
            if (latitudSeleccionada != null && longitudSeleccionada != null) {
                intent.putExtra(SelectLocation.EXTRA_LATITUD, latitudSeleccionada);
                intent.putExtra(SelectLocation.EXTRA_LONGITUD, longitudSeleccionada);
            }
            launcherUbicacion.launch(intent);
        };
        findViewById(R.id.btn_abrir_mapa).setOnClickListener(abrirSelectorMapa);
        direccion.setOnClickListener(abrirSelectorMapa);

        // phoneNumber es requerido por la API: el campo teléfono queda visible.
        ocultarCampo(R.id.rj_text_org_correo);
        ocultarCampo(R.id.rj_text_org_password);
        ActivityResultLauncher<String> elegirFoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    foto = uri;
                    if (uri != null) Glide.with(this).load(uri).into(
                            (android.widget.ImageView) findViewById(R.id.img_preview_refugio));
                });
        findViewById(R.id.btn_seleccionar_foto).setOnClickListener(v -> elegirFoto.launch("image/*"));
        guardar.setOnClickListener(v -> crearRefugio());
    }

    private void ocultarCampo(int id) {
        View field = findViewById(id);
        android.view.ViewParent parent = field.getParent();
        while (parent instanceof View && !(parent instanceof com.google.android.material.textfield.TextInputLayout))
            parent = parent.getParent();
        if (parent instanceof View) ((View) parent).setVisibility(View.GONE);
    }

    private void crearRefugio() {
        if (!ApiApp.exigirOnline(this)) return;
        CreateShelterRequest request = new CreateShelterRequest();
        request.name = nombre.getText().toString().trim();
        request.phoneNumber = telefono.getText().toString().trim();
        if (request.name.isEmpty()) { nombre.setError("Ingresa un nombre"); return; }
        if (latitudSeleccionada == null || longitudSeleccionada == null) {
            direccion.setError("Selecciona la ubicación en el mapa");
            Toast.makeText(this, "Por favor selecciona la ubicación en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }
        if (request.phoneNumber.isEmpty()) { telefono.setError("Ingresa un teléfono"); return; }
        request.latitude = latitudSeleccionada;
        request.longitude = longitudSeleccionada;
        request.showAddress = switchMostrarDireccion != null ? switchMostrarDireccion.isChecked() : true;

        try { if (foto != null) request.photo = ApiApp.upload(this, foto); }
        catch (IOException error) { Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show(); return; }
        guardar.setEnabled(false);
        ApiApp.client().shelters.createShelter(request).enqueue(new Callback<ShelterResponse>() {
            @Override public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                guardar.setEnabled(true);
                if (response.isSuccessful()) {
                    // Guardar solicitud: shelterId + momento, para ocultar "Solicitar aprobación" por 24h.
                    PatitasSessionManager session =
                            PatitasSessionManager.getInstance(ActividadRegistrarOrganizacion.this);
                    if (response.body() != null && response.body().id != null) {
                        session.setShelterId(response.body().id);
                        getSharedPreferences("PatitasShelterConfig", MODE_PRIVATE).edit()
                                .putBoolean("show_address_" + response.body().id, Boolean.TRUE.equals(request.showAddress))
                                .apply();
                    }
                    session.setShelterRequestAt(System.currentTimeMillis());
                    Toast.makeText(ActividadRegistrarOrganizacion.this,
                            "Refugio enviado. Un administrador debe habilitarlo.", Toast.LENGTH_LONG).show();
                    finish();
                } else Toast.makeText(ActividadRegistrarOrganizacion.this,
                        "Error al registrar (" + response.code() + ")", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<ShelterResponse> call, Throwable error) {
                if (isFinishing() || isDestroyed()) return;
                guardar.setEnabled(true);
                Toast.makeText(ActividadRegistrarOrganizacion.this,
                        "Sin conexión con refugios", Toast.LENGTH_LONG).show();
            }
        });
    }
}
