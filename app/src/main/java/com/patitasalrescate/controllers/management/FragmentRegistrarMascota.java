package com.patitasalrescate.controllers.management;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.patitasalrescate.R;
import com.patitasalrescate.data_access.DAOMascota;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.ui.AdaptadorFotosPreview;
import com.patitasalrescate.utils.PatitasSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FragmentRegistrarMascota extends Fragment {

    private EditText txtNombre, txtEdad, txtTemperamento, txtHistoria;
    private EditText txtOtraEspecie, txtOtraRaza;
    private TextInputLayout lyOtraEspecie, lyOtraRaza;

    private Spinner spinnerEspecie, spinnerRaza, spinnerSexo;
    private Button btnSeleccionarFotos, btnGuardar;
    private RecyclerView recyclerFotosPreview;

    private DAOMascota daoMascota;

    private List<Uri> urisFotosSeleccionadas = new ArrayList<>();
    private ActivityResultLauncher<Intent> launcherGaleria;

    private final String[] especies = {"Seleccione...", "Perro", "Gato", "Conejo", "Otro"};
    private final String[] razasPerro = {"Seleccione...", "Perro único (Chusco)", "Schnauzer", "Poodle", "Golden Retriever", "Otro"};
    private final String[] razasGato = {"Seleccione...", "Persa", "Siamés", "Angora", "Otro"};
    private final String[] razasConejo = {"Seleccione...", "Cabeza de León", "Belier", "Otro"};
    private final String[] sexos = {"Macho", "Hembra"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_registrar_mascota, container, false);

        daoMascota = new DAOMascota(requireContext());

        txtNombre = view.findViewById(R.id.txt_reg_nombre_mascota);
        txtEdad = view.findViewById(R.id.txt_reg_edad);
        txtTemperamento = view.findViewById(R.id.txt_reg_temperamento);
        txtHistoria = view.findViewById(R.id.txt_reg_historia);

        spinnerEspecie = view.findViewById(R.id.spinner_especie);
        spinnerRaza = view.findViewById(R.id.spinner_raza);
        spinnerSexo = view.findViewById(R.id.spinner_sexo);

        txtOtraEspecie = view.findViewById(R.id.txt_otra_especie);
        txtOtraRaza = view.findViewById(R.id.txt_otra_raza);
        lyOtraEspecie = view.findViewById(R.id.ly_otra_especie);
        lyOtraRaza = view.findViewById(R.id.ly_otra_raza);

        btnSeleccionarFotos = view.findViewById(R.id.btn_seleccionar_fotos);
        btnGuardar = view.findViewById(R.id.btn_guardar_mascota);
        recyclerFotosPreview = view.findViewById(R.id.recycler_fotos_preview);

        recyclerFotosPreview.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerFotosPreview.setAdapter(new AdaptadorFotosPreview(urisFotosSeleccionadas));
        configurarLauncherFotos();

        configurarSpinners();

        btnGuardar.setOnClickListener(v -> registrarMascota());

        return view;
    }

    private void configurarSpinners() {
        ArrayAdapter<String> adapterSexo = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sexos);
        spinnerSexo.setAdapter(adapterSexo);

        ArrayAdapter<String> adapterEspecie = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, especies);
        spinnerEspecie.setAdapter(adapterEspecie);

        spinnerEspecie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccion = especies[position];
                actualizarSpinnerRaza(seleccion);

                if (seleccion.equals("Otro")) {
                    lyOtraEspecie.setVisibility(View.VISIBLE);
                    lyOtraRaza.setVisibility(View.VISIBLE);
                    spinnerRaza.setVisibility(View.GONE);
                } else {
                    lyOtraEspecie.setVisibility(View.GONE);
                    spinnerRaza.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerRaza.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String razaSeleccionada = (String) parent.getItemAtPosition(position);
                if (razaSeleccionada.equals("Otro")) {
                    lyOtraRaza.setVisibility(View.VISIBLE);
                } else {
                    lyOtraRaza.setVisibility(View.GONE);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void actualizarSpinnerRaza(String especie) {
        String[] razas;
        switch (especie) {
            case "Perro": razas = razasPerro; break;
            case "Gato": razas = razasGato; break;
            case "Conejo": razas = razasConejo; break;
            default: razas = new String[]{}; break;
        }

        ArrayAdapter<String> adapterRaza = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, razas);
        spinnerRaza.setAdapter(adapterRaza);
    }

    private void registrarMascota() {
        String nombre = txtNombre.getText().toString().trim();
        String edadStr = txtEdad.getText().toString().trim();
        String temperamento = txtTemperamento.getText().toString().trim();
        String historia = txtHistoria.getText().toString().trim();
        String sexo = spinnerSexo.getSelectedItem().toString();
        String especie = spinnerEspecie.getSelectedItem().toString();
        String raza = "";

        if (especie.equals("Seleccione...")) {
            Toast.makeText(requireContext(), "Selecciona una especie", Toast.LENGTH_SHORT).show();
            return;
        }
        if (especie.equals("Otro")) {
            especie = txtOtraEspecie.getText().toString().trim();
            raza = txtOtraRaza.getText().toString().trim();
        } else {
            if (spinnerRaza.getSelectedItem() != null) {
                raza = spinnerRaza.getSelectedItem().toString();
            }
            if (raza.equals("Seleccione...")) {
                Toast.makeText(requireContext(), "Selecciona una raza", Toast.LENGTH_SHORT).show();
                return;
            }
            if (raza.equals("Otro")) {
                raza = txtOtraRaza.getText().toString().trim();
            }
        }

        if (nombre.isEmpty() || especie.isEmpty() || raza.isEmpty()) {
            Toast.makeText(requireContext(), "Faltan datos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Edad inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        String idRefugio = PatitasSessionManager.getInstance(requireContext()).getUserId();
        
        List<String> fotosMock = new ArrayList<>();
        fotosMock.add("https://images.dog.ceo/breeds/labrador/n02099712_1150.jpg");

        Mascota nuevaMascota = new Mascota(
                UUID.randomUUID().toString(), idRefugio, nombre, especie, raza, sexo,
                edad, temperamento, historia, fotosMock,
                "DISPONIBLE", System.currentTimeMillis()
        );

        daoMascota.insertar(nuevaMascota);
        Toast.makeText(requireContext(), "¡Mascota registrada exitosamente (Demo)!", Toast.LENGTH_SHORT).show();
        
        if (getActivity() != null) {
            getActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private void configurarLauncherFotos() {
        launcherGaleria = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                if (result.getData().getClipData() != null) {
                    int count = result.getData().getClipData().getItemCount();
                    for (int i = 0; i < count; i++) urisFotosSeleccionadas.add(result.getData().getClipData().getItemAt(i).getUri());
                } else if (result.getData().getData() != null) {
                    urisFotosSeleccionadas.add(result.getData().getData());
                }
                if (recyclerFotosPreview.getAdapter() != null) {
                    recyclerFotosPreview.getAdapter().notifyDataSetChanged();
                }
            }
        });

        btnSeleccionarFotos.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            launcherGaleria.launch(intent);
        });
    }
}
