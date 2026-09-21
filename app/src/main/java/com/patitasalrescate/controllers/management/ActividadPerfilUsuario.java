package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.auth.ActividadIngresar;
import com.patitasalrescate.controllers.feed.ActividadFeedAdoptante;
import com.patitasalrescate.controllers.feed.ActividadFeedRefugio;
import com.patitasalrescate.data_access.DAOAdoptante;
import com.patitasalrescate.model.Adoptante;
import com.patitasalrescate.utils.PatitasSessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ActividadPerfilUsuario extends AppCompatActivity {

    private DAOAdoptante daoAdoptante;
    private Adoptante adoptante;
    private PatitasSessionManager sesion;

    // Header
    private TextView txtNombreUsuario, txtTipoUsuario;
    private com.google.android.material.imageview.ShapeableImageView imgAvatarUsuario;
    private ImageButton btnCambiarFoto;

    // Campos editables
    private EditText txtNombreUsuarioInput, txtCorreoUsuarioInput, txtCelularUsuarioInput;
    private ImageButton btnEditarNombre, btnEditarCorreo, btnEditarCelular;

    private Uri uriFotoNueva; // solo se setea si el usuario elige una foto nueva

    private ActivityResultLauncher<Intent> launcherGaleria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_perfil_usuario);

        Toolbar toolbar = findViewById(R.id.toolbarPerfilUsuario);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        sesion = PatitasSessionManager.getInstance(this);
        daoAdoptante = new DAOAdoptante(this);
        adoptante = daoAdoptante.obtenerPorId(sesion.getUserId());

        enlazarVistas();
        configurarLauncherFoto();
        cargarDatosEnVista();
        configurarListeners();
    }

    private void enlazarVistas() {
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);
        txtTipoUsuario = findViewById(R.id.txtTipoUsuario);
        imgAvatarUsuario = findViewById(R.id.imgAvatarUsuario);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);

        txtNombreUsuarioInput = findViewById(R.id.txtNombreUsuarioInput);
        txtCorreoUsuarioInput = findViewById(R.id.txtCorreoUsuarioInput);
        txtCelularUsuarioInput = findViewById(R.id.txtCelularUsuarioInput);

        btnEditarNombre = findViewById(R.id.btnEditarNombre);
        btnEditarCorreo = findViewById(R.id.btnEditarCorreo);
        btnEditarCelular = findViewById(R.id.btnEditarCelular);
    }

    private void configurarListeners() {
        // Cámara sobre el avatar
        btnCambiarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            launcherGaleria.launch(intent);
        });

        // Lápiz por campo: habilita solo ese EditText y le da foco
        btnEditarNombre.setOnClickListener(v -> habilitarCampo(txtNombreUsuarioInput));
        btnEditarCorreo.setOnClickListener(v -> habilitarCampo(txtCorreoUsuarioInput));
        btnEditarCelular.setOnClickListener(v -> habilitarCampo(txtCelularUsuarioInput));

        findViewById(R.id.btnGuardarPerfil).setOnClickListener(v -> guardarCambios());

        MaterialCardView cardFavoritos = findViewById(R.id.btnVerFavoritos);
        cardFavoritos.setOnClickListener(v -> {
            Intent i = new Intent(this, ActividadFeedAdoptante.class);
            i.putExtra("navegarA", R.id.fragmentFavoritos);
            startActivity(i);
            finish();
        });

        MaterialCardView cardCambiarModo = findViewById(R.id.btnCambiarModoRefugio);
        cardCambiarModo.setOnClickListener(v -> {
            startActivity(new Intent(this, ActividadFeedRefugio.class));
            finish();
        });

        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> {
            sesion.logout();
            Intent i = new Intent(this, ActividadIngresar.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void habilitarCampo(EditText campo) {
        campo.setEnabled(true);
        campo.requestFocus();
        campo.setSelection(campo.getText().length());
    }

    private void configurarLauncherFoto() {
        launcherGaleria = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                Uri uriSeleccionada = result.getData().getData();
                Uri uriLocal = copiarImagenAAlmacenamientoInterno(uriSeleccionada);
                if (uriLocal != null) {
                    uriFotoNueva = uriLocal;
                    Glide.with(this).load(uriFotoNueva).circleCrop().into(imgAvatarUsuario);
                } else {
                    Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Uri copiarImagenAAlmacenamientoInterno(Uri uriOrigen) {
        try (InputStream in = getContentResolver().openInputStream(uriOrigen)) {
            if (in == null) return null;

            File carpetaFotos = new File(getFilesDir(), "avatares");
            if (!carpetaFotos.exists()) carpetaFotos.mkdirs();

            File archivoDestino = new File(carpetaFotos, "avatar_" + UUID.randomUUID() + ".jpg");
            try (OutputStream out = new FileOutputStream(archivoDestino)) {
                byte[] buffer = new byte[4096];
                int leido;
                while ((leido = in.read(buffer)) != -1) {
                    out.write(buffer, 0, leido);
                }
            }
            return Uri.fromFile(archivoDestino);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void cargarDatosEnVista() {
        if (adoptante == null) return;

        txtNombreUsuario.setText(adoptante.getNombre());
        txtTipoUsuario.setText(sesion.isRefugio() ? "Refugio" : "Adoptante");

        txtNombreUsuarioInput.setText(adoptante.getNombre());
        txtCorreoUsuarioInput.setText(adoptante.getCorreo());
        txtCelularUsuarioInput.setText(adoptante.getNumCelular());

        if (adoptante.getFotoUrl() != null && !adoptante.getFotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(adoptante.getFotoUrl())
                    .placeholder(R.drawable.rj_persona)
                    .circleCrop()
                    .into(imgAvatarUsuario);
        }
    }

    private void guardarCambios() {
        if (adoptante == null) return;

        String nombre = txtNombreUsuarioInput.getText().toString().trim();
        String correo = txtCorreoUsuarioInput.getText().toString().trim();
        String celular = txtCelularUsuarioInput.getText().toString().trim();

        if (nombre.isEmpty()) {
            txtNombreUsuarioInput.setError("El nombre no puede estar vacío");
            habilitarCampo(txtNombreUsuarioInput);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            txtCorreoUsuarioInput.setError("Correo inválido");
            habilitarCampo(txtCorreoUsuarioInput);
            return;
        }
        if (celular.length() != 9) {
            txtCelularUsuarioInput.setError("Teléfono de 9 dígitos");
            habilitarCampo(txtCelularUsuarioInput);
            return;
        }

        adoptante.setNombre(nombre);
        adoptante.setCorreo(correo);
        adoptante.setNumCelular(celular);

        if (uriFotoNueva != null) {
            adoptante.setFotoUrl(uriFotoNueva.toString());
        }

        int actualizado = daoAdoptante.actualizar(adoptante);
        if (actualizado == 1) {
            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
            // Vuelve a bloquear los campos y refresca el nombre grande del header
            txtNombreUsuarioInput.setEnabled(false);
            txtCorreoUsuarioInput.setEnabled(false);
            txtCelularUsuarioInput.setEnabled(false);
            txtNombreUsuario.setText(nombre);
        } else {
            Toast.makeText(this, "No se pudo actualizar el perfil", Toast.LENGTH_SHORT).show();
        }
    }
}