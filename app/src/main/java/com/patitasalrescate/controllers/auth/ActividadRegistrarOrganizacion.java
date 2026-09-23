package com.patitasalrescate.controllers.auth;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.CreateShelterRequest;
import com.patitasalrescate.data.remote.dto.ShelterResponse;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.PatitasSessionManager;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Creates a shelter for the signed-in user. Approval remains a separate dev action. */
public class ActividadRegistrarOrganizacion extends AppCompatActivity {
    private EditText nombre, direccion;
    private Button guardar;
    private Uri foto;

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
        guardar = findViewById(R.id.rj_button_registrar_organizacion);
        ocultarCampo(R.id.rj_text_org_telefono);
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
        CreateShelterRequest request = new CreateShelterRequest();
        request.name = nombre.getText().toString().trim();
        request.address = direccion.getText().toString().trim();
        if (request.name.isEmpty()) { nombre.setError("Ingresa un nombre"); return; }
        if (request.address.isEmpty()) { direccion.setError("Ingresa una dirección"); return; }
        try { if (foto != null) request.photo = ApiApp.upload(this, foto); }
        catch (IOException error) { Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show(); return; }
        guardar.setEnabled(false);
        ApiApp.client().shelters.createShelter(request).enqueue(new Callback<ShelterResponse>() {
            @Override public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                guardar.setEnabled(true);
                if (response.isSuccessful()) {
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
