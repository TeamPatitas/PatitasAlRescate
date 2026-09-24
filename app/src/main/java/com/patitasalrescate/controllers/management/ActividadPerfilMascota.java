package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.data.mock.DAOMascota;
import com.patitasalrescate.data.mock.DAOFavoritos;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.data.remote.dto.PetResponse;
import com.patitasalrescate.data.remote.dto.UpdatePetRequest;
import com.patitasalrescate.data.remote.dto.Species;
import com.patitasalrescate.data.remote.dto.Gender;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class ActividadPerfilMascota extends AppCompatActivity {
    private EditText txtNombre, txtEspecie, txtRaza, txtSexo, txtEdad, txtTemperamento, txtHistoria;
    private ImageView imgFoto;
    private Button btnAccion;
    private Button btnFavorito;
    private DAOMascota daoMascota;
    private DAOFavoritos daoFavoritos;
    private Mascota mascotaActual;
    private String idMascota;
    private String idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_perfil_mascota);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.perfilmascota), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        daoMascota = new DAOMascota(this);
        daoFavoritos = new DAOFavoritos(this);

        initViews();
        configToolbar();

        idMascota = getIntent().getStringExtra("id_mascota_key");

        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        idUsuario = session.getUserId();

        if (idMascota == null || idMascota.isEmpty()) {
            Toast.makeText(this, "Error: no llegó el ID de la mascota", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarDatosMascota();
    }

    private void initViews() {
        txtNombre = findViewById(R.id.txt_edit_nombre);
        txtEspecie = findViewById(R.id.txt_edit_especie);
        txtRaza = findViewById(R.id.txt_edit_raza);
        txtSexo = findViewById(R.id.txt_edit_sexo);
        txtEdad = findViewById(R.id.txt_edit_edad);
        txtTemperamento = findViewById(R.id.txt_edit_temperamento);
        txtHistoria = findViewById(R.id.txt_edit_historia);
        imgFoto = findViewById(R.id.img_detalle_mascota);
        btnAccion = findViewById(R.id.btn_accion_principal);
        btnFavorito = findViewById(R.id.btn_favorito);
    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarPerfilMascota);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles de la Mascota");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void cargarDatosMascota() {
        ApiApp.client().pets.getPetById(idMascota).enqueue(new Callback<PetResponse>() {
            @Override public void onResponse(Call<PetResponse> call, Response<PetResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ActividadPerfilMascota.this, "Mascota no encontrada", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                mascotaActual = ApiApp.pet(response.body());
                mostrarMascota();
                configurarModoVisualPorRol();
            }
            @Override public void onFailure(Call<PetResponse> call, Throwable error) {
                if (!isFinishing()) Toast.makeText(ActividadPerfilMascota.this, "Sin conexión con mascotas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarMascota() {
        txtNombre.setText(valorSeguro(mascotaActual.getNombre()));
        txtEspecie.setText(valorSeguro(mascotaActual.getEspecie()));
        txtRaza.setText(valorSeguro(mascotaActual.getRaza()));
        txtSexo.setText(valorSeguro(mascotaActual.getSexo()));
        txtEdad.setText("No disponible");
        txtTemperamento.setText(valorSeguro(mascotaActual.getTemperamento()));
        txtHistoria.setText(valorSeguro(mascotaActual.getHistoria()));

        List<String> fotos = mascotaActual.getFotos();
        if (fotos != null && !fotos.isEmpty()) {
            Glide.with(this)
                    .load(fotos.get(0))
                    .centerCrop()
                    .into(imgFoto);
        }
    }

    private String valorSeguro(String s) {
        return s == null ? "" : s;
    }

    private void configurarModoVisualPorRol() {
        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        boolean esRefugio = session.isRefugio();

        if (esRefugio) {
            btnFavorito.setVisibility(View.GONE);
            txtEdad.setEnabled(false);
            habilitarCampos(false);
            btnAccion.setText("EDITAR MASCOTA");
            btnAccion.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));

            btnAccion.setOnClickListener(v -> {
                if (!txtNombre.isEnabled()) {
                    habilitarCampos(true);
                    btnAccion.setText("GUARDAR CAMBIOS");
                    btnAccion.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                } else {
                    guardarCambios();
                }
            });
            return;
        }

        btnFavorito.setVisibility(View.GONE);
        habilitarCampos(false);

        String estado = mascotaActual.getEstado();
        if (estado == null) estado = "DISPONIBLE";

        switch (estado) {
            case "ADOPTADO":
                btnAccion.setText("YA FUE ADOPTADO");
                btnAccion.setEnabled(false);
                break;
            case "EN_PROCESO":
                btnAccion.setText("EN PROCESO DE ADOPCIÓN");
                btnAccion.setEnabled(false);
                break;
            default:
                btnAccion.setText("¡QUIERO ADOPTARLO! 🐾");
                btnAccion.setEnabled(true);
                btnAccion.setOnClickListener(v -> Toast.makeText(this,
                        "La API aún no permite solicitar adopciones", Toast.LENGTH_LONG).show());
                break;
        }
    }

    private void habilitarCampos(boolean habilitar) {
        int drawableRes = habilitar ? android.R.drawable.edit_text : android.R.color.transparent;
        
        txtNombre.setEnabled(habilitar);
        txtNombre.setBackgroundResource(drawableRes);
        
        txtEspecie.setEnabled(habilitar);
        txtEspecie.setBackgroundResource(drawableRes);
        
        txtRaza.setEnabled(habilitar);
        txtRaza.setBackgroundResource(drawableRes);
        
        txtSexo.setEnabled(habilitar);
        txtSexo.setBackgroundResource(drawableRes);
        
        txtEdad.setEnabled(habilitar);
        txtEdad.setBackgroundResource(drawableRes);
        txtEdad.setEnabled(false);
        
        txtTemperamento.setEnabled(habilitar);
        txtTemperamento.setBackgroundResource(drawableRes);
        
        txtHistoria.setEnabled(habilitar);
        txtHistoria.setBackgroundResource(drawableRes);
    }

    private void guardarCambios() {
        UpdatePetRequest update = new UpdatePetRequest();
        update.name = txtNombre.getText().toString().trim();
        update.breed = txtRaza.getText().toString().trim();
        update.temperament = txtTemperamento.getText().toString().trim();
        update.story = txtHistoria.getText().toString().trim();
        try {
            update.species = Species.valueOf(txtEspecie.getText().toString().trim().toUpperCase());
            update.gender = Gender.valueOf(txtSexo.getText().toString().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Especie o sexo inválidos", Toast.LENGTH_SHORT).show();
            return;
        }
        btnAccion.setEnabled(false);
        ApiApp.client().pets.updatePet(idMascota, update).enqueue(new Callback<PetResponse>() {
            @Override public void onResponse(Call<PetResponse> call, Response<PetResponse> response) {
                btnAccion.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ActividadPerfilMascota.this, "Mascota actualizada", Toast.LENGTH_SHORT).show();
                    finish();
                } else Toast.makeText(ActividadPerfilMascota.this, "Error al guardar (" + response.code() + ")", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<PetResponse> call, Throwable error) {
                btnAccion.setEnabled(true);
                Toast.makeText(ActividadPerfilMascota.this, "Sin conexión al guardar", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void irAAdoptar() {
        Intent i = new Intent(this, ActividadAdopcion.class);
        i.putExtra("id_mascota_key", mascotaActual.getIdMascota());
        startActivity(i);
    }
}
