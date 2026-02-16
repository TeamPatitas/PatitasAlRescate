package com.patitasalrescate.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.SupabaseService;
import com.patitasalrescate.accesoADatos.DAOMascota;
import com.patitasalrescate.accesoADatos.DAOFavoritos;
import com.patitasalrescate.model.Mascota;

import java.util.List;

public class ActividadPerfilMascota extends AppCompatActivity {

    private EditText txtNombre, txtEspecie, txtRaza, txtEdad, txtTemperamento, txtHistoria;
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
        txtEdad.setText(String.valueOf(mascotaActual.getEdad()));
        txtTemperamento.setText(valorSeguro(mascotaActual.getTemperamento()));
        txtHistoria.setText(valorSeguro(mascotaActual.getHistoria()));

        List<String> fotos = mascotaActual.getFotos();
        if (fotos != null && !fotos.isEmpty()) {
            Glide.with(this)
                    .load(fotos.get(0))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(imgFoto);
        } else {
            imgFoto.setImageResource(R.drawable.ic_launcher_foreground);
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

            btnAccion.setText("EDITAR MASCOTA ✏️");
            btnAccion.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
            btnAccion.setOnClickListener(v -> {
                if (!txtNombre.isEnabled()) {
                    habilitarCampos(true);
                    btnAccion.setText("GUARDAR CAMBIOS ✅");
                } else {
                    guardarCambios();
                }
            });
            return;
        }

        // 🔥 ADOPTANTE
        btnFavorito.setVisibility(View.VISIBLE);
        habilitarCampos(false);

        btnFavorito.setOnClickListener(v -> {
            if (idUsuario == null) {
                Toast.makeText(this, "No se identificó al adoptante", Toast.LENGTH_SHORT).show();
                return;
            }

            long r = daoFavoritos.addFavorito(idUsuario, idMascota);
            if (r > 0) {
                Toast.makeText(this, "Agregado a favoritos ❤️", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔥 LÓGICA POR ESTADO
        String estado = mascotaActual.getEstado();
        if (estado == null) estado = "DISPONIBLE";

        switch (estado) {

            case "ADOPTADO":
                btnAccion.setText("YA FUE ADOPTADO ✅");
                btnAccion.setEnabled(false);
                btnAccion.setBackgroundColor(
                        ContextCompat.getColor(this, android.R.color.darker_gray));
                break;

            case "EN_PROCESO":
                btnAccion.setText("EN PROCESO DE ADOPCIÓN ⏳");
                btnAccion.setEnabled(false);
                btnAccion.setBackgroundColor(
                        ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                break;

            default:
                btnAccion.setText("¡QUIERO ADOPTARLO! 🐾");
                btnAccion.setEnabled(true);
                btnAccion.setBackgroundColor(
                        ContextCompat.getColor(this, android.R.color.holo_green_dark));
                btnAccion.setOnClickListener(v -> irAAdoptar());
                break;
        }
    }

    private void habilitarCampos(boolean habilitar) {
        txtNombre.setEnabled(habilitar);
        txtEspecie.setEnabled(habilitar);
        txtRaza.setEnabled(habilitar);
        txtEdad.setEnabled(habilitar);
        txtTemperamento.setEnabled(habilitar);
        txtHistoria.setEnabled(habilitar);
    }

    private void guardarCambios() {

        mascotaActual.setNombre(txtNombre.getText().toString().trim());
        mascotaActual.setEspecie(txtEspecie.getText().toString().trim());
        mascotaActual.setRaza(txtRaza.getText().toString().trim());
        mascotaActual.setTemperamento(txtTemperamento.getText().toString().trim());
        mascotaActual.setHistoria(txtHistoria.getText().toString().trim());

        try {
            mascotaActual.setEdad(Integer.parseInt(txtEdad.getText().toString().trim()));
        } catch (Exception e) {
            mascotaActual.setEdad(0);
        }

        int filas = daoMascota.actualizar(mascotaActual);

        if (filas > 0) {
            Toast.makeText(this, "Cambios guardados ✅", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                try {
                    supabaseService.actualizarMascota(mascotaActual);
                } catch (Exception ignored) {}
            }).start();

            finish();
        }
    }

    private void irAAdoptar() {
        Intent intent = new Intent(this, ActividadAdopcion.class);
        intent.putExtra("id_mascota_key", mascotaActual.getIdMascota());
        startActivity(intent);
    }
}
