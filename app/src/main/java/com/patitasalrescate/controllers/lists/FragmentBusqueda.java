package com.patitasalrescate.controllers.lists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.data.mock.DAOMascota;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.ui.AdaptadorMascotas;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.ApiPages;
import com.patitasalrescate.data.remote.dto.PetSummaryResponse;
import com.patitasalrescate.data.remote.dto.PetResponse;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class FragmentBusqueda extends Fragment {
    private Spinner spFiltro;
    private EditText txtFiltro;
    private Button btnBuscar;
    private RecyclerView recycler;
    private DAOMascota daoMascota;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_busqueda, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.busquedafiltro), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spFiltro = view.findViewById(R.id.spFiltro);
        txtFiltro = view.findViewById(R.id.txtFiltro);
        btnBuscar = view.findViewById(R.id.btnBuscar);
        recycler = view.findViewById(R.id.recycler_mascotas);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        daoMascota = new DAOMascota(getContext());

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Nombre", "Especie", "Raza", "Sexo"}
        );
        spFiltro.setAdapter(adapterSpinner);

        btnBuscar.setOnClickListener(v -> buscar());
        updateTitle("Búsqueda");
        return view;
    }

    private void buscar() {
        String texto = txtFiltro.getText().toString().toLowerCase();
        String tipoFiltro = spFiltro.getSelectedItem().toString();
        btnBuscar.setEnabled(false);
        ApiPages.load(page -> ApiApp.client().pets.getAllPets(page, 50),
                data -> data.totalPages, data -> data.items,
                summaries -> cargarDetalles(summaries, 0, new ArrayList<>(), texto, tipoFiltro),
                error -> {
                    if (!isAdded()) return;
                    btnBuscar.setEnabled(true);
                    Toast.makeText(requireContext(), "No se pudo buscar en la API", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarDetalles(List<PetSummaryResponse> summaries, int index, List<Mascota> lista,
                                String texto, String tipoFiltro) {
        if (!isAdded()) return;
        if (index < summaries.size()) {
            ApiApp.client().pets.getPetById(summaries.get(index).id).enqueue(new Callback<PetResponse>() {
                @Override public void onResponse(Call<PetResponse> call, Response<PetResponse> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) lista.add(ApiApp.pet(response.body()));
                    cargarDetalles(summaries, index + 1, lista, texto, tipoFiltro);
                }
                @Override public void onFailure(Call<PetResponse> call, Throwable error) {
                    if (!isAdded()) return;
                    btnBuscar.setEnabled(true);
                    Toast.makeText(requireContext(), "No se pudieron cargar los detalles", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        btnBuscar.setEnabled(true);
        List<Mascota> resultados = new ArrayList<>();
        for (Mascota m : lista) {
            boolean coincide = false;
            switch (tipoFiltro) {
                case "Nombre":
                    coincide = m.getNombre() != null && m.getNombre().toLowerCase().contains(texto);
                    break;
                case "Especie":
                    coincide = m.getEspecie() != null && m.getEspecie().toLowerCase().contains(texto);
                    break;
                case "Raza":
                    coincide = m.getRaza() != null && m.getRaza().toLowerCase().contains(texto);
                    break;
                case "Sexo":
                    coincide = m.getSexo() != null && m.getSexo().toLowerCase().contains(texto);
                    break;
            }
            if (coincide) resultados.add(m);
        }

        AdaptadorMascotas adapter = new AdaptadorMascotas(
                resultados,
                false,
                getContext(),
                daoMascota
        );
        recycler.setAdapter(adapter);
    }

    private void updateTitle(String title) {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(title);
            }
        }
    }
}
