package com.patitasalrescate.controllers.lists;

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

import com.patitasalrescate.R;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.ui.AdaptadorEventos;

import java.util.ArrayList;
import java.util.List;

public class FragmentEventosLista extends Fragment {

    private RecyclerView recycler;
    private AdaptadorEventos adaptador;
    private TextView txtVacio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_eventos_lista, container, false);

        recycler = view.findViewById(R.id.recycler_eventos);
        txtVacio = view.findViewById(R.id.txt_lista_eventos_vacia);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Evento> listaEventos = obtenerEventosPrueba();
        
        if (listaEventos.isEmpty()) {
            recycler.setVisibility(View.GONE);
            txtVacio.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            txtVacio.setVisibility(View.GONE);
            adaptador = new AdaptadorEventos(listaEventos, requireContext());
            recycler.setAdapter(adaptador);
        }

        return view;
    }

    private List<Evento> obtenerEventosPrueba() {
        List<Evento> eventos = new ArrayList<>();
        eventos.add(new Evento("1", "Gran Campaña de Adopción", "20 de Octubre, 2023", "Ven a conocer a tu futuro mejor amigo en el Parque Central.", "", -7.1589, -78.5147));
        eventos.add(new Evento("2", "Colecta de Alimento", "25 de Octubre, 2023", "Estaremos recibiendo donaciones de alimento para perros y gatos.", "", -7.1565, -78.5168));
        eventos.add(new Evento("3", "Charla sobre Tenencia Responsable", "2 de Noviembre, 2023", "Aprende todo lo necesario para cuidar a tu mascota.", "", -7.1522, -78.5095));
        return eventos;
    }
}