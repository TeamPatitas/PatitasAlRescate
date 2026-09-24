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
import com.patitasalrescate.controllers.auth.ActividadRegistrarOrganizacion;
import com.patitasalrescate.data.mock.DAOAdoptante;
import com.patitasalrescate.model.Adoptante;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.ApiErrors;
import com.patitasalrescate.data.remote.dto.UpdateUserRequest;
import com.patitasalrescate.data.remote.dto.UserResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        enlazarVistas();
        configurarLauncherFoto();
        configurarListeners();
        txtNombreUsuario.setText(sesion.getUserName());
        txtTipoUsuario.setText("Perfil no disponible");
        findViewById(R.id.btnGuardarPerfil).setEnabled(false);
        btnCambiarFoto.setEnabled(false);
        btnEditarNombre.setEnabled(false);
        cargarDatosEnVista();
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
        btnEditarCorreo.setVisibility(android.view.View.GONE);
        btnEditarCelular.setVisibility(android.view.View.GONE);
        txtCorreoUsuarioInput.setEnabled(false);
        txtCelularUsuarioInput.setEnabled(false);
        txtCelularUsuarioInput.setHint("No disponible en la API");

        findViewById(R.id.btnGuardarPerfil).setOnClickListener(v -> guardarCambios());

        MaterialCardView cardFavoritos = findViewById(R.id.btnVerFavoritos);
        cardFavoritos.setVisibility(android.view.View.GONE);

        MaterialCardView cardCambiarModo = findViewById(R.id.btnCambiarModoRefugio);
        if (sesion.isRefugio()) {
            ((TextView) findViewById(R.id.txtAccesoRefugio)).setText("Modo adoptante");
            ((TextView) findViewById(R.id.txtAccesoRefugioDetalle)).setText("Cambiar de perfil");
        } else if (!sesion.getShelterId().isEmpty() && !sesion.canManageShelter()) {
            ((TextView) findViewById(R.id.txtAccesoRefugio)).setText("Refugio pendiente");
            ((TextView) findViewById(R.id.txtAccesoRefugioDetalle)).setText("Esperando aprobación");
        } else if (sesion.getShelterId().isEmpty()) {
            ((TextView) findViewById(R.id.txtAccesoRefugio)).setText("Registrar refugio");
            ((TextView) findViewById(R.id.txtAccesoRefugioDetalle)).setText("Solicitar aprobación");
        }
        cardCambiarModo.setOnClickListener(v -> {
            if (sesion.isRefugio()) {
                sesion.createSession(sesion.getUserId(), sesion.getUserName(), "ADOPTANTE", sesion.getShelterId());
                startActivity(new Intent(this, ActividadFeedAdoptante.class));
                finish();
                return;
            }
            if (!sesion.getShelterId().isEmpty() && !sesion.canManageShelter()) {
                Toast.makeText(this, "Un administrador debe habilitar tu refugio", Toast.LENGTH_LONG).show();
                return;
            }
            if (sesion.getShelterId().isEmpty()) {
                startActivity(new Intent(this, ActividadRegistrarOrganizacion.class));
                return;
            }
            sesion.createSession(sesion.getUserId(), sesion.getUserName(), "REFUGIO", sesion.getShelterId());
            startActivity(new Intent(this, ActividadFeedRefugio.class));
            finish();
        });

        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> {
            sesion.logout();
            ApiApp.client().session.logout();
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
        ApiApp.client().admin.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ActividadPerfilUsuario.this,
                            "No se pudo cargar el perfil (" + ApiErrors.describe(response) + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                findViewById(R.id.btnGuardarPerfil).setEnabled(true);
                btnCambiarFoto.setEnabled(true);
                btnEditarNombre.setEnabled(true);
                UserResponse user = response.body();
                String nombre = ((user.firstName == null ? "" : user.firstName) + " " +
                        (user.lastName == null ? "" : user.lastName)).trim();
                txtNombreUsuario.setText(nombre);
                txtNombreUsuarioInput.setText(nombre);
                txtCorreoUsuarioInput.setText(user.email);
                txtTipoUsuario.setText(sesion.getShelterId().isEmpty() ? "Adoptante" : "Refugio");
                if (user.photoUrl != null && !user.photoUrl.isEmpty()) Glide.with(ActividadPerfilUsuario.this)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.rj_persona)
                    .circleCrop()
                    .into(imgAvatarUsuario);
            }
            @Override public void onFailure(Call<UserResponse> call, Throwable error) {
                if (!isFinishing()) Toast.makeText(ActividadPerfilUsuario.this, "Sin conexión con perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarCambios() {
        String nombre = txtNombreUsuarioInput.getText().toString().trim();

        if (nombre.isEmpty()) {
            txtNombreUsuarioInput.setError("El nombre no puede estar vacío");
            habilitarCampo(txtNombreUsuarioInput);
            return;
        }
        String[] parts = nombre.split("\\s+", 2);
        UpdateUserRequest update = new UpdateUserRequest();
        update.firstName = parts[0];
        update.lastName = parts.length > 1 ? parts[1] : "";
        try { if (uriFotoNueva != null) update.photo = ApiApp.upload(this, uriFotoNueva); }
        catch (IOException error) { Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show(); return; }
        findViewById(R.id.btnGuardarPerfil).setEnabled(false);
        ApiApp.client().admin.updateCurrentUser(update).enqueue(new Callback<UserResponse>() {
            @Override public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                findViewById(R.id.btnGuardarPerfil).setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ActividadPerfilUsuario.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    txtNombreUsuarioInput.setEnabled(false);
                    cargarDatosEnVista();
                } else Toast.makeText(ActividadPerfilUsuario.this, "Error al guardar (" + response.code() + ")", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<UserResponse> call, Throwable error) {
                if (!isFinishing()) {
                    findViewById(R.id.btnGuardarPerfil).setEnabled(true);
                    Toast.makeText(ActividadPerfilUsuario.this, "Sin conexión al guardar", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
