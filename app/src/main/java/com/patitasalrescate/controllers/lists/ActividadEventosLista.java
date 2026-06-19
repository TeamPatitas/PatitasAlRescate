package com.patitasalrescate.controllers.lists;

import android.os.Bundle;

import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.ui.AdaptadorEventos;

import java.util.ArrayList;
import java.util.List;

public class ActividadEventosLista extends AppCompatActivity {

    private RecyclerView recycler;
    private AdaptadorEventos adaptador;
    private TextView txtVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ly_eventos_lista);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.listar_eventos), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarListarEventos);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler_eventos);
        txtVacio = findViewById(R.id.txt_lista_eventos_vacia);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        List<Evento> listaEventos = obtenerEventosPrueba();
        
        if (listaEventos.isEmpty()) {
            recycler.setVisibility(View.GONE);
            txtVacio.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            txtVacio.setVisibility(View.GONE);
            adaptador = new AdaptadorEventos(listaEventos, this);
            recycler.setAdapter(adaptador);
        }

        recycler = findViewById(R.id.recycler_eventos);
        txtVacio = findViewById(R.id.txt_lista_eventos_vacia);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        cargarEventos();
    }

    private void cargarEventos() {
        List<Evento> listaEventos = obtenerEventosPrueba();
        mostrarEventos(listaEventos);
    }

    private void mostrarEventos(List<Evento> listaEventos) {
        if (listaEventos == null || listaEventos.isEmpty()) {
            recycler.setVisibility(View.GONE);
            txtVacio.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            txtVacio.setVisibility(View.GONE);
            adaptador = new AdaptadorEventos(listaEventos, this);
            recycler.setAdapter(adaptador);
        }
    }

    private List<Evento> obtenerEventosPrueba() {
        List<Evento> eventos = new ArrayList<>();
        eventos.add(new Evento("1", "Gran Campaña de Adopción", "20 de Octubre, 2023", "Ven a conocer a tu futuro mejor amigo en el Parque Central.", ""));
        eventos.add(new Evento("2", "Colecta de Alimento", "25 de Octubre, 2023", "Estaremos recibiendo donaciones de alimento para perros y gatos.", ""));
        eventos.add(new Evento("3", "Charla sobre Tenencia Responsable", "2 de Noviembre, 2023", "Aprende todo lo necesario para cuidar a tu mascota.", ""));
        return eventos;
    }
}