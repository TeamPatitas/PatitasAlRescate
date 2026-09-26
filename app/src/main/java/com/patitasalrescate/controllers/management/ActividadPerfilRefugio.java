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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
    private EditText txtNombre, txtDireccion, txtContacto;
    private TextView txtEstado, txtTituloEventos, txtTituloDonaciones, txtDescDonaciones, txtNoEventos;
    private CardView cardQr;
    private RecyclerView recyclerEventos;
    private Button btnModoAdoptante, btnEditarRefugio;
    private String idRefugio;
    private String telefonoCrudo = "";
    private String qrUrlServidor;
    private boolean esModoRefugio;
    private boolean eventosVisibles = true;
    private boolean editando = false;
    // QR elegido pendiente de subir: solo se envía al presionar GUARDAR CAMBIOS.
    private Uri qrPendiente;
    private ActivityResultLauncher<Intent> launcherQr;

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
        txtEstado.setVisibility(View.GONE);
        if (eventosVisibles) {
            recyclerEventos.setVisibility(View.GONE);
            txtNoEventos.setVisibility(View.VISIBLE);
            txtNoEventos.setText("Cargando eventos…");
        }
    }

    private void resolverShelterPropio() {
        mostrarCargando();
        ApiApp.client().admin.getCurrentUser().enqueue(new Callback<com.patitasalrescate.data.remote.dto.UserResponse>() {
            @Override public void onResponse(Call<com.patitasalrescate.data.remote.dto.UserResponse> call,
                    Response<com.patitasalrescate.data.remote.dto.UserResponse> response) {
                if (isFinishing() || isDestroyed()) return;
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
        txtEstado = findViewById(R.id.txt_estado_refugio);
        txtDireccion = findViewById(R.id.txt_direccion_refugio_perfil);
        txtContacto = findViewById(R.id.txt_contacto_refugio_perfil);
        txtTituloEventos = findViewById(R.id.txt_titulo_eventos);
        txtTituloDonaciones = findViewById(R.id.txt_titulo_donaciones);
        txtDescDonaciones = findViewById(R.id.txt_desc_donaciones);
        cardQr = findViewById(R.id.card_qr_donacion);
        txtNoEventos = findViewById(R.id.txt_no_eventos);
        recyclerEventos = findViewById(R.id.recycler_eventos_refugio);
        btnModoAdoptante = findViewById(R.id.btnCambiarModoAdoptante);
        btnEditarRefugio = findViewById(R.id.btn_editar_refugio);

        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));

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
                // Se usa la copia interna (siempre legible): el permiso de la Uri original
                // puede perderse y dejaba la imagen por defecto.
                qrPendiente = qrInterno;
                Log.d(TAG, "QR pendiente para " + idRefugio + " uri=" + qrInterno);
                actualizarSeccionQr();
                // Al agregar imagen se entra en modo guardado (botón verde).
                if (esModoRefugio && !editando) {
                    editando = true;
                    habilitarCamposRefugio(true);
                }
                pintarBotonEditar();
                Toast.makeText(this, "Imagen cambiada", Toast.LENGTH_SHORT).show();
            }
        });
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
        } else {
            btnModoAdoptante.setVisibility(View.GONE);
        }
        btnModoAdoptante.setOnClickListener(v -> {
            PatitasSessionManager sesion = PatitasSessionManager.getInstance(this);
            sesion.createSession(sesion.getUserId(), sesion.getUserName(),
                    "ADOPTANTE", sesion.getShelterId());
            Intent i = new Intent(this, ActividadFeedAdoptante.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        // Edición solo en modo refugio (dueño).
        btnEditarRefugio.setVisibility(esModoRefugio ? View.VISIBLE : View.GONE);
        pintarBotonEditar();
        btnEditarRefugio.setOnClickListener(v -> {
            if (!editando) {
                editando = true;
                txtContacto.setVisibility(View.VISIBLE);
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
        int fondo = habilitar ? android.R.drawable.edit_text : android.R.color.transparent;
        txtNombre.setEnabled(habilitar);
        txtNombre.setBackgroundResource(fondo);
        txtDireccion.setEnabled(habilitar);
        txtDireccion.setBackgroundResource(fondo);
        txtContacto.setEnabled(habilitar);
        txtContacto.setBackgroundResource(fondo);
        if (habilitar) txtNombre.requestFocus();
    }

    private void guardarRefugio() {
        if (!ApiApp.exigirOnline(this)) return;
        String nombre = txtNombre.getText().toString().trim();
        String direccion = txtDireccion.getText().toString().trim();
        String telefono = txtContacto.getText().toString().trim();
        if (nombre.isEmpty()) {
            txtNombre.setError("Ingresa un nombre");
            return;
        }
        UpdateShelterRequest update = new UpdateShelterRequest();
        // La API exige los demás parámetros con su valor actual aunque no hayan cambiado.
        update.name = nombre;
        update.address = direccion;
        update.phoneNumber = telefono;
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
                btnEditarRefugio.setEnabled(true);
                if (response.isSuccessful()) {
                    Log.d(TAG, "refugio actualizado HTTP " + response.code());
                    qrPendiente = null;
                    editando = false;
                    habilitarCamposRefugio(false);
                    pintarBotonEditar();
                    Toast.makeText(ActividadPerfilRefugio.this, "Refugio actualizado", Toast.LENGTH_SHORT).show();
                    cargarDatosRefugio();
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
                btnEditarRefugio.setEnabled(true);
                Log.e(TAG, "update shelter onFailure: " + error, error);
                Toast.makeText(ActividadPerfilRefugio.this, "Sin conexión al guardar", Toast.LENGTH_LONG).show();
            }
        });
    }

    /** QR: el servidor (yapeQrCode) manda; si no hay, vale el local. Sin QR no se muestra nada
     * en adoptante; en refugio sale lápiz + texto para agregar. */
    /** Comprueba que una Uri local aún se pueda leer (el permiso puede haberse perdido).
     * Los file:// propios se verifican por File directo, sin ContentResolver. */
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
        ApiApp.client().shelters.getShelterById(idRefugio).enqueue(new Callback<ShelterResponse>() {
            @Override public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                Log.d(TAG, "GET shelter id=" + idRefugio + " HTTP " + response.code()
                        + " traeQr=" + (response.body() != null && response.body().yapeQrCode != null
                        && !response.body().yapeQrCode.isEmpty()));
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ActividadPerfilRefugio.this, "No se pudo cargar el refugio", Toast.LENGTH_SHORT).show();
                    return;
                }
                ShelterResponse body = response.body();
                qrUrlServidor = (body.yapeQrCode == null || body.yapeQrCode.isEmpty())
                        ? null : body.yapeQrCode;
                actualizarSeccionQr();
                Refugio refugio = ApiApp.shelter(body);
                txtNombre.setText(refugio.getNombre() == null || refugio.getNombre().isEmpty()
                        ? "Refugio" : refugio.getNombre());
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(txtNombre.getText());
                txtDireccion.setText(refugio.getDireccion() == null || refugio.getDireccion().isEmpty()
                        ? "Dirección no disponible" : refugio.getDireccion());
                telefonoCrudo = refugio.getNumCelular() == null ? "" : refugio.getNumCelular();
                if (!telefonoCrudo.isEmpty()) {
                    txtContacto.setVisibility(View.VISIBLE);
                    txtContacto.setText("Contacto: " + telefonoCrudo);
                } else {
                    // Sin teléfono: ocultar la fila en vez de mostrar "no disponible".
                    txtContacto.setVisibility(View.GONE);
                }

                // Badge de estado según habilitación del refugio.
                if (Boolean.TRUE.equals(body.isAvailable)) {
                    txtEstado.setVisibility(View.VISIBLE);
                    txtEstado.setText("✓ Habilitado");
                    txtEstado.setBackgroundTintList(
                            ColorStateList.valueOf(getColor(R.color.verde_persona)));
                } else {
                    txtEstado.setVisibility(View.VISIBLE);
                    txtEstado.setText("◷ En revisión");
                    txtEstado.setBackgroundTintList(
                            ColorStateList.valueOf(getColor(R.color.naranja_asociacion)));
                }

                if (refugio.getFotoUrl() != null && !refugio.getFotoUrl().isEmpty()) {
                    Glide.with(ActividadPerfilRefugio.this).load(refugio.getFotoUrl())
                            .placeholder(R.drawable.img_default_refugio)
                            .error(R.drawable.img_default_refugio)
                            .fitCenter().into(imgFoto);
                } else {
                    imgFoto.setImageResource(R.drawable.img_default_refugio);
                }

                if (eventosVisibles) cargarEventos();

                // Imagen QR por defecto (Yape)
                imgQR.setImageResource(R.drawable.img_yape_default);
            }
            @Override public void onFailure(Call<ShelterResponse> call, Throwable error) {
                if (!isFinishing()) Toast.makeText(ActividadPerfilRefugio.this, "Sin conexión con refugios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarEventos(List<Evento> eventos) {
        if (eventos.isEmpty()) {
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
        ApiPages.load(page -> ApiApp.client().events.getAllEvents(page, 50),
                data -> data.totalPages, data -> data.items,
                items -> {
                    if (isFinishing() || isDestroyed()) return;
                    List<Evento> eventos = new ArrayList<>();
                    for (EventSummaryResponse item : items)
                        if (idRefugio.equals(item.shelterId)) eventos.add(ApiApp.event(item));
                    mostrarEventos(eventos);
                }, error -> {
                    if (!isFinishing()) txtNoEventos.setText("No se pudieron cargar los eventos");
                });
    }
}
