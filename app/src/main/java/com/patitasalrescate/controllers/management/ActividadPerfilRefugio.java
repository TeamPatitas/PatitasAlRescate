package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.feed.ActividadFeedAdoptante;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.ui.AdaptadorEventos;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.ApiPages;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.data.remote.dto.ShelterResponse;
import com.patitasalrescate.data.remote.dto.EventSummaryResponse;
import com.patitasalrescate.data.remote.dto.UpdateShelterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class ActividadPerfilRefugio extends com.patitasalrescate.controllers.base.BaseActivity {

    private static final String TAG = "PerfilRefugio";

    private ImageView imgFoto, imgQR;
    private EditText txtNombre, txtContacto;
    private MaterialButton btnVerMapaRefugio;
    private SwitchMaterial switchMostrarDireccion;
    private TextView txtTituloEventos, txtTituloDonaciones, txtDescDonaciones, txtNoEventos;
    private TextView txtIndicadorMiRefugio;
    private ShelterResponse refugioActual;
    private CardView cardQr;
    private RecyclerView recyclerEventos;
    private Button btnModoAdoptante, btnEditarRefugio;
    private SwipeRefreshLayout swipeRef;
    private String idRefugio;
    private String telefonoCrudo = "";
    private String qrUrlServidor;
    private Double latitudRefugio = null;
    private Double longitudRefugio = null;
    private Boolean mostrarDireccionRefugio = null;
    private boolean esModoRefugio;
    private boolean eventosVisibles = true;
    private boolean editando = false;
    private boolean eventosResueltos = false;
    private final android.os.Handler timeoutHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable timeoutEventosRunnable = () -> {
        if (isFinishing() || isDestroyed() || !eventosVisibles || eventosResueltos) return;
        eventosResueltos = true;
        recyclerEventos.setVisibility(View.GONE);
        txtNoEventos.setVisibility(View.VISIBLE);
        txtNoEventos.setText("No hay eventos disponibles");
    };
    // QR elegido pendiente de subir: solo se envía al presionar GUARDAR CAMBIOS.
    private Uri qrPendiente;
    private ActivityResultLauncher<Intent> launcherQr;
    private ActivityResultLauncher<Intent> launcherUbicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_perfil_refugio);

        idRefugio = getIntent().getStringExtra("id_refugio_key");
        esModoRefugio = PatitasSessionManager.getInstance(this).isRefugio();

        initViews();
        configToolbar();
        configurarBotones();

        if (esModoRefugio) {
            // El shelterId de sesión puede estar desactualizado: se usa el fresco del usuario.
            resolverShelterPropio();
            return;
        }
        if (idRefugio == null || idRefugio.isEmpty()) {
            Toast.makeText(this, "Error al cargar refugio", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mostrarCargando();
        cargarDatosRefugio();
    }

    private void mostrarCargando() {
        // Estado de carga hasta que responda la API (eventos solo en modo usuario).
        txtNombre.setText("Cargando refugio…");
        if (eventosVisibles) {
            recyclerEventos.setVisibility(View.GONE);
            txtNoEventos.setVisibility(View.VISIBLE);
            eventosResueltos = false;
            txtNoEventos.setText("Cargando eventos…");
            timeoutHandler.removeCallbacks(timeoutEventosRunnable);
            timeoutHandler.postDelayed(timeoutEventosRunnable, 5000L);
        }
    }

    private void resolverShelterPropio() {
        mostrarCargando();
        ApiApp.client().admin.getCurrentUser().enqueue(new Callback<com.patitasalrescate.data.remote.dto.UserResponse>() {
            @Override public void onResponse(Call<com.patitasalrescate.data.remote.dto.UserResponse> call,
                    Response<com.patitasalrescate.data.remote.dto.UserResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (swipeRef != null) swipeRef.setRefreshing(false);
                String fresco = response.isSuccessful() && response.body() != null
                        ? response.body().shelterId : null;
                if (fresco == null || fresco.isEmpty()) {
                    Toast.makeText(ActividadPerfilRefugio.this,
                            "No se pudo determinar tu refugio", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                idRefugio = fresco;
                PatitasSessionManager.getInstance(ActividadPerfilRefugio.this).setShelterId(fresco);
                Log.d(TAG, "shelter propio resuelto id=" + fresco);
                cargarDatosRefugio();
            }
            @Override public void onFailure(Call<com.patitasalrescate.data.remote.dto.UserResponse> call, Throwable error) {
                if (isFinishing() || isDestroyed()) return;
                if (swipeRef != null) swipeRef.setRefreshing(false);
                Log.e(TAG, "no se pudo resolver el refugio propio: " + error, error);
                Toast.makeText(ActividadPerfilRefugio.this, "Sin conexión al cargar tu refugio",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void initViews() {
        imgFoto = findViewById(R.id.img_foto_refugio_perfil);
        imgQR = findViewById(R.id.img_qr_donacion);
        txtNombre = findViewById(R.id.txt_nombre_refugio_perfil);
        txtIndicadorMiRefugio = findViewById(R.id.txt_indicador_mi_refugio);
        btnVerMapaRefugio = findViewById(R.id.btn_ver_mapa_refugio);
        switchMostrarDireccion = findViewById(R.id.switch_mostrar_direccion_refugio);
        txtContacto = findViewById(R.id.txt_contacto_refugio_perfil);
        txtTituloEventos = findViewById(R.id.txt_titulo_eventos);
        txtTituloDonaciones = findViewById(R.id.txt_titulo_donaciones);
        txtDescDonaciones = findViewById(R.id.txt_desc_donaciones);
        cardQr = findViewById(R.id.card_qr_donacion);
        txtNoEventos = findViewById(R.id.txt_no_eventos);
        recyclerEventos = findViewById(R.id.recycler_eventos_refugio);
        btnModoAdoptante = findViewById(R.id.btnCambiarModoAdoptante);
        btnEditarRefugio = findViewById(R.id.btn_editar_refugio);
        swipeRef = findViewById(R.id.swipePerfilRefugio);

        txtNombre.setTextColor(getColor(R.color.texto_titulo_sobre_crema));
        txtNombre.setHintTextColor(getColor(R.color.textoSecundario));
        txtNombre.setMaxLines(2);
        txtNombre.setHorizontallyScrolling(false);
        txtNombre.setEllipsize(TextUtils.TruncateAt.END);
        txtContacto.setTextColor(getColor(R.color.verde_persona));
        txtContacto.setHintTextColor(getColor(R.color.textoSecundario));
        if (switchMostrarDireccion != null) {
            switchMostrarDireccion.setTextColor(getColor(R.color.texto_titulo_sobre_crema));
            switchMostrarDireccion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed() || editando) {
                    mostrarDireccionRefugio = isChecked;
                    if (idRefugio != null) {
                        getSharedPreferences("PatitasShelterConfig", MODE_PRIVATE).edit()
                                .putBoolean("show_address_" + idRefugio, isChecked)
                                .apply();
                    }
                }
            });
        }

        if (swipeRef != null) {
            swipeRef.setColorSchemeColors(getColor(R.color.marronPrimario), getColor(R.color.naranja_asociacion));
            swipeRef.setOnRefreshListener(() -> {
                com.patitasalrescate.utils.ApiCache.invalidateAll(ActividadPerfilRefugio.this);
                cargarDatosRefugio();
            });
        }

        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));

        launcherUbicacion = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && !isFinishing()) {
                        latitudRefugio = result.getData().getDoubleExtra(SelectLocation.EXTRA_LATITUD, -7.1617);
                        longitudRefugio = result.getData().getDoubleExtra(SelectLocation.EXTRA_LONGITUD, -78.5128);
                        actualizarBotonMapa();
                        Toast.makeText(this, "Ubicación seleccionada en el mapa", Toast.LENGTH_SHORT).show();
                    }
                });

        btnVerMapaRefugio.setOnClickListener(v -> {
            if (esModoRefugio && editando) {
                Intent intent = new Intent(this, SelectLocation.class);
                if (latitudRefugio != null && longitudRefugio != null) {
                    intent.putExtra(SelectLocation.EXTRA_LATITUD, latitudRefugio);
                    intent.putExtra(SelectLocation.EXTRA_LONGITUD, longitudRefugio);
                }
                launcherUbicacion.launch(intent);
                return;
            }
            if (latitudRefugio == null || longitudRefugio == null) {
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            String nombre = txtNombre.getText().toString().trim();
            Uri uri = Uri.parse("geo:" + latitudRefugio + "," + longitudRefugio + "?q=" + latitudRefugio + "," + longitudRefugio + "(" + Uri.encode(nombre) + ")");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(intent);
            } catch (Exception e) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + latitudRefugio + "," + longitudRefugio)));
                } catch (Exception ex) {
                    Toast.makeText(this, "No hay aplicación de mapas instalada", Toast.LENGTH_SHORT).show();
                }
            }
        });

        launcherQr = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null
                    && result.getData().getData() != null && !isFinishing()) {
                Uri uri = result.getData().getData();
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
                Uri qrInterno = copiarQrAAlmacenamientoInterno(uri, mime);
                if (qrInterno == null) {
                    Toast.makeText(this, "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show();
                    return;
                }
                getSharedPreferences("PatitasQR", MODE_PRIVATE).edit()
                        .putString("qr_" + idRefugio, qrInterno.toString()).apply();
                // Solo vista previa: el endpoint se llama al presionar GUARDAR CAMBIOS.
                qrPendiente = qrInterno;
                Log.d(TAG, "QR pendiente para " + idRefugio + " uri=" + qrInterno);
                actualizarSeccionQr();

                if (esModoRefugio && !editando) {
                    editando = true;
                    habilitarCamposRefugio(true);
                }
                pintarBotonEditar();
                Toast.makeText(this, "Imagen cambiada", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean esDuenioDelRefugio() {
        if (esModoRefugio) return true;
        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        String miShelterId = session.getShelterId();
        if (idRefugio != null && !idRefugio.isEmpty() && idRefugio.equals(miShelterId)) return true;
        if (refugioActual != null && refugioActual.owners != null) {
            String miUserId = session.getUserId();
            if (miUserId != null && !miUserId.isEmpty() && refugioActual.owners.contains(miUserId)) {
                return true;
            }
        }
        return false;
    }

    private void actualizarBotonMapa() {
        boolean tieneCoordenadas = (latitudRefugio != null && longitudRefugio != null
                && !(latitudRefugio == 0.0 && longitudRefugio == 0.0));
        boolean esDuenio = esDuenioDelRefugio();
        if (esModoRefugio) {
            if (editando) {
                btnVerMapaRefugio.setVisibility(View.VISIBLE);
                btnVerMapaRefugio.setText(tieneCoordenadas ? "Cambiar ubicación en mapa" : "Seleccionar ubicación en mapa");
            } else {
                btnVerMapaRefugio.setVisibility(tieneCoordenadas ? View.VISIBLE : View.GONE);
                btnVerMapaRefugio.setText("Ver ubicación en el mapa");
            }
        } else {
            // Modo adoptante: solo mostrar si tiene coordenadas y (showAddress es true o el usuario es dueño)
            boolean visible = tieneCoordenadas && (Boolean.TRUE.equals(mostrarDireccionRefugio) || esDuenio);
            btnVerMapaRefugio.setVisibility(visible ? View.VISIBLE : View.GONE);
            btnVerMapaRefugio.setText("Ver ubicación en el mapa");
        }
    }

    /** Copia el QR a almacenamiento interno (permiso permanente, sobrevive reinicios). */
    private Uri copiarQrAAlmacenamientoInterno(Uri uriOrigen, String mime) {
        try (java.io.InputStream in = getContentResolver().openInputStream(uriOrigen)) {
            if (in == null) return null;
            String ext = "jpg";
            if ("image/png".equalsIgnoreCase(mime)) ext = "png";
            else if ("image/webp".equalsIgnoreCase(mime)) ext = "webp";
            java.io.File carpeta = new java.io.File(getFilesDir(), "qr");
            if (!carpeta.exists()) carpeta.mkdirs();
            java.io.File destino = new java.io.File(carpeta, "qr_" + idRefugio + "." + ext);
            try (java.io.OutputStream out = new java.io.FileOutputStream(destino)) {
                byte[] buffer = new byte[8192];
                int leido;
                while ((leido = in.read(buffer)) != -1) out.write(buffer, 0, leido);
            }
            return Uri.fromFile(destino);
        } catch (Exception e) {
            Log.e(TAG, "no se pudo copiar el QR: " + e.getMessage());
            return null;
        }
    }

    private void configurarBotones() {
        // En modo usuario ambos botones inferiores están ocultos: se esconde la barra completa.
        findViewById(R.id.ly_botones_refugio).setVisibility(
                esModoRefugio ? View.VISIBLE : View.GONE);
        // En modo refugio no hay sección de eventos; en modo usuario no hay cambio de modo.
        eventosVisibles = !esModoRefugio;
        if (esModoRefugio) {
            txtTituloEventos.setVisibility(View.GONE);
            recyclerEventos.setVisibility(View.GONE);
            txtNoEventos.setVisibility(View.GONE);
            switchMostrarDireccion.setVisibility(View.VISIBLE);
        } else {
            btnModoAdoptante.setVisibility(View.GONE);
            switchMostrarDireccion.setVisibility(View.GONE);
        }
        btnModoAdoptante.setOnClickListener(v -> {
            PatitasSessionManager sesion = PatitasSessionManager.getInstance(this);
            sesion.createSession(sesion.getUserId(), sesion.getUserName(),
                    "ADOPTANTE", sesion.getShelterId());
            Intent i = new Intent(this, ActividadFeedAdoptante.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        findViewById(R.id.btnCerrarSesionRefugio).setOnClickListener(v -> ApiApp.cerrarSesion(this));

        // Edición solo en modo refugio (dueño).
        btnEditarRefugio.setVisibility(esModoRefugio ? View.VISIBLE : View.GONE);
        pintarBotonEditar();
        btnEditarRefugio.setOnClickListener(v -> {
            if (!editando) {
                editando = true;
                txtContacto.setVisibility(View.VISIBLE);
                if (txtContacto.getParent() != null && txtContacto.getParent().getParent() instanceof View) {
                    ((View) txtContacto.getParent().getParent()).setVisibility(View.VISIBLE);
                }
                txtContacto.setText(telefonoCrudo);
                habilitarCamposRefugio(true);
                pintarBotonEditar();
            } else {
                guardarRefugio();
            }
        });

        cardQr.setOnClickListener(v -> {
            if (esModoRefugio) elegirQr();
        });
        actualizarSeccionQr();
    }

    /** Naranja en reposo, verde en modo guardado. */
    private void pintarBotonEditar() {
        if (editando) {
            btnEditarRefugio.setText("GUARDAR CAMBIOS");
            btnEditarRefugio.setBackgroundTintList(ColorStateList.valueOf(
                    getColor(android.R.color.holo_green_dark)));
        } else {
            btnEditarRefugio.setText("EDITAR REFUGIO");
            btnEditarRefugio.setBackgroundTintList(ColorStateList.valueOf(
                    getColor(R.color.naranja_asociacion)));
        }
    }

    private void habilitarCamposRefugio(boolean habilitar) {
        int fondo = habilitar ? R.drawable.bg_input_rounded : android.R.color.transparent;
        int padH = habilitar ? 24 : 0;
        int padV = habilitar ? 14 : 0;
        txtNombre.setEnabled(habilitar);
        txtNombre.setBackgroundResource(fondo);
        txtNombre.setPadding(padH, padV, padH, padV);
        txtNombre.setTextColor(getColor(habilitar ? R.color.texto_busqueda : R.color.texto_titulo_sobre_crema));
        txtNombre.setHintTextColor(getColor(R.color.textoSecundario));
        txtNombre.setMaxLines(2);
        txtNombre.setHorizontallyScrolling(false);
        txtNombre.setEllipsize(habilitar ? null : TextUtils.TruncateAt.END);
        txtContacto.setEnabled(habilitar);
        txtContacto.setBackgroundResource(fondo);
        txtContacto.setPadding(padH, padV, padH, padV);
        txtContacto.setTextColor(getColor(habilitar ? R.color.texto_busqueda : R.color.verde_persona));
        txtContacto.setHintTextColor(getColor(R.color.textoSecundario));
        if (switchMostrarDireccion != null) {
            switchMostrarDireccion.setEnabled(habilitar && esModoRefugio);
        }
        actualizarBotonMapa();
        if (habilitar) txtNombre.requestFocus();
    }

    private void guardarRefugio() {
        if (!ApiApp.exigirOnline(this)) return;
        String nombre = txtNombre.getText().toString().trim();
        String telefono = txtContacto.getText().toString().trim();
        if (nombre.isEmpty()) {
            txtNombre.setError("Ingresa un nombre");
            return;
        }
        boolean mostrarDirSeleccionado = switchMostrarDireccion != null && switchMostrarDireccion.isChecked();
        mostrarDireccionRefugio = mostrarDirSeleccionado;
        if (idRefugio != null) {
            getSharedPreferences("PatitasShelterConfig", MODE_PRIVATE).edit()
                    .putBoolean("show_address_" + idRefugio, mostrarDirSeleccionado)
                    .apply();
        }

        UpdateShelterRequest update = new UpdateShelterRequest();
        update.name = nombre;
        update.phoneNumber = telefono;
        update.latitude = latitudRefugio;
        update.longitude = longitudRefugio;
        update.showAddress = mostrarDirSeleccionado;

        // El QR elegido va adjunto en este mismo PATCH (parte "yapeQrImage").
        if (qrPendiente != null) {
            try {
                update.yapeQrImage = ApiApp.upload(this, qrPendiente);
                long largo = -1;
                try { largo = update.yapeQrImage.content.contentLength(); } catch (Exception ignored) { }
                Log.d(TAG, "PATCH shelter/" + idRefugio + " yapeQrImage: filename="
                        + update.yapeQrImage.filename + " contentType="
                        + update.yapeQrImage.content.contentType() + " bytes=" + largo);
            } catch (java.io.IOException e) {
                Log.e(TAG, "QR no se pudo leer: " + e.getMessage());
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            Log.d(TAG, "PATCH shelter/" + idRefugio + " sin cambios de QR");
        }
        btnEditarRefugio.setEnabled(false);
        ApiApp.client().shelters.updateShelter(idRefugio, update).enqueue(new Callback<ShelterResponse>() {
            @Override public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (swipeRef != null) swipeRef.setRefreshing(false);
                btnEditarRefugio.setEnabled(true);
                if (response.isSuccessful()) {
                    Log.d(TAG, "refugio actualizado HTTP " + response.code());
                    com.patitasalrescate.utils.ApiCache.invalidateAll(ActividadPerfilRefugio.this);
                    qrPendiente = null;
                    editando = false;
                    habilitarCamposRefugio(false);
                    pintarBotonEditar();
                    Toast.makeText(ActividadPerfilRefugio.this, "Refugio actualizado", Toast.LENGTH_SHORT).show();
                    if (response.body() != null) {
                        ShelterResponse rBody = response.body();
                        if (extraerShowAddress(rBody) == null) {
                            rBody.showAddress = mostrarDirSeleccionado;
                        }
                        aplicarDatosRefugio(rBody);
                    } else {
                        cargarDatosRefugio();
                    }
                } else {
                    String cuerpo = "";
                    try {
                        if (response.errorBody() != null) cuerpo = response.errorBody().string();
                    } catch (Exception ignored) { }
                    Log.e(TAG, "update shelter -> HTTP " + response.code() + " body=" + cuerpo);
                    String detalle = cuerpo.length() > 120 ? cuerpo.substring(0, 120) : cuerpo;
                    Toast.makeText(ActividadPerfilRefugio.this,
                            "Error al guardar (HTTP " + response.code()
                                    + (detalle.isEmpty() ? "" : ": " + detalle) + ")",
                            Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<ShelterResponse> call, Throwable error) {
                if (isFinishing() || isDestroyed()) return;
                if (swipeRef != null) swipeRef.setRefreshing(false);
                btnEditarRefugio.setEnabled(true);
                Log.e(TAG, "update shelter onFailure: " + error, error);
                Toast.makeText(ActividadPerfilRefugio.this, "Sin conexión al guardar", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean uriLegible(String texto) {
        if (texto == null || texto.isEmpty()) return false;
        try {
            Uri u = Uri.parse(texto);
            if ("file".equals(u.getScheme())) {
                return u.getPath() != null && new java.io.File(u.getPath()).isFile();
            }
            try (java.io.InputStream in = getContentResolver().openInputStream(u)) {
                return in != null;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void actualizarSeccionQr() {
        String qrServidor = qrUrlServidor;
        String qrLocal = getSharedPreferences("PatitasQR", MODE_PRIVATE)
                .getString("qr_" + idRefugio, null);
        if (qrLocal != null && !uriLegible(qrLocal)) {
            Log.d(TAG, "QR local ilegible, se descarta: " + qrLocal);
            getSharedPreferences("PatitasQR", MODE_PRIVATE).edit()
                    .remove("qr_" + idRefugio).apply();
            qrLocal = null;
        }
        String qrGuardado = qrServidor != null ? qrServidor : qrLocal;
        boolean hayQr = qrGuardado != null && !qrGuardado.isEmpty();
        Log.d(TAG, "seccionQR modoRefugio=" + esModoRefugio + " hayQr=" + hayQr
                + " id=" + idRefugio + " delServidor=" + (qrServidor != null));
        if (!esModoRefugio) {
            int vis = hayQr ? View.VISIBLE : View.GONE;
            txtTituloDonaciones.setVisibility(vis);
            txtDescDonaciones.setVisibility(vis);
            cardQr.setVisibility(vis);
            if (txtTituloDonaciones.getParent() != null && txtTituloDonaciones.getParent().getParent() instanceof View) {
                ((View) txtTituloDonaciones.getParent().getParent()).setVisibility(vis);
            }
            if (!hayQr) return;
        } else {
            txtTituloDonaciones.setVisibility(View.VISIBLE);
            txtDescDonaciones.setVisibility(View.VISIBLE);
            cardQr.setVisibility(View.VISIBLE);
        }
        if (hayQr) {
            txtDescDonaciones.setText(esModoRefugio
                    ? "Tu QR de donaciones (Yape). Tócalo para cambiarlo."
                    : "Apóyanos escaneando el siguiente código QR para Yape:");
            if (qrServidor != null) {
                Glide.with(this).load(qrServidor).fitCenter().into(imgQR);
            } else {
                Glide.with(this).load(Uri.parse(qrGuardado)).fitCenter().into(imgQR);
            }
        } else {
            txtDescDonaciones.setText("Agregar QR de donaciones");
            imgQR.setImageResource(android.R.drawable.ic_menu_edit);
        }
    }

    private void elegirQr() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png", "image/webp"});
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        launcherQr.launch(intent);
    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarPerfilRefugio);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del Refugio");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void cargarDatosRefugio() {
        com.patitasalrescate.utils.ApiCache.invalidateAll(ActividadPerfilRefugio.this);
        ApiApp.client().shelters.getShelterById(idRefugio).enqueue(new Callback<ShelterResponse>() {
            @Override public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (swipeRef != null) swipeRef.setRefreshing(false);
                Log.d(TAG, "GET shelter id=" + idRefugio + " HTTP " + response.code()
                        + " traeQr=" + (response.body() != null && response.body().yapeQrCode != null
                        && !response.body().yapeQrCode.isEmpty()));
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ActividadPerfilRefugio.this, "No se pudo cargar el refugio", Toast.LENGTH_SHORT).show();
                    return;
                }
                aplicarDatosRefugio(response.body());
            }
            @Override public void onFailure(Call<ShelterResponse> call, Throwable error) {
                if (!isFinishing()) {
                    if (swipeRef != null) swipeRef.setRefreshing(false);
                    Toast.makeText(ActividadPerfilRefugio.this, "Sin conexión con refugios", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Boolean extraerShowAddress(ShelterResponse body) {
        if (body == null) return null;
        if (body.showAddress != null) return body.showAddress;
        if (body.location != null && body.location.showAddress != null) {
            return body.location.showAddress;
        }
        return null;
    }

    private void aplicarDatosRefugio(ShelterResponse body) {
        if (body == null) return;
        refugioActual = body;

        qrUrlServidor = (body.yapeQrCode == null || body.yapeQrCode.isEmpty())
                ? null : body.yapeQrCode;
        actualizarSeccionQr();

        latitudRefugio = null;
        longitudRefugio = null;
        if (body.location != null) {
            latitudRefugio = body.location.latitude;
            longitudRefugio = body.location.longitude;
        }
        if (latitudRefugio == null) latitudRefugio = body.latitude;
        if (longitudRefugio == null) longitudRefugio = body.longitude;

        Boolean sa = extraerShowAddress(body);
        if (sa != null) {
            mostrarDireccionRefugio = sa;
            if (idRefugio != null) {
                getSharedPreferences("PatitasShelterConfig", MODE_PRIVATE).edit()
                        .putBoolean("show_address_" + idRefugio, sa)
                        .apply();
            }
        } else if (idRefugio != null && getSharedPreferences("PatitasShelterConfig", MODE_PRIVATE).contains("show_address_" + idRefugio)) {
            mostrarDireccionRefugio = getSharedPreferences("PatitasShelterConfig", MODE_PRIVATE)
                    .getBoolean("show_address_" + idRefugio, false);
        } else {
            mostrarDireccionRefugio = Boolean.TRUE.equals(body.showAddress);
        }

        boolean esDuenio = esDuenioDelRefugio();

        if (esModoRefugio) {
            switchMostrarDireccion.setVisibility(View.VISIBLE);
            switchMostrarDireccion.setChecked(Boolean.TRUE.equals(mostrarDireccionRefugio));
            switchMostrarDireccion.setEnabled(editando);
            if (txtIndicadorMiRefugio != null) txtIndicadorMiRefugio.setVisibility(View.GONE);
        } else {
            switchMostrarDireccion.setVisibility(View.GONE);
            if (txtIndicadorMiRefugio != null) {
                txtIndicadorMiRefugio.setVisibility(esDuenio ? View.VISIBLE : View.GONE);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(esDuenio ? "Tu Refugio" : null);
            }
        }
        actualizarBotonMapa();

        Refugio refugio = ApiApp.shelter(body);
        txtNombre.setText(refugio.getNombre() == null || refugio.getNombre().isEmpty()
                ? "Refugio" : refugio.getNombre());
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(txtNombre.getText());

        telefonoCrudo = refugio.getNumCelular() == null ? "" : refugio.getNumCelular();
        if (!telefonoCrudo.isEmpty()) {
            txtContacto.setVisibility(View.VISIBLE);
            if (txtContacto.getParent() != null && txtContacto.getParent().getParent() instanceof View) {
                ((View) txtContacto.getParent().getParent()).setVisibility(View.VISIBLE);
            }
            txtContacto.setText("Contacto: " + telefonoCrudo);
        } else {
            // Sin teléfono: ocultar la fila en vez de mostrar "no disponible".
            txtContacto.setVisibility(View.GONE);
            if (!editando && txtContacto.getParent() != null && txtContacto.getParent().getParent() instanceof View) {
                ((View) txtContacto.getParent().getParent()).setVisibility(View.GONE);
            }
        }

        if (refugio.getFotoUrl() != null && !refugio.getFotoUrl().isEmpty()) {
            Glide.with(ActividadPerfilRefugio.this).load(refugio.getFotoUrl())
                    .placeholder(R.drawable.img_default_refugio)
                    .error(R.drawable.img_default_refugio)
                    .centerCrop().into(imgFoto);
        } else {
            imgFoto.setImageResource(R.drawable.img_default_refugio);
        }

        if (eventosVisibles) cargarEventos();
    }

    private void mostrarEventos(List<Evento> eventos) {
        eventosResueltos = true;
        timeoutHandler.removeCallbacks(timeoutEventosRunnable);
        if (eventos == null || eventos.isEmpty()) {
            txtNoEventos.setText("No hay eventos próximos disponibles");
            txtNoEventos.setVisibility(View.VISIBLE);
            recyclerEventos.setVisibility(View.GONE);
        } else {
            txtNoEventos.setVisibility(View.GONE);
            recyclerEventos.setVisibility(View.VISIBLE);
            AdaptadorEventos adaptador = new AdaptadorEventos(eventos, this);
            recyclerEventos.setAdapter(adaptador);
        }
    }

    private void cargarEventos() {
        eventosResueltos = false;
        timeoutHandler.removeCallbacks(timeoutEventosRunnable);
        timeoutHandler.postDelayed(timeoutEventosRunnable, 5000L);
        ApiPages.load(page -> ApiApp.client().events.getAllEvents(page, 50),
                data -> data.totalPages, data -> data.items,
                items -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (swipeRef != null) swipeRef.setRefreshing(false);
                    List<Evento> eventos = new ArrayList<>();
                    for (EventSummaryResponse item : items)
                        // Del refugio VISITADO (idRefugio), no del logueado: isYours no sirve aquí.
                        if (item != null && !Boolean.FALSE.equals(item.isActive)
                                && idRefugio.equals(item.shelterId)
                                && !ApiApp.esPasado(item.eventDate)) {
                            eventos.add(ApiApp.event(item));
                        }
                    mostrarEventos(eventos);
                }, error -> {
                    if (!isFinishing() && !eventosResueltos) {
                        eventosResueltos = true;
                        timeoutHandler.removeCallbacks(timeoutEventosRunnable);
                        recyclerEventos.setVisibility(View.GONE);
                        txtNoEventos.setVisibility(View.VISIBLE);
                        txtNoEventos.setText("No se pudieron cargar los eventos");
                    }
                });
    }
}
