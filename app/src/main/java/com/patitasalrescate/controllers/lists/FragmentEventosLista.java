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
import com.patitasalrescate.data_access.DAOEvento;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.ui.AdaptadorEventos;
import com.patitasalrescate.utils.PatitasSessionManager;

import java.util.List;

public class FragmentEventosLista extends Fragment {

    private RecyclerView recycler;
    private AdaptadorEventos adaptador;
    private TextView txtVacio;
    private FloatingActionButton fabAgregar;
    private DAOEvento daoEvento;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_eventos_lista, container, false);

        daoEvento = new DAOEvento(requireContext());
        recycler = view.findViewById(R.id.recycler_eventos);
        txtVacio = view.findViewById(R.id.txt_lista_eventos_vacia);
        fabAgregar = view.findViewById(R.id.fab_agregar_evento);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        configurarAccesoPorRol();
        cargarEventos();

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
        List<Evento> listaEventos = daoEvento.listarTodos();

        if (listaEventos.isEmpty()) {
            recycler.setVisibility(View.GONE);
            txtVacio.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            txtVacio.setVisibility(View.GONE);
            adaptador = new AdaptadorEventos(listaEventos, requireContext());
            recycler.setAdapter(adaptador);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarEventos();
    }
}