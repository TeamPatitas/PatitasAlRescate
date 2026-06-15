package com.patitasalrescate.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.data_access.SupabaseService;
import com.patitasalrescate.data_access.DAOMascota;
import com.patitasalrescate.data_access.DAOFavoritos;
import com.patitasalrescate.model.Mascota;

import java.util.List;

public class ActividadPerfilMascota extends AppCompatActivity {
    private EditText txtNombre, txtEspecie, txtRaza, txtSexo, txtEdad, txtTemperamento, txtHistoria;
    private ImageView imgFoto;
    private Button btnAccion;
    private Button btnFavorito;
    private DAOMascota daoMascota;
    private DAOFavoritos daoFavoritos;
    private SupabaseService supabaseService;
    private Mascota mascotaActual;
    private String idMascota;
    private String tipoUsuario;
    private String idUsuario;
    private boolean esModoEdicion = false;

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
        supabaseService = new SupabaseService();

        initViews();

        idMascota = getIntent().getStringExtra("id_mascota_key");
        esModoEdicion = getIntent().getBooleanExtra("es_modo_edicion", false);

        tipoUsuario = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_TIPO_USUARIO);
        idUsuario = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO);

        if (tipoUsuario == null || tipoUsuario.trim().isEmpty()) {
            tipoUsuario = esModoEdicion ? "REFUGIO" : "ADOPTANTE";
        }

        if (idMascota == null || idMascota.isEmpty()) {
            Toast.makeText(this, "Error: no llegó el ID de la mascota", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarDatosMascota();
        configurarModoVisualPorRol();
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
    private void cargarDatosMascota() {

        mascotaActual = daoMascota.obtenerPorId(idMascota);

        if (mascotaActual == null) {
            Toast.makeText(this, "Mascota no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        txtNombre.setText(valorSeguro(mascotaActual.getNombre()));
        txtEspecie.setText(valorSeguro(mascotaActual.getEspecie()));
        txtRaza.setText(valorSeguro(mascotaActual.getRaza()));
        txtSexo.setText(valorSeguro(mascotaActual.getSexo()));
        txtEdad.setText(String.valueOf(mascotaActual.getEdad()));
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

        boolean esRefugio = "REFUGIO".equalsIgnoreCase(tipoUsuario);

        if (esRefugio) {

            btnFavorito.setVisibility(View.GONE);
            habilitarCampos(false);
            btnAccion.setText("EDITAR MASCOTA");
            btnAccion.setBackgroundColor(
                    ContextCompat.getColor(this, android.R.color.holo_orange_dark));

            btnAccion.setOnClickListener(v -> {
                if (!txtNombre.isEnabled()) {
                    habilitarCampos(true);
                    btnAccion.setText("GUARDAR CAMBIOS");
                } else {
                    guardarCambios();
                }
            });
            return;
        }
        btnFavorito.setVisibility(View.VISIBLE);
        habilitarCampos(false);
        btnFavorito.setOnClickListener(v -> {
            if (idUsuario == null) {
                Toast.makeText(this,
                        "No se identificó al adoptante",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            long r = daoFavoritos.addFavorito(idUsuario, idMascota);

            if (r > 0) {
                Toast.makeText(this,
                        "Agregado a favoritos ❤️",
                        Toast.LENGTH_SHORT).show();
                // supabase
                new Thread(() -> {
                    try {
                        supabaseService.insertarFavorito(
                                idUsuario,
                                idMascota
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

            } else {
                Toast.makeText(this,
                        "Ya estaba en favoritos",
                        Toast.LENGTH_SHORT).show();
            }
        });
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
                btnAccion.setOnClickListener(v -> irAAdoptar());
                break;
        }
    }
    private void habilitarCampos(boolean habilitar) {
        txtNombre.setEnabled(habilitar);
        txtEspecie.setEnabled(habilitar);
        txtRaza.setEnabled(habilitar);
        txtSexo.setEnabled(habilitar);
        txtEdad.setEnabled(habilitar);
        txtTemperamento.setEnabled(habilitar);
        txtHistoria.setEnabled(habilitar);
    }

    private void guardarCambios() {

        mascotaActual.setNombre(txtNombre.getText().toString());
        mascotaActual.setEspecie(txtEspecie.getText().toString());
        mascotaActual.setRaza(txtRaza.getText().toString());
        mascotaActual.setSexo(txtSexo.getText().toString());
        mascotaActual.setTemperamento(txtTemperamento.getText().toString());
        mascotaActual.setHistoria(txtHistoria.getText().toString());

        try {
            mascotaActual.setEdad(Integer.parseInt(txtEdad.getText().toString()));
        } catch (Exception e) {
            mascotaActual.setEdad(0);
        }

        daoMascota.actualizar(mascotaActual);

        new Thread(() -> {
            try {
                supabaseService.actualizarMascota(mascotaActual);
            } catch (Exception ignored) {}
        }).start();

        finish();
    }

    private void irAAdoptar() {
        Intent i = new Intent(this, ActividadAdopcion.class);
        i.putExtra("id_mascota_key", mascotaActual.getIdMascota());
        i.putExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO, idUsuario);
        startActivity(i);
    }
}