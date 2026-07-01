package com.patitasalrescate.controllers.lists;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.data_access.DAOMascota;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.ui.AdaptadorMascotas;

import java.util.ArrayList;
import java.util.List;

public class ActividadListarMascotas extends AppCompatActivity {
    private RecyclerView recycler;
    private TextView txtVacio;
    private LinearLayout lyFiltros;
    private Button btnEnAdopcion, btnAdoptados;
    private DAOMascota dao;
    private List<Mascota> listaCacheRefugio;
    private boolean esModoRefugio = false;
    private String idUsuario;
    private String tipoUsuario;
    private boolean verAdoptadosRefugio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_listar_mascotas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.listarmascotas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dao = new DAOMascota(this);
        listaCacheRefugio = new ArrayList<>();

        if (getIntent().hasExtra("es_refugio_key")) {
            esModoRefugio = getIntent().getBooleanExtra("es_refugio_key", false);
        }

        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        idUsuario = session.getUserId();
        tipoUsuario = session.getUserType();

        if (idUsuario == null || idUsuario.isEmpty()) {
            Toast.makeText(this, "Error de sesión. Vuelve a ingresar.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbarListarMascotas);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler_mascotas);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        txtVacio = findViewById(R.id.txt_lista_vacia);
        lyFiltros = findViewById(R.id.layout_filtros_refugio);
        btnEnAdopcion = findViewById(R.id.btn_filtro_en_adopcion);
        btnAdoptados = findViewById(R.id.btn_filtro_adoptados);

        if (esModoRefugio) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Gestionar Mis Mascotas");
            lyFiltros.setVisibility(View.VISIBLE);

            btnEnAdopcion.setOnClickListener(v -> {
                verAdoptadosRefugio = false;
                actualizarEstiloFiltros();
                filtrarYMostrarRefugio();
            });

            btnAdoptados.setOnClickListener(v -> {
                verAdoptadosRefugio = true;
                actualizarEstiloFiltros();
                filtrarYMostrarRefugio();
            });
            actualizarEstiloFiltros();
        } else {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Mascotas en Adopción");
            lyFiltros.setVisibility(View.GONE);
        }
        cargarDatosLocales();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (idUsuario != null) {
            cargarDatosLocales();
        }
    }

    private void cargarDatosLocales() {
        try {
            if (esModoRefugio) {
                if (idUsuario != null) {
                    listaCacheRefugio = dao.listarPorRefugio(idUsuario);
                    filtrarYMostrarRefugio();
                }
            } else {
                List<Mascota> lista = dao.listarDisponibles();
                mostrarListaEnRecycler(lista);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error cargando datos locales", Toast.LENGTH_SHORT).show();
        }
    }

    private void filtrarYMostrarRefugio() {
        if (listaCacheRefugio == null) return;

        List<Mascota> listaFiltrada = new ArrayList<>();
        for (Mascota m : listaCacheRefugio) {
            String estado = m.getEstado() != null ? m.getEstado() : "DISPONIBLE";

            if (verAdoptadosRefugio) {
                if ("ADOPTADO".equals(estado)) listaFiltrada.add(m);
            } else {
                if (!"ADOPTADO".equals(estado)) listaFiltrada.add(m);
            }
        }
        mostrarListaEnRecycler(listaFiltrada);
    }

    private void mostrarListaEnRecycler(List<Mascota> listaFinal) {
        if (listaFinal == null || listaFinal.isEmpty()) {
            recycler.setVisibility(View.GONE);
            txtVacio.setVisibility(View.VISIBLE);
            txtVacio.setText(esModoRefugio ? "No tienes mascotas en esta categoría" : "No hay mascotas disponibles 🐾");
        } else {
            recycler.setVisibility(View.VISIBLE);
            txtVacio.setVisibility(View.GONE);

            AdaptadorMascotas adapter = new AdaptadorMascotas(
                    listaFinal,
                    esModoRefugio,
                    this,
                    dao
            );
            recycler.setAdapter(adapter);
        }
    }

    private void actualizarEstiloFiltros() {
        if (verAdoptadosRefugio) {
            btnAdoptados.setAlpha(1.0f);
            btnEnAdopcion.setAlpha(0.5f);
        } else {
            btnEnAdopcion.setAlpha(1.0f);
            btnAdoptados.setAlpha(0.5f);
        }
    }
}
