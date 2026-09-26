package com.patitasalrescate.controllers.management;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.patitasalrescate.R;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.data.remote.dto.CreatePetRequest;
import com.patitasalrescate.data.remote.dto.PetResponse;
import com.patitasalrescate.data.remote.dto.Species;
import com.patitasalrescate.data.remote.dto.Gender;
import com.patitasalrescate.data.remote.dto.UserResponse;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class FragmentRegistrarMascota extends Fragment {

    private static final String TAG = "RegistrarMascota";
    private static final int MAX_FOTOS = 3;
    private static final String[] MIME_FOTOS = {"image/jpeg", "image/png", "image/webp"};

    private EditText txtNombre, txtTemperamento, txtHistoria;
    private EditText txtOtraRaza;
    private TextInputLayout lyOtraRaza;

    private Spinner spinnerEspecie, spinnerRaza, spinnerSexo;
    private Button btnGuardar;
    private com.google.android.material.switchmaterial.SwitchMaterial switchDisponible;

    // Una imagen por índice (0, 1, 2): el endpoint de fotos exige índice.
    private String ultimoErrorFoto = "";
    private final Uri[] fotosPorIndice = new Uri[MAX_FOTOS];
    private final ImageView[] slotsFotos = new ImageView[MAX_FOTOS];
    private int slotPendiente = 0;
    private ActivityResultLauncher<Intent> launcherGaleria;

    // Especies sincronizadas con el enum Species: el spinner se arma desde estos valores,
    // así lo que se envía a la API siempre es un enum válido.
    private final Species[] especiesEnum = {Species.DOG, Species.CAT, Species.OTHER};
    private final String[] razasPerro = {"Seleccione...", "Perro único (Chusco)", "Schnauzer", "Poodle", "Golden Retriever", "Otro"};
    private final String[] razasGato = {"Seleccione...", "Persa", "Siamés", "Angora", "Otro"};
    private final String[] sexos = {"Macho", "Hembra"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_registrar_mascota, container, false);

        txtNombre = view.findViewById(R.id.txt_reg_nombre_mascota);
        txtTemperamento = view.findViewById(R.id.txt_reg_temperamento);
        txtHistoria = view.findViewById(R.id.txt_reg_historia);

        spinnerEspecie = view.findViewById(R.id.spinner_especie);
        spinnerRaza = view.findViewById(R.id.spinner_raza);
        spinnerSexo = view.findViewById(R.id.spinner_sexo);

        txtOtraRaza = view.findViewById(R.id.txt_otra_raza);
        lyOtraRaza = view.findViewById(R.id.ly_otra_raza);

        switchDisponible = view.findViewById(R.id.switch_disponible);
        btnGuardar = view.findViewById(R.id.btn_guardar_mascota);

        slotsFotos[0] = view.findViewById(R.id.foto_slot_0);
        slotsFotos[1] = view.findViewById(R.id.foto_slot_1);
        slotsFotos[2] = view.findViewById(R.id.foto_slot_2);
        for (int i = 0; i < slotsFotos.length; i++) {
            final int indice = i;
            slotsFotos[i].setOnClickListener(v -> elegirFotoParaSlot(indice));
            slotsFotos[i].setOnLongClickListener(v -> {
                fotosPorIndice[indice] = null;
                pintarSlot(indice);
                Toast.makeText(requireContext(), "Foto " + (indice + 1) + " quitada",
                        Toast.LENGTH_SHORT).show();
                return true;
            });
        }
        configurarLauncherFotos();

        configurarSpinners();

        btnGuardar.setOnClickListener(v -> registrarMascota());

        return view;
    }

    private String etiquetaEspecie(Species especie) {
        if (especie == Species.DOG) return "Perro";
        if (especie == Species.CAT) return "Gato";
        return "Otro";
    }

    /** Posición 0 = "Seleccione..." (null); el resto mapea directo al enum. */
    private Species especieDePosicion(int posicion) {
        if (posicion <= 0 || posicion > especiesEnum.length) return null;
        return especiesEnum[posicion - 1];
    }

    private void configurarSpinners() {
        ArrayAdapter<String> adapterSexo = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sexos);
        spinnerSexo.setAdapter(adapterSexo);

        List<String> etiquetas = new ArrayList<>();
        etiquetas.add("Seleccione...");
        for (Species especie : especiesEnum) etiquetas.add(etiquetaEspecie(especie));
        ArrayAdapter<String> adapterEspecie = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, etiquetas);
        spinnerEspecie.setAdapter(adapterEspecie);

        spinnerEspecie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarSpinnerRaza(especieDePosicion(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerRaza.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object razaSeleccionada = parent.getItemAtPosition(position);
                // "Otro" dentro de Perro/Gato pide texto libre; en especie "Otro" el texto es obligatorio.
                lyOtraRaza.setVisibility("Otro".equals(razaSeleccionada == null ? null : razaSeleccionada.toString())
                        ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void actualizarSpinnerRaza(Species especie) {
        if (especie == Species.OTHER) {
            // Sin lista cerrada: la raza se escribe libremente (ej. "Conejo Cabeza de León").
            spinnerRaza.setVisibility(View.GONE);
            lyOtraRaza.setVisibility(View.VISIBLE);
            return;
        }
        String[] razas;
        if (especie == Species.DOG) razas = razasPerro;
        else if (especie == Species.CAT) razas = razasGato;
        else razas = new String[]{"Seleccione..."};

        spinnerRaza.setVisibility(View.VISIBLE);
        lyOtraRaza.setVisibility(View.GONE);
        ArrayAdapter<String> adapterRaza = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, razas);
        spinnerRaza.setAdapter(adapterRaza);
    }

    private void registrarMascota() {
        if (!ApiApp.exigirOnline(requireContext())) return;
        Log.d(TAG, "registrarMascota() iniciado");
        String nombre = txtNombre.getText().toString().trim();
        String temperamento = txtTemperamento.getText().toString().trim();
        String historia = txtHistoria.getText().toString().trim();
        Object sexoSel = spinnerSexo.getSelectedItem();
        String sexo = sexoSel == null ? "" : sexoSel.toString();
        int posEspecie = spinnerEspecie.getSelectedItemPosition();
        Species especie = especieDePosicion(posEspecie);
        Log.d(TAG, "form: nombre='" + nombre + "' posEspecie=" + posEspecie + " especie=" + especie
                + " sexo='" + sexo + "' fotos=" + contarFotos());

        if (nombre.isEmpty()) {
            Log.d(TAG, "validación fallida: nombre vacío");
            Toast.makeText(requireContext(), "Ingresa el nombre de la mascota", Toast.LENGTH_SHORT).show();
            return;
        }
        if (especie == null) {
            Log.d(TAG, "validación fallida: especie null");
            Toast.makeText(requireContext(), "Selecciona una especie", Toast.LENGTH_SHORT).show();
            return;
        }

        String raza;
        if (especie == Species.OTHER) {
            raza = txtOtraRaza.getText().toString().trim();
            if (raza.isEmpty()) {
                Log.d(TAG, "validación fallida: raza libre vacía (OTHER)");
                Toast.makeText(requireContext(), "Escribe la raza", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Object seleccion = spinnerRaza.getSelectedItem();
            raza = seleccion == null ? "" : seleccion.toString();
            if (raza.isEmpty() || raza.equals("Seleccione...")) {
                Log.d(TAG, "validación fallida: raza='" + raza + "'");
                Toast.makeText(requireContext(), "Selecciona una raza", Toast.LENGTH_SHORT).show();
                return;
            }
            if (raza.equals("Otro")) {
                raza = txtOtraRaza.getText().toString().trim();
                if (raza.isEmpty()) {
                    Log.d(TAG, "validación fallida: raza 'Otro' sin texto");
                    Toast.makeText(requireContext(), "Escribe la raza", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        CreatePetRequest request = new CreatePetRequest();
        request.name = nombre;
        request.species = especie;
        request.breed = raza;
        request.gender = "Macho".equalsIgnoreCase(sexo) ? Gender.MALE : Gender.FEMALE;
        request.temperament = temperamento.isEmpty() ? null : temperamento;
        request.story = historia.isEmpty() ? null : historia;
        request.available = switchDisponible != null && switchDisponible.isChecked();
        // Las fotos van por endpoint separado (PATCH /pet/{id}/photo/{index}): la mascota
        // se crea sin fotos y luego se suben una por índice.
        request.photos = null;
        Log.d(TAG, "request: species=" + request.species + " breed='" + raza + "' gender=" + request.gender
                + " available=" + request.available + " fotos en slots=" + contarFotos());
        btnGuardar.setEnabled(false);
        verificarPermisoYEnviar(request);
    }

    /**
     * POST /pet exige rol ShelterOwner. Se verifica con roles frescos de la API antes de enviar
     * para no recibir un 403 a ciegas.
     */
    private void verificarPermisoYEnviar(CreatePetRequest request) {
        Log.d(TAG, "verificando rol ShelterOwner… autenticado="
                + ApiApp.client().session.isAuthenticated());
        ApiApp.client().admin.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    btnGuardar.setEnabled(true);
                    Log.e(TAG, "no se pudo verificar el rol: HTTP " + response.code());
                    Toast.makeText(requireContext(), "No se pudo verificar tu permiso (HTTP "
                            + response.code() + ")", Toast.LENGTH_LONG).show();
                    return;
                }
                boolean owner = false;
                if (response.body().roles != null) {
                    for (String rol : response.body().roles) {
                        if ("ShelterOwner".equalsIgnoreCase(rol)) { owner = true; break; }
                    }
                }
                Log.d(TAG, "roles frescos=" + response.body().roles + " owner=" + owner
                        + " shelterId=" + response.body().shelterId);
                if (!owner) {
                    btnGuardar.setEnabled(true);
                    Toast.makeText(requireContext(),
                            "Necesitas un refugio habilitado para registrar mascotas",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                enviarRegistro(request);
            }
            @Override public void onFailure(Call<UserResponse> call, Throwable error) {
                if (!isAdded()) return;
                btnGuardar.setEnabled(true);
                Log.e(TAG, "falló verificación de rol: " + error, error);
                Toast.makeText(requireContext(), "Sin conexión al verificar tu permiso",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enviarRegistro(CreatePetRequest request) {
        Log.d(TAG, "enviando POST /pet…");
        try {
            ApiApp.client().pets.createPet(request).enqueue(new Callback<PetResponse>() {
                @Override public void onResponse(Call<PetResponse> call, Response<PetResponse> response) {
                    if (!isAdded()) return;
                    btnGuardar.setEnabled(true);
                    Log.d(TAG, "respuesta POST /pet: HTTP " + response.code());
                    if (response.isSuccessful() && response.body() != null && response.body().id != null) {
                        ultimoErrorFoto = "";
                        Log.d(TAG, "mascota creada id=" + response.body().id
                                + ", iniciando subida de " + contarFotos() + " foto(s)");
                        subirFotosPorIndice(response.body().id, 0, 0);
                    } else if (response.isSuccessful()) {
                        Log.e(TAG, "POST /pet OK pero sin id en el body");
                        finalizarRegistro(0);
                    } else {
                        String cuerpo = "";
                        try {
                            if (response.errorBody() != null) cuerpo = response.errorBody().string();
                        } catch (Exception ignored) { }
                        Log.e(TAG, "POST pet -> HTTP " + response.code() + " body=" + cuerpo);
                        if (response.code() == 403) {
                            // El JWT es anterior al permiso: los roles van en el token.
                            Toast.makeText(requireContext(), "Permiso denegado. Si tu refugio fue "
                                    + "habilitado hace poco, cierra sesión y vuelve a entrar para "
                                    + "renovar tu acceso.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String detalle = cuerpo.length() > 120 ? cuerpo.substring(0, 120) : cuerpo;
                        Toast.makeText(requireContext(), "Error al registrar (HTTP " + response.code()
                                + (detalle.isEmpty() ? "" : ": " + detalle) + ")", Toast.LENGTH_LONG).show();
                    }
                }
                @Override public void onFailure(Call<PetResponse> call, Throwable error) {
                    if (!isAdded()) return;
                    btnGuardar.setEnabled(true);
                    Log.e(TAG, "POST /pet onFailure: " + error, error);
                    Toast.makeText(requireContext(), "Sin conexión al registrar mascota", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            btnGuardar.setEnabled(true);
            Log.e(TAG, "excepción al crear la llamada: " + e, e);
            Toast.makeText(requireContext(), "Error interno: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean esMimePermitido(Uri uri) {
        String mime = null;
        try {
            mime = requireContext().getContentResolver().getType(uri);
        } catch (Exception ignored) { }
        for (String permitido : MIME_FOTOS) {
            if (permitido.equalsIgnoreCase(mime)) return true;
        }
        return false;
    }

    private int contarFotos() {
        int n = 0;
        for (Uri uri : fotosPorIndice) if (uri != null) n++;
        return n;
    }

    private void pintarSlot(int indice) {
        if (!isAdded() || slotsFotos[indice] == null) return;
        Uri uri = fotosPorIndice[indice];
        if (uri == null) {
            slotsFotos[indice].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            slotsFotos[indice].setPadding(dp(28), dp(28), dp(28), dp(28));
            slotsFotos[indice].setImageResource(android.R.drawable.ic_menu_camera);
        } else {
            slotsFotos[indice].setScaleType(ImageView.ScaleType.CENTER_CROP);
            slotsFotos[indice].setPadding(0, 0, 0, 0);
            Glide.with(this).load(uri).centerCrop().into(slotsFotos[indice]);
        }
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    private void elegirFotoParaSlot(int indice) {
        slotPendiente = indice;
        // Una sola imagen por slot (el índice lo da el slot, no el endpoint múltiple).
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, MIME_FOTOS);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        launcherGaleria.launch(intent);
    }

    private void configurarLauncherFotos() {
        launcherGaleria = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null
                    && result.getData().getData() != null && isAdded()) {
                Uri uri = result.getData().getData();
                if (!esMimePermitido(uri)) {
                    Toast.makeText(requireContext(), "Solo se permiten imágenes jpeg, png o webp",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                fotosPorIndice[slotPendiente] = uri;
                Log.d(TAG, "slot " + slotPendiente + " <- " + uri);
                pintarSlot(slotPendiente);
            }
        });
    }

    /**
     * Sube las fotos una por una con PATCH /pet/{id}/photo/{index}, saltando slots vacíos.
     */
    private void subirFotosPorIndice(String petId, int indice, int fallos) {
        if (!isAdded()) return;
        Log.d(TAG, "subirFotosPorIndice pet=" + petId + " desde=" + indice + " fallos=" + fallos);
        while (indice < fotosPorIndice.length && fotosPorIndice[indice] == null) {
            Log.d(TAG, "slot " + indice + " vacío, salto");
            indice++;
        }
        if (indice >= fotosPorIndice.length) {
            finalizarRegistro(fallos);
            return;
        }
        final int actual = indice;
        // El backend numera las fotos desde 1 (slots 0,1,2 -> índices 1,2,3).
        final int indiceApi = actual + 1;
        final Uri uri = fotosPorIndice[indice];
        Log.d(TAG, "PATCH /pet/" + petId + "/photo/" + indiceApi + " uri=" + uri);
        final com.patitasalrescate.data.remote.dto.UploadFile archivo;
        try {
            archivo = ApiApp.upload(requireContext(), uri);
            long largo = -1;
            try { largo = archivo.content.contentLength(); } catch (Exception ignored) { }
            Log.d(TAG, "foto índice " + actual + " lista: filename=" + archivo.filename
                    + " contentType=" + archivo.content.contentType() + " bytes=" + largo);
        } catch (IOException e) {
            Log.e(TAG, "foto índice " + actual + " no se pudo leer: " + e.getMessage());
            subirFotosPorIndice(petId, actual + 1, fallos + 1);
            return;
        }
        ApiApp.client().pets.updatePetPhoto(petId, indiceApi, archivo)
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
                        subirFotosPorIndice(petId, actual + 1, nuevosFallos);
                    }
                    @Override public void onFailure(Call<PetResponse> call, Throwable error) {
                        Log.e(TAG, "foto índice " + indiceApi + " onFailure: " + error, error);
                        subirFotosPorIndice(petId, actual + 1, fallos + 1);
                    }
                });
    }

    private void finalizarRegistro(int fallosFotos) {
        if (!isAdded()) return;
        btnGuardar.setEnabled(true);
        int total = contarFotos();
        Log.d(TAG, "finalizarRegistro fallos=" + fallosFotos + " total=" + total
                + " ultimoError=" + ultimoErrorFoto);
        if (fallosFotos == 0) {
            Toast.makeText(requireContext(), "Mascota registrada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Mascota registrada, pero "
                    + fallosFotos + " de " + total + " fotos no se subieron. "
                    + ultimoErrorFoto, Toast.LENGTH_LONG).show();
        }
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }
}
