package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class ActividadPerfilUsuario extends com.patitasalrescate.controllers.base.BaseActivity {

    private static final String TAG = "PerfilUsuario";

    private Adoptante adoptante;
    private PatitasSessionManager sesion;

    // Header
    private TextView txtNombreUsuario, txtTipoUsuario;
    private LinearLayout layoutRolesBadges;
    private com.google.android.material.imageview.ShapeableImageView imgAvatarUsuario;
    private ImageButton btnCambiarFoto;

    // Campos editables
    private EditText txtNombresUsuarioInput, txtApellidosUsuarioInput, txtCorreoUsuarioInput;
    private EditText txtNacimientoUsuarioInput, txtSexoUsuarioInput;
    private ImageButton btnEditarNombres, btnEditarApellidos, btnEditarCorreo;
    private ImageButton btnEditarNacimiento, btnEditarSexo;

    // Valores originales para enviar solo lo que cambió
    private String birthDateIsoOriginal;
    private String fechaIsoSeleccionada;
    private Integer genderOriginal;
    private Integer sexoSeleccionado;

    // Estado de la tarjeta de refugio (se refresca con los roles de la API)
    private String tarjetaModo = "REGISTRAR"; // ADMINISTRAR | PENDIENTE | REGISTRAR | MODO_ADOPTANTE
    private String tarjetaShelterId = "";

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

        enlazarVistas();
        configurarLauncherFoto();
        configurarListeners();
        txtNombreUsuario.setText(sesion.getUserName());
        txtTipoUsuario.setText("Perfil no disponible");
        findViewById(R.id.btnGuardarPerfil).setEnabled(false);
        btnCambiarFoto.setEnabled(false);
        btnEditarNombres.setEnabled(false);
        btnEditarApellidos.setEnabled(false);
        btnEditarNacimiento.setEnabled(false);
        btnEditarSexo.setEnabled(false);
        cargarDatosEnVista();
    }

    private void enlazarVistas() {
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);
        txtTipoUsuario = findViewById(R.id.txtTipoUsuario);
        layoutRolesBadges = findViewById(R.id.layoutRolesBadges);
        imgAvatarUsuario = findViewById(R.id.imgAvatarUsuario);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);

        txtNombresUsuarioInput = findViewById(R.id.txtNombresUsuarioInput);
        txtApellidosUsuarioInput = findViewById(R.id.txtApellidosUsuarioInput);
        txtCorreoUsuarioInput = findViewById(R.id.txtCorreoUsuarioInput);
        txtNacimientoUsuarioInput = findViewById(R.id.txtNacimientoUsuarioInput);
        txtSexoUsuarioInput = findViewById(R.id.txtSexoUsuarioInput);

        btnEditarNombres = findViewById(R.id.btnEditarNombres);
        btnEditarApellidos = findViewById(R.id.btnEditarApellidos);
        btnEditarCorreo = findViewById(R.id.btnEditarCorreo);
        btnEditarNacimiento = findViewById(R.id.btnEditarNacimiento);
        btnEditarSexo = findViewById(R.id.btnEditarSexo);
    }

    private void configurarListeners() {
        // Cámara sobre el avatar (ACTION_GET_CONTENT para que respete el filtro MIME)
        btnCambiarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES,
                    new String[]{"image/jpeg", "image/png", "image/webp"});
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            launcherGaleria.launch(intent);
        });

        // Lápiz por campo: habilita solo ese EditText y le da foco
        btnEditarNombres.setOnClickListener(v -> habilitarCampo(txtNombresUsuarioInput));
        btnEditarApellidos.setOnClickListener(v -> habilitarCampo(txtApellidosUsuarioInput));
        // La API (PATCH /admin/user) no permite cambiar el correo: sin lápiz.
        btnEditarCorreo.setVisibility(android.view.View.GONE);
        txtCorreoUsuarioInput.setEnabled(false);
        txtNacimientoUsuarioInput.setEnabled(false);
        txtSexoUsuarioInput.setEnabled(false);
        btnEditarNombres.setEnabled(false);
        btnEditarApellidos.setEnabled(false);
        btnEditarNacimiento.setEnabled(false);
        btnEditarSexo.setEnabled(false);

        // La fecha se edita con calendario, no con teclado.
        btnEditarNacimiento.setOnClickListener(v -> mostrarCalendario());
        txtNacimientoUsuarioInput.setOnClickListener(v -> {
            if (btnEditarNacimiento.isEnabled()) mostrarCalendario();
        });
        btnEditarSexo.setOnClickListener(v -> mostrarDialogoSexo());

        findViewById(R.id.btnGuardarPerfil).setOnClickListener(v -> guardarCambios());

        MaterialCardView cardFavoritos = findViewById(R.id.btnVerFavoritos);
        cardFavoritos.setVisibility(android.view.View.GONE);

        // Estado inicial con sesión local; se corrige con roles frescos al cargar la API.
        if (sesion.isRefugio()) {
            tarjetaModo = "MODO_ADOPTANTE";
            tarjetaShelterId = sesion.getShelterId();
        } else if (!sesion.getShelterId().isEmpty()) {
            tarjetaModo = "PENDIENTE";
            tarjetaShelterId = sesion.getShelterId();
        } else {
            tarjetaModo = "REGISTRAR";
            tarjetaShelterId = "";
        }
        pintarTarjetaRefugio();
        findViewById(R.id.btnCambiarModoRefugio).setOnClickListener(v -> onTarjetaRefugioClick());

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

    private void pintarTarjetaRefugio() {
        MaterialCardView card = findViewById(R.id.btnCambiarModoRefugio);
        TextView titulo = findViewById(R.id.txtAccesoRefugio);
        TextView detalle = findViewById(R.id.txtAccesoRefugioDetalle);
        switch (tarjetaModo) {
            case "ADMINISTRAR":
                card.setVisibility(android.view.View.VISIBLE);
                titulo.setText("Administrar refugio");
                detalle.setText("Gestionar tu refugio");
                break;
            case "PENDIENTE":
                // Solicitud reciente (< 24h): ocultar para no spamear "Solicitar aprobación".
                if (sesion.tieneSolicitudReciente()) {
                    card.setVisibility(android.view.View.GONE);
                    return;
                }
                card.setVisibility(android.view.View.VISIBLE);
                titulo.setText("Refugio pendiente");
                detalle.setText("Esperando aprobación");
                break;
            case "MODO_ADOPTANTE":
                card.setVisibility(android.view.View.VISIBLE);
                titulo.setText("Modo adoptante");
                detalle.setText("Cambiar de perfil");
                break;
            default:
                card.setVisibility(android.view.View.VISIBLE);
                titulo.setText("Registrar refugio");
                detalle.setText("Solicitar aprobación");
                break;
        }
    }

    private void onTarjetaRefugioClick() {
        switch (tarjetaModo) {
            case "ADMINISTRAR":
                sesion.createSession(sesion.getUserId(), sesion.getUserName(),
                        "REFUGIO", tarjetaShelterId, true);
                irAlFeed(ActividadFeedRefugio.class);
                break;
            case "PENDIENTE":
                Toast.makeText(this, "Un administrador debe habilitar tu refugio",
                        Toast.LENGTH_LONG).show();
                break;
            case "MODO_ADOPTANTE":
                sesion.createSession(sesion.getUserId(), sesion.getUserName(),
                        "ADOPTANTE", sesion.getShelterId());
                irAlFeed(ActividadFeedAdoptante.class);
                break;
            default:
                startActivity(new Intent(this, ActividadRegistrarOrganizacion.class));
                break;
        }
    }

    /** Cambio de modo: se limpia la pila para que el botón volver no regrese al modo anterior. */
    private void irAlFeed(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /** Refresca la tarjeta con roles/shelterId frescos de la API (pueden cambiar al habilitar). */
    private void actualizarTarjetaRefugio(UserResponse user) {
        String shelterId = user.shelterId == null ? "" : user.shelterId;
        if (tieneRol(user, "ShelterOwner") && !shelterId.isEmpty()) {
            tarjetaModo = "ADMINISTRAR";
            tarjetaShelterId = shelterId;
        } else if (!shelterId.isEmpty()) {
            tarjetaModo = "PENDIENTE";
            tarjetaShelterId = shelterId;
        } else if (sesion.isRefugio()) {
            tarjetaModo = "MODO_ADOPTANTE";
            tarjetaShelterId = sesion.getShelterId();
        } else {
            tarjetaModo = "REGISTRAR";
            tarjetaShelterId = "";
        }
        pintarTarjetaRefugio();
    }

    private boolean tieneRol(UserResponse user, String rol) {
        if (user.roles == null) return false;
        for (String r : user.roles) {
            if (r != null && r.equalsIgnoreCase(rol)) return true;
        }
        return false;
    }

    private void mostrarDialogoSexo() {
        String[] opciones = {"Masculino", "Femenino"};
        int seleccionado = sexoSeleccionado != null && sexoSeleccionado == 1 ? 1 : 0;
        final int[] temporal = {seleccionado};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Selecciona tu sexo")
                .setSingleChoiceItems(opciones, seleccionado, (dialog, which) -> temporal[0] = which)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    sexoSeleccionado = temporal[0];
                    txtSexoUsuarioInput.setText(opciones[temporal[0]]);
                })
                .show();
    }

    private String normalizarIso(String iso) {
        if (iso == null || iso.trim().isEmpty()) return null;
        String recortado = iso.trim();
        return recortado.length() >= 10 ? recortado.substring(0, 10) : recortado;
    }

    /** Muestra como badges los roles distintos de "User" + el estado de verificación del correo. */
    private void mostrarBadges(UserResponse user) {
        layoutRolesBadges.removeAllViews();
        boolean hayBadges = false;

        if (user.roles != null) {
            for (String rol : user.roles) {
                if (rol == null || rol.trim().isEmpty() || rol.equalsIgnoreCase("User")) continue;
                agregarBadge(etiquetaRol(rol), getColor(R.color.marronPrimario));
                hayBadges = true;
            }
        }

        if (Boolean.TRUE.equals(user.isEmailConfirmed)) {
            agregarBadge("✓ Correo verificado", getColor(R.color.verde_persona));
        } else {
            agregarBadge("⚠ Correo sin verificar", getColor(R.color.naranja_asociacion));
        }
        hayBadges = true;

        layoutRolesBadges.setVisibility(hayBadges ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void agregarBadge(String texto, int colorFondo) {
        TextView badge = new TextView(this);
        badge.setText(texto);
        badge.setTextColor(getColor(R.color.blanco));
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        badge.setTypeface(badge.getTypeface(), Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(R.drawable.bg_badge_pill);
        badge.setBackgroundTintList(ColorStateList.valueOf(colorFondo));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                getResources().getDisplayMetrics());
        params.setMargins(margen, 0, margen, 0);
        badge.setLayoutParams(params);
        layoutRolesBadges.addView(badge);
    }

    private String etiquetaRol(String rol) {
        if (rol.equalsIgnoreCase("ShelterOwner")) return "Dueño de refugio";
        if (rol.equalsIgnoreCase("Admin") || rol.equalsIgnoreCase("Administrator")) return "Administrador";
        return rol;
    }

    /** La API usa 0 = Masculino, 1 = Femenino (ver Gender MALE/FEMALE y registro). */
    private String mapearGenero(Integer gender) {
        if (gender == null) return "No especificado";
        if (gender == 0) return "Masculino";
        if (gender == 1) return "Femenino";
        return "No especificado";
    }

    /** "2000-02-01" -> "1, Feb 2000". Null/vacío -> "No especificada". */
    private String formatearFechaBonita(String iso) {
        java.util.Calendar cal = isoACalendario(iso);
        if (cal == null) return "No especificada";
        return bonito(cal.get(java.util.Calendar.DAY_OF_MONTH),
                cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.YEAR));
    }

    private String bonito(int dia, int mes0, int anio) {
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        return dia + ", " + meses[mes0] + " " + anio;
    }

    private java.util.Calendar isoACalendario(String iso) {
        String normal = normalizarIso(iso);
        if (normal == null) return null;
        try {
            java.text.SimpleDateFormat entrada =
                    new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            entrada.setLenient(false);
            java.util.Date fecha = entrada.parse(normal);
            if (fecha == null) return null;
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(fecha);
            return cal;
        } catch (java.text.ParseException e) {
            return null;
        }
    }

    private void mostrarCalendario() {
        java.util.Calendar actual = isoACalendario(fechaIsoSeleccionada);
        if (actual == null) actual = java.util.Calendar.getInstance();
        com.google.android.material.datepicker.MaterialDatePicker<Long> datePicker =
                com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Elige tu fecha de nacimiento")
                        .setSelection(actual.getTimeInMillis())
                        .setTheme(com.patitasalrescate.R.style.ThemeOverlay_Patitas_CalendarLight)
                        .build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            java.util.Calendar utc =
                    java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            utc.setTimeInMillis(selection);
            int dia = utc.get(java.util.Calendar.DAY_OF_MONTH);
            int mes = utc.get(java.util.Calendar.MONTH);
            int anio = utc.get(java.util.Calendar.YEAR);
            fechaIsoSeleccionada = String.format(java.util.Locale.US,
                    "%04d-%02d-%02d", anio, mes + 1, dia);
            txtNacimientoUsuarioInput.setText(bonito(dia, mes, anio));
            txtNacimientoUsuarioInput.setError(null);
        });
        datePicker.show(getSupportFragmentManager(), "fecha_nacimiento");
    }

    private void configurarLauncherFoto() {
        launcherGaleria = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                Uri uriSeleccionada = result.getData().getData();
                Uri uriLocal = copiarImagenAAlmacenamientoInterno(uriSeleccionada);
                if (uriLocal != null) {
                    uriFotoNueva = uriLocal;
                    Glide.with(this).load(uriFotoNueva).fitCenter().into(imgAvatarUsuario);
                } else {
                    Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Uri copiarImagenAAlmacenamientoInterno(Uri uriOrigen) {
        // Conservar el tipo real: si se guarda un webp como ".jpg", el MIME deducido sale mal.
        String mimeOrigen = getContentResolver().getType(uriOrigen);
        if (mimeOrigen != null && !mimeOrigen.equals("image/jpeg")
                && !mimeOrigen.equals("image/png") && !mimeOrigen.equals("image/webp")) {
            Toast.makeText(this, "Solo se permiten imágenes jpeg, png o webp", Toast.LENGTH_LONG).show();
            return null;
        }
        String extOrigen = mimeOrigen == null ? null
                : android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeOrigen);
        if (extOrigen == null || extOrigen.isEmpty()) extOrigen = "jpg";
        try (InputStream in = getContentResolver().openInputStream(uriOrigen)) {
            if (in == null) return null;

            File carpetaFotos = new File(getFilesDir(), "avatares");
            if (!carpetaFotos.exists()) carpetaFotos.mkdirs();

            File archivoDestino = new File(carpetaFotos, "avatar_" + UUID.randomUUID() + "." + extOrigen);
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
                btnEditarNombres.setEnabled(true);
                btnEditarApellidos.setEnabled(true);
                btnEditarNacimiento.setEnabled(true);
                btnEditarSexo.setEnabled(true);
                UserResponse user = response.body();
                String nombres = user.firstName == null ? "" : user.firstName.trim();
                String apellidos = user.lastName == null ? "" : user.lastName.trim();
                String nombre = (nombres + " " + apellidos).trim();
                txtNombreUsuario.setText(nombre);
                txtNombresUsuarioInput.setText(nombres);
                txtNombresUsuarioInput.setEnabled(false);
                txtApellidosUsuarioInput.setText(apellidos);
                txtApellidosUsuarioInput.setEnabled(false);
                txtCorreoUsuarioInput.setText(user.email == null ? "" : user.email);
                txtNacimientoUsuarioInput.setText(formatearFechaBonita(user.birthDate));
                txtSexoUsuarioInput.setText(mapearGenero(user.gender));
                birthDateIsoOriginal = normalizarIso(user.birthDate);
                fechaIsoSeleccionada = birthDateIsoOriginal;
                genderOriginal = user.gender;
                sexoSeleccionado = user.gender;
                String shelterFresco = user.shelterId == null ? "" : user.shelterId;
                txtTipoUsuario.setText(shelterFresco.isEmpty() ? "Adoptante" : "Refugio");
                mostrarBadges(user);
                actualizarTarjetaRefugio(user);
                if (user.photoUrl != null && !user.photoUrl.isEmpty()) {
                    Glide.with(ActividadPerfilUsuario.this)
                        .load(user.photoUrl)
                        .placeholder(R.drawable.rj_persona)
                        .fitCenter()
                        .into(imgAvatarUsuario);
                } else {
                    imgAvatarUsuario.setImageResource(R.drawable.rj_persona);
                }
            }
            @Override public void onFailure(Call<UserResponse> call, Throwable error) {
                if (!isFinishing()) Toast.makeText(ActividadPerfilUsuario.this, "Sin conexión con perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarCambios() {
        if (!ApiApp.exigirOnline(this)) return;
        String nombres = txtNombresUsuarioInput.getText().toString().trim();
        String apellidos = txtApellidosUsuarioInput.getText().toString().trim();

        if (nombres.isEmpty()) {
            txtNombresUsuarioInput.setError("Los nombres no pueden estar vacíos");
            habilitarCampo(txtNombresUsuarioInput);
            return;
        }
        UpdateUserRequest update = new UpdateUserRequest();
        update.firstName = nombres;
        // Enviar lastName="" el backend lo rechaza (400). Null => FormParts lo omite.
        update.lastName = apellidos.isEmpty() ? null : apellidos;
        // La fecha solo cambia vía calendario (fechaIsoSeleccionada); el campo no se teclea.
        if (fechaIsoSeleccionada != null && !fechaIsoSeleccionada.equals(birthDateIsoOriginal)) {
            update.birthDate = fechaIsoSeleccionada;
        }
        if (sexoSeleccionado != null && !sexoSeleccionado.equals(genderOriginal)) {
            update.gender = sexoSeleccionado;
        }
        try { if (uriFotoNueva != null) update.photo = ApiApp.upload(this, uriFotoNueva); }
        catch (IOException error) { Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show(); return; }
        findViewById(R.id.btnGuardarPerfil).setEnabled(false);
        ApiApp.client().admin.updateCurrentUser(update).enqueue(new Callback<UserResponse>() {
            @Override public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                findViewById(R.id.btnGuardarPerfil).setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ActividadPerfilUsuario.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    txtNombresUsuarioInput.setEnabled(false);
                    txtApellidosUsuarioInput.setEnabled(false);
                    cargarDatosEnVista();
                } else {
                    String cuerpo = "";
                    try {
                        if (response.errorBody() != null) cuerpo = response.errorBody().string();
                    } catch (Exception ignored) { }
                    Log.e(TAG, "PATCH admin/user -> HTTP " + response.code() + " body=" + cuerpo);
                    String detalle = cuerpo.length() > 120 ? cuerpo.substring(0, 120) : cuerpo;
                    Toast.makeText(ActividadPerfilUsuario.this,
                            "Error al guardar (HTTP " + response.code()
                                    + (detalle.isEmpty() ? "" : ": " + detalle) + ")",
                            Toast.LENGTH_LONG).show();
                }

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
