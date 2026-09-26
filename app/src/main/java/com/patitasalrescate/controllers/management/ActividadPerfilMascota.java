package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.data.remote.dto.PetResponse;
import com.patitasalrescate.data.remote.dto.UpdatePetRequest;
import com.patitasalrescate.data.remote.dto.UploadFile;
import com.patitasalrescate.data.remote.dto.Species;
import com.patitasalrescate.data.remote.dto.Gender;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class ActividadPerfilMascota extends com.patitasalrescate.controllers.base.BaseActivity {
    private static final String TAG = "PerfilMascota";
    private static final int MAX_FOTOS = 3;
    private static final String[] MIME_FOTOS = {"image/jpeg", "image/png", "image/webp"};

    private EditText txtNombre, txtEspecie, txtRaza, txtSexo, txtTemperamento, txtHistoria;
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
        txtTemperamento = findViewById(R.id.txt_edit_temperamento);
        txtHistoria = findViewById(R.id.txt_edit_historia);
        imgFoto = findViewById(R.id.img_detalle_mascota);
        btnAccion = findViewById(R.id.btn_accion_principal);
        btnFavorito = findViewById(R.id.btn_favorito);
        layoutFotos = findViewById(R.id.layout_fotos_mascota);
        slotsFotos[0] = findViewById(R.id.foto_mascota_slot_0);
        slotsFotos[1] = findViewById(R.id.foto_mascota_slot_1);
        slotsFotos[2] = findViewById(R.id.foto_mascota_slot_2);
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

        if (esRefugio) {
            btnFavorito.setVisibility(View.GONE);
            layoutFotos.setVisibility(View.VISIBLE);
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
                // La adopción se coordina por WhatsApp con el teléfono del refugio.
                btnAccion.setOnClickListener(v -> irAAdoptar());
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

        txtTemperamento.setEnabled(habilitar);
        txtTemperamento.setBackgroundResource(drawableRes);
        
        txtHistoria.setEnabled(habilitar);
        txtHistoria.setBackgroundResource(drawableRes);
    }

    private void guardarCambios() {
        if (!ApiApp.exigirOnline(this)) return;
        UpdatePetRequest update = new UpdatePetRequest();
        update.name = txtNombre.getText().toString().trim();
        update.breed = txtRaza.getText().toString().trim();
        update.temperament = txtTemperamento.getText().toString().trim();
        update.story = txtHistoria.getText().toString().trim();
        update.species = parseEspecie(txtEspecie.getText().toString());
        update.gender = parseGenero(txtSexo.getText().toString());
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

    private void irAAdoptar() {
        Intent i = new Intent(this, ActividadAdopcion.class);
        i.putExtra("id_mascota_key", mascotaActual.getIdMascota());
        startActivity(i);
    }
}
