package com.patitasalrescate.controllers.lists;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private RecyclerView recycler, recyclerPasados;
    private AdaptadorEventos adaptador;
    private TextView txtVacio;
    private FloatingActionButton fabAgregar;
    private android.view.View layoutPasados;
    private Button btnMostrarPasados;
    private boolean esModoRefugio = false;
    private boolean pasadosExpandidos = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_eventos_lista, container, false);

        recycler = view.findViewById(R.id.recycler_eventos);
        txtVacio = view.findViewById(R.id.txt_lista_eventos_vacia);
        fabAgregar = view.findViewById(R.id.fab_agregar_evento);
        layoutPasados = view.findViewById(R.id.layout_pasados);
        btnMostrarPasados = view.findViewById(R.id.btn_mostrar_pasados);
        recyclerPasados = view.findViewById(R.id.recycler_eventos_pasados);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPasados.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPasados.setVisibility(android.view.View.GONE);
        btnMostrarPasados.setOnClickListener(v -> {
            pasadosExpandidos = !pasadosExpandidos;
            pintarColapsable();
        });

        esModoRefugio = PatitasSessionManager.getInstance(requireContext()).isRefugio();
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
                    List<Evento> vigentes = new ArrayList<>();
                    List<Evento> pasados = new ArrayList<>();
                    if (events != null) {
                        for (EventSummaryResponse event : events) {
                            if (event == null || event.id == null || event.id.isEmpty()) continue;
                            // El resumen no trae descripción; el detalle la carga por id.
                            if (Boolean.FALSE.equals(event.isActive)) continue;
                            // En refugio solo los propios (isYours), sin pedir el detalle.
                            if (esModoRefugio && !Boolean.TRUE.equals(event.isYours)) continue;
                            Evento item = ApiApp.event(event);
                            if (item.getDescripcion() == null) item.setDescripcion("");
                            if (ApiApp.esPasado(item.getFecha())) pasados.add(item);
                            else vigentes.add(item);
                        }
                    }
                    boolean refugio = PatitasSessionManager.getInstance(getContext()).isRefugio();
                    if (refugio) {
                        // Modo refugio: todos los eventos en una sola lista, sin colapsable.
                        layoutPasados.setVisibility(android.view.View.GONE);
                        List<Evento> todos = new ArrayList<>(vigentes);
                        todos.addAll(pasados);
                        if (todos.isEmpty()) {
                            recycler.setVisibility(View.GONE);
                            txtVacio.setVisibility(View.VISIBLE);
                            txtVacio.setText("No tienes eventos registrados");
                        } else {
                            recycler.setVisibility(View.VISIBLE);
                            txtVacio.setVisibility(View.GONE);
                            adaptador = new AdaptadorEventos(todos, getContext(), true);
                            recycler.setAdapter(adaptador);
                        }
                        return;
                    }
                    // Modo usuario: vigentes arriba + pasados en colapsable (solo si hay).
                    if (vigentes.isEmpty() && pasados.isEmpty()) {
                        recycler.setVisibility(View.GONE);
                        txtVacio.setVisibility(View.VISIBLE);
                        txtVacio.setText("No hay eventos disponibles");
                        layoutPasados.setVisibility(android.view.View.GONE);
                    } else {
                        if (vigentes.isEmpty()) {
                            recycler.setVisibility(View.GONE);
                            txtVacio.setVisibility(View.VISIBLE);
                            txtVacio.setText("No hay próximos eventos");
                        } else {
                            recycler.setVisibility(View.VISIBLE);
                            txtVacio.setVisibility(View.GONE);
                        }
                        adaptador = new AdaptadorEventos(vigentes, getContext(), false);
                        recycler.setAdapter(adaptador);
                        if (pasados.isEmpty()) {
                            layoutPasados.setVisibility(android.view.View.GONE);
                        } else {
                            layoutPasados.setVisibility(android.view.View.VISIBLE);
                            btnMostrarPasados.setText("Mostrar eventos pasados (" + pasados.size() + ")");
                            recyclerPasados.setAdapter(new AdaptadorEventos(pasados, getContext(), false));
                            pintarColapsable();
                        }
                    }
                }, error -> {
                    if (!isAdded() || txtVacio == null) return;
                    if (recycler != null) recycler.setVisibility(View.GONE);
                    txtVacio.setVisibility(View.VISIBLE);
                    txtVacio.setText("No se pudieron cargar los eventos");
                });
    }

    /** Colapsable de pasados: oculto por defecto, alterna texto y lista. */
    private void pintarColapsable() {
        if (recyclerPasados == null || btnMostrarPasados == null) return;
        recyclerPasados.setVisibility(pasadosExpandidos ? android.view.View.VISIBLE
                : android.view.View.GONE);
        String base = ((String) btnMostrarPasados.getText()).replace("Ocultar ", "Mostrar ");
        if (pasadosExpandidos) base = base.replace("Mostrar ", "Ocultar ");
        btnMostrarPasados.setText(base);
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarEventos();
    }
}
