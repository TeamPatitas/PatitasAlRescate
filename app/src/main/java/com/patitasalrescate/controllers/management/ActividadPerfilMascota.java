package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.FormResponse;
import com.patitasalrescate.data.remote.dto.FormResponsePagedResponse;
import com.patitasalrescate.data.remote.dto.Gender;
import com.patitasalrescate.data.remote.dto.PetResponse;
import com.patitasalrescate.data.remote.dto.ShelterResponse;
import com.patitasalrescate.data.remote.dto.Species;
import com.patitasalrescate.data.remote.dto.UpdatePetRequest;
import com.patitasalrescate.data.remote.dto.UploadFile;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.PatitasSessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadPerfilMascota extends com.patitasalrescate.controllers.base.BaseActivity {
    private static final String TAG = "PerfilMascota";
    private static final int MAX_FOTOS = 3;
    private static final String[] MIME_FOTOS = {"image/jpeg", "image/png", "image/webp"};

    private EditText txtNombre, txtEspecie, txtRaza, txtSexo, txtTemperamento, txtHistoria;
    private com.google.android.material.switchmaterial.SwitchMaterial switchDisponible;
    private com.google.android.material.switchmaterial.SwitchMaterial switchAdoptable;
    private com.google.android.material.switchmaterial.SwitchMaterial switchRequiereFormulario;
    private View layoutSelectorFormulario;
    private Spinner spinnerFormularioMascota;
    private TextView txtDisponibleVista, txtAdoptableVista, txtRequiereFormularioVista, txtAvisoNoAdoptable;
    private View cardRequiereFormulario;
    private TextView txtRequiereFormularioAdoptante;
    private View lyBotonesAccion;
    private boolean esModoRefugio = false;
    private ImageView imgFoto;
    private Button btnAccion;
    private Button btnFavorito;
    private LinearLayout layoutFotos;
    private final ImageView[] slotsFotos = new ImageView[MAX_FOTOS];
    // Una Uri por índice solo si el refugio la cambió; null = conservar la actual.
    private final Uri[] fotosNuevas = new Uri[MAX_FOTOS];
    private int slotPendiente = 0;
    private String ultimoErrorFoto = "";
    private ActivityResultLauncher<Intent> launcherFotos;
    private Mascota mascotaActual;
    private final List<FormResponse> formulariosRefugio = new ArrayList<>();
    private String idMascota;
    private String idUsuario;
    private String nombreRefugio = "";
    private String telefonoRefugio = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_perfil_mascota);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.perfilmascota), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        txtNombre.setMaxLines(2);
        txtNombre.setHorizontallyScrolling(false);
        txtNombre.setEllipsize(TextUtils.TruncateAt.END);
        txtEspecie = findViewById(R.id.txt_edit_especie);
        txtRaza = findViewById(R.id.txt_edit_raza);
        txtSexo = findViewById(R.id.txt_edit_sexo);
        txtTemperamento = findViewById(R.id.txt_edit_temperamento);
        txtHistoria = findViewById(R.id.txt_edit_historia);
        switchDisponible = findViewById(R.id.switch_disponible_mascota);
        switchAdoptable = findViewById(R.id.switch_adoptable_mascota);
        switchRequiereFormulario = findViewById(R.id.switch_requiere_formulario_mascota);
        layoutSelectorFormulario = findViewById(R.id.layout_selector_formulario);
        spinnerFormularioMascota = findViewById(R.id.spinner_formulario_mascota);
        txtDisponibleVista = findViewById(R.id.txt_disponible_mascota);
        txtAdoptableVista = findViewById(R.id.txt_adoptable_mascota);
        txtRequiereFormularioVista = findViewById(R.id.txt_requiere_formulario_mascota);
        txtAvisoNoAdoptable = findViewById(R.id.txt_aviso_no_adoptable);
        cardRequiereFormulario = findViewById(R.id.card_requiere_formulario);
        txtRequiereFormularioAdoptante = findViewById(R.id.txt_requiere_formulario_adoptante);
        lyBotonesAccion = findViewById(R.id.ly_botones_accion);
        imgFoto = findViewById(R.id.img_detalle_mascota);
        btnAccion = findViewById(R.id.btn_accion_principal);
        btnFavorito = findViewById(R.id.btn_favorito);
        layoutFotos = findViewById(R.id.layout_fotos_mascota);
        slotsFotos[0] = findViewById(R.id.foto_mascota_slot_0);
        slotsFotos[1] = findViewById(R.id.foto_mascota_slot_1);
        slotsFotos[2] = findViewById(R.id.foto_mascota_slot_2);

        if (switchRequiereFormulario != null) {
            switchRequiereFormulario.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (layoutSelectorFormulario != null) {
                    boolean editando = txtNombre != null && txtNombre.isEnabled();
                    layoutSelectorFormulario.setVisibility(
                            esModoRefugio && editando && isChecked ? View.VISIBLE : View.GONE);
                }
            });
        }

        for (int i = 0; i < slotsFotos.length; i++) {
            final int indice = i;
            slotsFotos[i].setOnClickListener(v -> {
                // Solo el refugio en modo edición puede cambiar fotos.
                if (txtNombre.isEnabled()) elegirFotoParaSlot(indice);
            });
            slotsFotos[i].setOnLongClickListener(v -> {
                if (txtNombre.isEnabled() && fotosNuevas[indice] != null) {
                    fotosNuevas[indice] = null;
                    pintarSlot(indice);
                    Toast.makeText(this, "Cambio de foto " + (indice + 1) + " descartado",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }
        launcherFotos = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null
                    && result.getData().getData() != null && !isFinishing()) {
                Uri uri = result.getData().getData();
                String mime = null;
                try {
                    mime = getContentResolver().getType(uri);
                } catch (Exception ignored) { }
                boolean permitido = false;
                for (String m : MIME_FOTOS) {
                    if (m.equalsIgnoreCase(mime)) { permitido = true; break; }
                }
                if (!permitido) {
                    Toast.makeText(this, "Solo se permiten imágenes jpeg, png o webp",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                fotosNuevas[slotPendiente] = uri;
                Log.d(TAG, "slot " + slotPendiente + " <- " + uri);
                pintarSlot(slotPendiente);
            }
        });
    }

    private void elegirFotoParaSlot(int indice) {
        slotPendiente = indice;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, MIME_FOTOS);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        launcherFotos.launch(intent);
    }

    private void pintarSlot(int indice) {
        if (slotsFotos[indice] == null) return;
        Uri nueva = fotosNuevas[indice];
        if (nueva != null) {
            slotsFotos[indice].setScaleType(ImageView.ScaleType.CENTER_CROP);
            slotsFotos[indice].setPadding(0, 0, 0, 0);
            Glide.with(this).load(nueva).centerCrop().into(slotsFotos[indice]);
            return;
        }
        List<String> fotos = mascotaActual == null ? null : mascotaActual.getFotos();
        if (fotos != null && indice < fotos.size() && fotos.get(indice) != null
                && !fotos.get(indice).isEmpty()) {
            slotsFotos[indice].setScaleType(ImageView.ScaleType.CENTER_CROP);
            slotsFotos[indice].setPadding(0, 0, 0, 0);
            Glide.with(this).load(fotos.get(indice)).centerCrop().into(slotsFotos[indice]);
        } else {
            slotsFotos[indice].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            int pad = (int) (24 * getResources().getDisplayMetrics().density);
            slotsFotos[indice].setPadding(pad, pad, pad, pad);
            slotsFotos[indice].setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    private void pintarSlots() {
        for (int i = 0; i < slotsFotos.length; i++) pintarSlot(i);
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
                configurarModoVisualPorRol();
                mostrarMascota();
                cargarDatosRefugio(mascotaActual.getIdRefugio());
                if (esModoRefugio) {
                    cargarFormulariosRefugio();
                }
            }
            @Override public void onFailure(Call<PetResponse> call, Throwable error) {
                if (!isFinishing()) Toast.makeText(ActividadPerfilMascota.this, "Sin conexión con mascotas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarFormulariosRefugio() {
        ApiApp.client().shelters.getShelterForms().enqueue(new Callback<FormResponsePagedResponse>() {
            @Override
            public void onResponse(Call<FormResponsePagedResponse> call, Response<FormResponsePagedResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                formulariosRefugio.clear();
                if (response.isSuccessful() && response.body() != null && response.body().items != null) {
                    for (FormResponse f : response.body().items) {
                        if (f != null && f.id != null) {
                            formulariosRefugio.add(f);
                        }
                    }
                }
                poblarSpinnerFormularios();
            }

            @Override
            public void onFailure(Call<FormResponsePagedResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                poblarSpinnerFormularios();
            }
        });
    }

    private void poblarSpinnerFormularios() {
        if (spinnerFormularioMascota == null) return;
        List<String> opciones = new ArrayList<>();
        opciones.add("Seleccione un formulario...");
        int seleccion = 0;
        String formActual = mascotaActual == null ? null : mascotaActual.getFormId();
        for (int i = 0; i < formulariosRefugio.size(); i++) {
            FormResponse f = formulariosRefugio.get(i);
            opciones.add(f.name == null || f.name.isEmpty() ? "Formulario " + (i + 1) : f.name);
            if (formActual != null && formActual.equals(f.id)) {
                seleccion = i + 1;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFormularioMascota.setAdapter(adapter);
        spinnerFormularioMascota.setSelection(seleccion);
    }

    private void mostrarMascota() {
        txtNombre.setText(valorSeguro(mascotaActual.getNombre()));
        txtEspecie.setText(etiquetaEspecie(parseEspecie(mascotaActual.getEspecie())));
        txtRaza.setText(valorSeguro(mascotaActual.getRaza()));
        txtSexo.setText(etiquetaGenero(parseGenero(mascotaActual.getSexo())));
        txtTemperamento.setText(valorSeguro(mascotaActual.getTemperamento()));
        txtHistoria.setText(valorSeguro(mascotaActual.getHistoria()));

        List<String> fotos = mascotaActual.getFotos();
        if (fotos != null && !fotos.isEmpty()) {
            Glide.with(this)
                    .load(fotos.get(0))
                    .centerCrop()
                    .into(imgFoto);
        }
        pintarSlots();
        pintarDisponibilidad();
    }

    /** En modo refugio: editando muestra switchs; viendo muestra Sí/No. En modo adoptante muestra si requiere formulario. */
    private void pintarDisponibilidad() {
        boolean editando = txtNombre.isEnabled();
        boolean verTexto = esModoRefugio && !editando;
        boolean verSwitch = esModoRefugio && editando;
        txtDisponibleVista.setVisibility(verTexto ? View.VISIBLE : View.GONE);
        txtAdoptableVista.setVisibility(verTexto ? View.VISIBLE : View.GONE);
        if (txtRequiereFormularioVista != null) {
            txtRequiereFormularioVista.setVisibility(verTexto ? View.VISIBLE : View.GONE);
        }
        switchDisponible.setVisibility(verSwitch ? View.VISIBLE : View.GONE);
        switchAdoptable.setVisibility(verSwitch ? View.VISIBLE : View.GONE);
        if (switchRequiereFormulario != null) {
            switchRequiereFormulario.setVisibility(verSwitch ? View.VISIBLE : View.GONE);
        }
        if (layoutSelectorFormulario != null) {
            boolean mostrarSelector = verSwitch
                    && switchRequiereFormulario != null
                    && switchRequiereFormulario.isChecked();
            layoutSelectorFormulario.setVisibility(mostrarSelector ? View.VISIBLE : View.GONE);
        }
        if (verTexto && mascotaActual != null) {
            txtDisponibleVista.setText("Visible: "
                    + ("DISPONIBLE".equals(mascotaActual.getEstado()) ? "Sí" : "No"));
            txtAdoptableVista.setText("Disponible para adopción: "
                    + (mascotaActual.esAdoptable() ? "Sí" : "No"));
            if (txtRequiereFormularioVista != null) {
                txtRequiereFormularioVista.setText("Requiere formulario: "
                        + (mascotaActual.isRequireForm() ? "Sí" : "No"));
            }
        }
        if (cardRequiereFormulario != null) {
            boolean mostrarCardAdoptante = !esModoRefugio
                    && mascotaActual != null
                    && mascotaActual.isRequireForm();
            cardRequiereFormulario.setVisibility(mostrarCardAdoptante ? View.VISIBLE : View.GONE);
            if (mostrarCardAdoptante && txtRequiereFormularioAdoptante != null) {
                txtRequiereFormularioAdoptante.setText("Sí, esta mascota requiere llenar un formulario de adopción");
            }
        }
    }

    private String valorSeguro(String s) {
        return s == null ? "" : s;
    }

    private String etiquetaEspecie(Species especie) {
        if (especie == Species.DOG) return "Perro";
        if (especie == Species.CAT) return "Gato";
        if (especie == Species.OTHER) return "Otro";
        return "No especificada";
    }

    private String etiquetaGenero(Gender genero) {
        if (genero == Gender.MALE) return "Masculino";
        if (genero == Gender.FEMALE) return "Femenino";
        return "No especificado";
    }

    /** Acepta tanto etiquetas ("Perro") como valores del enum ("DOG"). */
    private Species parseEspecie(String texto) {
        if (texto == null) return null;
        String t = texto.trim();
        if (t.equalsIgnoreCase("Perro") || t.equalsIgnoreCase("DOG")) return Species.DOG;
        if (t.equalsIgnoreCase("Gato") || t.equalsIgnoreCase("CAT")) return Species.CAT;
        if (t.equalsIgnoreCase("Otro") || t.equalsIgnoreCase("OTHER")) return Species.OTHER;
        return null;
    }

    private Gender parseGenero(String texto) {
        if (texto == null) return null;
        String t = texto.trim();
        if (t.equalsIgnoreCase("Masculino") || t.equalsIgnoreCase("MALE")) return Gender.MALE;
        if (t.equalsIgnoreCase("Femenino") || t.equalsIgnoreCase("FEMALE")) return Gender.FEMALE;
        return null;
    }

    private void configurarModoVisualPorRol() {
        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        boolean esRefugio = session.isRefugio();
        esModoRefugio = esRefugio;

        if (esRefugio) {
            txtAvisoNoAdoptable.setVisibility(View.GONE);
            lyBotonesAccion.setVisibility(View.VISIBLE);
            btnAccion.setVisibility(View.VISIBLE);
            btnFavorito.setVisibility(View.GONE);
            layoutFotos.setVisibility(View.VISIBLE);
            switchDisponible.setChecked("DISPONIBLE".equals(
                    mascotaActual == null ? null : mascotaActual.getEstado()));
            switchAdoptable.setChecked(mascotaActual != null && mascotaActual.esAdoptable());
            if (switchRequiereFormulario != null) {
                boolean req = mascotaActual != null
                        && mascotaActual.isRequireForm();
                switchRequiereFormulario.setChecked(req);
            }
            habilitarCampos(false);
            pintarDisponibilidad();
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

        boolean disponibleAdopcion = "DISPONIBLE".equals(estado)
                && mascotaActual != null && mascotaActual.esAdoptable();
        if (!disponibleAdopcion) {
            btnAccion.setVisibility(View.GONE);
            lyBotonesAccion.setVisibility(View.GONE);
            txtAvisoNoAdoptable.setVisibility(View.VISIBLE);
            txtAvisoNoAdoptable.setText(
                    "Esta mascota no está disponible para adopción pero puedes leer su historia");
            return;
        }
        txtAvisoNoAdoptable.setVisibility(View.GONE);
        lyBotonesAccion.setVisibility(View.VISIBLE);
        btnAccion.setVisibility(View.VISIBLE);
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
                btnAccion.setText("¡QUIERO ADOPTARLO!");
                btnAccion.setEnabled(true);
                btnAccion.setOnClickListener(v -> irAAdoptar());
                break;
        }
    }

    private void habilitarCampos(boolean habilitar) {
        int drawableRes = habilitar ? R.drawable.bg_input_rounded : android.R.color.transparent;
        int colorCard = ContextCompat.getColor(this,
                habilitar ? R.color.textoPrincipal : R.color.texto_card_oscuro);
        txtNombre.setTextColor(ContextCompat.getColor(this,
                habilitar ? R.color.textoPrincipal : R.color.texto_titulo_sobre_crema));
        txtEspecie.setTextColor(colorCard);
        txtRaza.setTextColor(colorCard);
        txtSexo.setTextColor(colorCard);
        txtTemperamento.setTextColor(colorCard);
        txtHistoria.setTextColor(colorCard);

        txtNombre.setEnabled(habilitar);
        txtNombre.setBackgroundResource(drawableRes);
        txtNombre.setMaxLines(2);
        txtNombre.setHorizontallyScrolling(false);
        txtNombre.setEllipsize(habilitar ? null : TextUtils.TruncateAt.END);

        txtEspecie.setEnabled(habilitar);
        txtEspecie.setBackgroundResource(drawableRes);

        txtRaza.setEnabled(habilitar);
        txtRaza.setBackgroundResource(drawableRes);

        txtSexo.setEnabled(habilitar);
        txtSexo.setBackgroundResource(drawableRes);

        txtTemperamento.setEnabled(habilitar);
        txtTemperamento.setBackgroundResource(drawableRes);

        txtHistoria.setEnabled(habilitar);
        txtHistoria.setBackgroundResource(drawableRes);

        switchDisponible.setEnabled(habilitar);
        switchAdoptable.setEnabled(habilitar);
        if (switchRequiereFormulario != null) {
            switchRequiereFormulario.setEnabled(habilitar);
        }
        if (spinnerFormularioMascota != null) {
            spinnerFormularioMascota.setEnabled(habilitar);
        }
        pintarDisponibilidad();
    }

    private void guardarCambios() {
        if (!ApiApp.exigirOnline(this)) return;
        boolean requiereForm = switchRequiereFormulario != null && switchRequiereFormulario.isChecked();
        String formIdElegido = null;
        if (requiereForm) {
            int pos = spinnerFormularioMascota == null ? 0 : spinnerFormularioMascota.getSelectedItemPosition();
            if (pos <= 0 || pos > formulariosRefugio.size()) {
                Toast.makeText(this,
                        "Debes seleccionar un formulario de los que agregaste para poder guardar los cambios",
                        Toast.LENGTH_LONG).show();
                return;
            }
            formIdElegido = formulariosRefugio.get(pos - 1).id;
        }

        UpdatePetRequest update = new UpdatePetRequest();
        update.name = txtNombre.getText().toString().trim();
        update.breed = txtRaza.getText().toString().trim();
        update.temperament = txtTemperamento.getText().toString().trim();
        update.story = txtHistoria.getText().toString().trim();
        update.species = parseEspecie(txtEspecie.getText().toString());
        update.gender = parseGenero(txtSexo.getText().toString());
        update.available = switchDisponible.isChecked();
        update.isAdoptable = switchAdoptable.isChecked();
        update.requireForm = requiereForm;
        update.formId = formIdElegido;
        if (update.species == null || update.gender == null) {
            Toast.makeText(this, "Especie o sexo inválidos (usa Perro/Gato/Otro y Masculino/Femenino)",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        btnAccion.setEnabled(false);
        ApiApp.client().pets.updatePet(idMascota, update).enqueue(new Callback<PetResponse>() {
            @Override public void onResponse(Call<PetResponse> call, Response<PetResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    ultimoErrorFoto = "";
                    int cambiadas = 0;
                    for (Uri u : fotosNuevas) if (u != null) cambiadas++;
                    Log.d(TAG, "datos OK, subiendo " + cambiadas + " foto(s) cambiada(s)");
                    subirFotosEditadas(0, 0);
                } else {
                    btnAccion.setEnabled(true);
                    Toast.makeText(ActividadPerfilMascota.this, "Error al guardar (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<PetResponse> call, Throwable error) {
                if (isFinishing() || isDestroyed()) return;
                btnAccion.setEnabled(true);
                Toast.makeText(ActividadPerfilMascota.this, "Sin conexión al guardar", Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Sube solo los índices que el refugio cambió, con PATCH /pet/{id}/photo/{index}. */
    private void subirFotosEditadas(int indice, int fallos) {
        if (isFinishing() || isDestroyed()) return;
        Log.d(TAG, "subirFotosEditadas desde=" + indice + " fallos=" + fallos);
        while (indice < fotosNuevas.length && fotosNuevas[indice] == null) {
            Log.d(TAG, "slot " + indice + " sin cambios, salto");
            indice++;
        }
        if (indice >= fotosNuevas.length) {
            btnAccion.setEnabled(true);
            Log.d(TAG, "fin subida fotos fallos=" + fallos + " ultimoError=" + ultimoErrorFoto);
            if (fallos == 0) {
                Toast.makeText(this, "Mascota actualizada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Datos guardados, pero " + fallos + " foto(s) no se subieron. "
                        + ultimoErrorFoto, Toast.LENGTH_LONG).show();
            }
            finish();
            return;
        }
        final int actual = indice;
        // El backend numera las fotos desde 1 (slots 0,1,2 -> índices 1,2,3).
        final int indiceApi = actual + 1;
        final UploadFile archivo;
        try {
            archivo = ApiApp.upload(this, fotosNuevas[indice]);
            long largo = -1;
            try { largo = archivo.content.contentLength(); } catch (Exception ignored) { }
            Log.d(TAG, "PATCH /pet/" + idMascota + "/photo/" + indiceApi
                    + " filename=" + archivo.filename
                    + " contentType=" + archivo.content.contentType() + " bytes=" + largo);
        } catch (IOException e) {
            Log.e(TAG, "foto índice " + actual + " no se pudo leer: " + e.getMessage());
            subirFotosEditadas(actual + 1, fallos + 1);
            return;
        }
        ApiApp.client().pets.updatePetPhoto(idMascota, indiceApi, archivo)
                .enqueue(new Callback<PetResponse>() {
                    @Override public void onResponse(Call<PetResponse> call, Response<PetResponse> response) {
                        String cuerpo = "";
                        try {
                            if (!response.isSuccessful() && response.errorBody() != null) {
                                cuerpo = response.errorBody().string();
                            }
                        } catch (Exception ignored) { }
                        Log.e(TAG, "foto índice " + indiceApi + " -> HTTP " + response.code()
                                + " body=" + cuerpo);
                        int nuevosFallos = fallos;
                        if (!response.isSuccessful()) {
                            nuevosFallos = fallos + 1;
                            String recorte = cuerpo.length() > 120 ? cuerpo.substring(0, 120) : cuerpo;
                            ultimoErrorFoto = "HTTP " + response.code()
                                    + (recorte.isEmpty() ? "" : " " + recorte);
                        }
                        subirFotosEditadas(actual + 1, nuevosFallos);
                    }
                    @Override public void onFailure(Call<PetResponse> call, Throwable error) {
                        Log.e(TAG, "foto índice " + indiceApi + " onFailure: " + error, error);
                        subirFotosEditadas(actual + 1, fallos + 1);
                    }
                });
    }

    private void cargarDatosRefugio(String shelterId) {
        if (shelterId == null || shelterId.isEmpty()) return;
        ApiApp.client().shelters.getShelterById(shelterId).enqueue(new Callback<ShelterResponse>() {
            @Override
            public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().name != null && !response.body().name.isEmpty()) {
                        nombreRefugio = response.body().name;
                    }
                    if (response.body().phoneNumber != null && !response.body().phoneNumber.isEmpty()) {
                        telefonoRefugio = response.body().phoneNumber;
                    }
                }
            }

            @Override
            public void onFailure(Call<ShelterResponse> call, Throwable error) {
                Log.w(TAG, "No se pudo cargar datos del refugio: " + error.getMessage());
            }
        });
    }

    private void irAAdoptar() {
        if (mascotaActual == null) return;
        if (!mascotaActual.isRequireForm()) {
            abrirWhatsApp(telefonoRefugio, mascotaActual.getNombre());
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Formulario de adopción requerido")
                .setMessage("Para adoptar a " + valorSeguro(mascotaActual.getNombre())
                        + " debes descargar y completar el formulario del refugio. "
                        + "Cuando lo tengas listo, envíalo por WhatsApp.")
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Descargar formulario", (dialog, which) -> descargarFormulario())
                .setPositiveButton("Ya tengo el formulario completo", (dialog, which) ->
                        abrirWhatsApp(telefonoRefugio, mascotaActual.getNombre()))
                .show();
    }

    /** Obtiene la URL del PDF con GET /shelter/form/{id} y la abre en el navegador. */
    private void descargarFormulario() {
        String formId = mascotaActual == null ? null : mascotaActual.getFormId();
        if (formId == null || formId.isEmpty()) {
            Toast.makeText(this, "El refugio aún no asoció un formulario a esta mascota",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!ApiApp.exigirOnline(this)) return;
        ApiApp.client().shelters.getShelterFormById(formId).enqueue(new Callback<FormResponse>() {
            @Override
            public void onResponse(Call<FormResponse> call, Response<FormResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                String url = response.isSuccessful() && response.body() != null
                        ? response.body().fileUrl : null;
                if (url == null || url.isEmpty()) {
                    Toast.makeText(ActividadPerfilMascota.this,
                            "No se pudo obtener el formulario (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    Toast.makeText(ActividadPerfilMascota.this,
                            "No hay una app para abrir el formulario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FormResponse> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(ActividadPerfilMascota.this,
                        "Sin conexión al obtener el formulario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirWhatsApp(String tel, String nombreMascota) {
        if (nombreMascota == null) nombreMascota = "";
        String telefono = normalizarTelefono(tel);
        if (telefono == null) {
            Toast.makeText(this, "El refugio no tiene un número telefónico registrado", Toast.LENGTH_LONG).show();
            return;
        }

        String mensaje = "¡Hola" + (nombreRefugio.isEmpty() ? "" : " " + nombreRefugio) + "!\n\n"
                + "Estoy interesado en adoptar a *" + (nombreMascota.isEmpty() ? "la mascota" : nombreMascota) + "*.\n\n"
                + "¿Podrían darme más información sobre los requisitos y el proceso de adopción?";

        String url = "https://api.whatsapp.com/send?phone=" + telefono + "&text=" + Uri.encode(mensaje);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Instala WhatsApp para continuar", Toast.LENGTH_SHORT).show();
        }
    }

    private String normalizarTelefono(String crudo) {
        if (crudo == null) return null;
        String digitos = crudo.replaceAll("[^0-9]", "");
        if (digitos.startsWith("51") && digitos.length() > 9) return digitos;
        if (digitos.length() == 9) return "51" + digitos;
        return digitos.isEmpty() ? null : digitos;
    }
}
