package com.patitasalrescate.Controllers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAOMascota;
import com.patitasalrescate.accesoADatos.DAORefugio;
import com.patitasalrescate.accesoADatos.SupabaseService;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.ui.AdaptadorMascotas;

import java.util.ArrayList;
import java.util.List;

public class ActividadListarMascotas extends AppCompatActivity {
    private RecyclerView recycler;
    private TextView txtVacio;
    private LinearLayout lyFiltros;
    private Button btnEnAdopcion, btnAdoptados;
    private DAORefugio daoRefugio;
    private DAOMascota dao;
    private SupabaseService supabase;
    private List<Mascota> listaCacheRefugio;
    private boolean esModoRefugio = false;
    private String idUsuario;
    private String tipoUsuario;
    private boolean verAdoptadosRefugio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_listar_mascotas);

        dao = new DAOMascota(this);
        supabase = new SupabaseService();
        daoRefugio = new DAORefugio(this);
        listaCacheRefugio = new ArrayList<>();

        if (getIntent().hasExtra("es_refugio_key")) {
            esModoRefugio = getIntent().getBooleanExtra("es_refugio_key", false);
        }
        idUsuario = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO);
        tipoUsuario = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_TIPO_USUARIO);

        if (idUsuario == null) {
            SharedPreferences prefs = getSharedPreferences("sesion_refugio", MODE_PRIVATE);
            idUsuario = prefs.getString("id_refugio", null);

            if (idUsuario == null) {
                SharedPreferences prefsAdopt = getSharedPreferences("sesion_adoptante", MODE_PRIVATE);
                idUsuario = prefsAdopt.getString("id_adoptante", null);
            }
        }

        if (idUsuario == null && esModoRefugio) {
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
        sincronizarConNube();
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
                    idUsuario,
                    tipoUsuario,
                    dao,
                    supabase
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

    private void sincronizarConNube() { //sincronizaciones
        new Thread(() -> {
            try {
                List<com.patitasalrescate.model.Refugio> refugiosNube = supabase.getRefugios();
                if (refugiosNube != null) {
                    com.patitasalrescate.accesoADatos.DAORefugio daoRefugio = new com.patitasalrescate.accesoADatos.DAORefugio(this);
                    for (com.patitasalrescate.model.Refugio r : refugiosNube) {
                        if (daoRefugio.obtenerPorId(r.getIdRefugio()) == null) {
                            daoRefugio.insertar(r);
                        } else {
                            daoRefugio.actualizar(r);
                        }
                    }
                }
                List<Mascota> nube = supabase.getMascotas();
                if (nube != null && !nube.isEmpty()) {
                    for (Mascota m : nube) {
                        if (m.getIdMascota() == null) continue;

                        Mascota local = dao.obtenerPorId(m.getIdMascota());
                        if (local == null) {
                            dao.insertar(m);
                        } else {
                            dao.actualizar(m);
                        }
                    }

                    runOnUiThread(this::cargarDatosLocales);
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error de sincronización", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}