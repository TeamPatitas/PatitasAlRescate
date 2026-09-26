package com.patitasalrescate.controllers.lists;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.management.ActividadRegistrarEvento;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.ui.AdaptadorEventos;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.ApiPages;
import com.patitasalrescate.data.remote.dto.EventSummaryResponse;
import java.util.ArrayList;

import java.util.List;

public class FragmentEventosLista extends Fragment {

    private RecyclerView recycler;
    private AdaptadorEventos adaptador;
    private TextView txtVacio;
    private FloatingActionButton fabAgregar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_eventos_lista, container, false);

        recycler = view.findViewById(R.id.recycler_eventos);
        txtVacio = view.findViewById(R.id.txt_lista_eventos_vacia);
        fabAgregar = view.findViewById(R.id.fab_agregar_evento);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        configurarAccesoPorRol();
        return view;
    }

    private void configurarAccesoPorRol() {
        if (PatitasSessionManager.getInstance(requireContext()).isRefugio()) {
            fabAgregar.setVisibility(View.VISIBLE);
            fabAgregar.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ActividadRegistrarEvento.class);
                startActivity(intent);
            });
        } else {
            fabAgregar.setVisibility(View.GONE);
        }
    }

    private void cargarEventos() {
        if (recycler == null || txtVacio == null) return;
        recycler.setVisibility(View.GONE);
        txtVacio.setVisibility(View.VISIBLE);
        txtVacio.setText("Cargando eventos...");
        ApiPages.load(page -> ApiApp.client().events.getAllEvents(page, 50),
                data -> data.totalPages, data -> data.items,
                events -> {
                    if (!isAdded() || getContext() == null) return;
                    List<Evento> lista = new ArrayList<>();
                    if (events != null) {
                        for (EventSummaryResponse event : events) {
                            if (event == null || event.id == null || event.id.isEmpty()) continue;
                            // El resumen no trae descripción; el detalle la carga por id.
                            if (Boolean.FALSE.equals(event.isActive)) continue;
                            Evento item = ApiApp.event(event);
                            if (item.getDescripcion() == null) item.setDescripcion("");
                            lista.add(item);
                        }
                    }
                    if (lista.isEmpty()) {
                        recycler.setVisibility(View.GONE);
                        txtVacio.setVisibility(View.VISIBLE);
                        txtVacio.setText("No hay eventos disponibles");
                    } else {
                        recycler.setVisibility(View.VISIBLE);
                        txtVacio.setVisibility(View.GONE);
                        adaptador = new AdaptadorEventos(lista, getContext(),
                                PatitasSessionManager.getInstance(getContext()).isRefugio());
                        recycler.setAdapter(adaptador);
                    }
                }, error -> {
                    if (!isAdded() || txtVacio == null) return;
                    if (recycler != null) recycler.setVisibility(View.GONE);
                    txtVacio.setVisibility(View.VISIBLE);
                    txtVacio.setText("No se pudieron cargar los eventos");
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarEventos();
    }
}
