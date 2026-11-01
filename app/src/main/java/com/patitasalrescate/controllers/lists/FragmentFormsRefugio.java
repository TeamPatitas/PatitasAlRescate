package com.patitasalrescate.controllers.lists;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.FormResponse;
import com.patitasalrescate.data.remote.dto.FormResponsePagedResponse;
import com.patitasalrescate.data.remote.dto.UploadFormRequest;
import com.patitasalrescate.ui.AdaptadorForms;
import com.patitasalrescate.utils.ApiApp;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentFormsRefugio extends Fragment {

    private RecyclerView recyclerForms;
    private TextView txtVacio;
    private FloatingActionButton fabAgregar;
    private String nombrePendienteSubida;

    private final ActivityResultLauncher<String> selectorPdf =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::subirPdfSeleccionado);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_forms_refugio, container, false);

        recyclerForms = view.findViewById(R.id.recycler_forms);
        txtVacio = view.findViewById(R.id.txt_lista_forms_vacia);
        fabAgregar = view.findViewById(R.id.fab_agregar_form);

        recyclerForms.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (fabAgregar != null) {
            fabAgregar.setOnClickListener(v -> mostrarDialogoSubirForm());
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarForms();
    }

    private void cargarForms() {
        if (recyclerForms == null || txtVacio == null) return;
        recyclerForms.setVisibility(View.GONE);
        txtVacio.setVisibility(View.VISIBLE);
        txtVacio.setText("Cargando formularios...");

        ApiApp.client().shelters.getShelterForms().enqueue(new Callback<FormResponsePagedResponse>() {
            @Override
            public void onResponse(Call<FormResponsePagedResponse> call, Response<FormResponsePagedResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (!response.isSuccessful() || response.body() == null) {
                    recyclerForms.setVisibility(View.GONE);
                    txtVacio.setVisibility(View.VISIBLE);
                    txtVacio.setText("No se pudieron cargar los formularios");
                    return;
                }
                List<FormResponse> items = response.body().items != null
                        ? response.body().items
                        : new ArrayList<>();
                if (items.isEmpty()) {
                    recyclerForms.setVisibility(View.GONE);
                    txtVacio.setVisibility(View.VISIBLE);
                    txtVacio.setText("No tienes formularios registrados");
                } else {
                    txtVacio.setVisibility(View.GONE);
                    recyclerForms.setVisibility(View.VISIBLE);
                    recyclerForms.setAdapter(new AdaptadorForms(items, getContext()));
                }
            }

            @Override
            public void onFailure(Call<FormResponsePagedResponse> call, Throwable t) {
                if (!isAdded() || txtVacio == null) return;
                if (recyclerForms != null) recyclerForms.setVisibility(View.GONE);
                txtVacio.setVisibility(View.VISIBLE);
                txtVacio.setText("No se pudieron cargar los formularios");
            }
        });
    }

    private void mostrarDialogoSubirForm() {
        if (getContext() == null || !ApiApp.exigirOnline(getContext())) return;

        EditText inputNombre = new EditText(getContext());
        inputNombre.setHint("Nombre del formulario");
        inputNombre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout contenedor = new LinearLayout(getContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(pad, pad / 2, pad, 0);
        contenedor.addView(inputNombre);

        new AlertDialog.Builder(getContext())
                .setTitle("Nuevo formulario PDF")
                .setMessage("Ingresa un nombre y selecciona un archivo PDF (máx. 5 MB).")
                .setView(contenedor)
                .setPositiveButton("Elegir PDF", (dialog, which) -> {
                    String nombre = inputNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Toast.makeText(getContext(), "Ingresa un nombre para el formulario", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    nombrePendienteSubida = nombre;
                    selectorPdf.launch("application/pdf");
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void subirPdfSeleccionado(Uri uri) {
        if (uri == null || getContext() == null) return;
        try {
            UploadFormRequest req = new UploadFormRequest();
            req.name = nombrePendienteSubida;
            req.file = ApiApp.uploadPdf(getContext(), uri);

            Toast.makeText(getContext(), "Subiendo formulario...", Toast.LENGTH_SHORT).show();
            ApiApp.client().shelters.uploadShelterForm(req).enqueue(new Callback<FormResponse>() {
                @Override
                public void onResponse(Call<FormResponse> call, Response<FormResponse> response) {
                    if (!isAdded() || getContext() == null) return;
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Formulario subido correctamente", Toast.LENGTH_SHORT).show();
                        cargarForms();
                    } else {
                        Toast.makeText(getContext(), "No se pudo subir el formulario (máx. 5 por refugio)", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<FormResponse> call, Throwable t) {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Error de red al subir el formulario", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
