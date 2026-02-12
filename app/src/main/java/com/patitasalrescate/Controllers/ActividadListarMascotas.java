package com.patitasalrescate.Controllers;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAOMascota;
import com.patitasalrescate.accesoADatos.SupabaseService; // Importar
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.ui.AdaptadorMascotas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActividadListarMascotas extends AppCompatActivity {

    private RecyclerView recyclerMascotas;
    private TextView txtListaVacia;
    private DAOMascota daoMascota;
    private AdaptadorMascotas adaptador;
    private List<Mascota> listaMascotas;
    private SupabaseService supabaseService; // Instancia

    private boolean esModoRefugio = false;
    private String tipoUsuario = null; // "ADOPTANTE" | "REFUGIO"
    private String idUsuario = null;
    private String nombreUsuario = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_listar_mascotas);

        Toolbar toolbar = findViewById(R.id.toolbarListarMascotas);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerMascotas = findViewById(R.id.recycler_mascotas);
        txtListaVacia = findViewById(R.id.txt_lista_vacia);
        recyclerMascotas.setLayoutManager(new LinearLayoutManager(this));

        daoMascota = new DAOMascota(this);
        supabaseService = new SupabaseService(); // Inicializar
        listaMascotas = new ArrayList<>();

        if (getIntent().hasExtra("es_refugio_key")) {
            esModoRefugio = getIntent().getBooleanExtra("es_refugio_key", false);
        }

        // Extras estándar (vienen desde InicioAdoptante/InicioRefugio)
        tipoUsuario = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_TIPO_USUARIO);
        idUsuario = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO);
        nombreUsuario = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_NOMBRE_USUARIO);

        if (esModoRefugio) {
            getSupportActionBar().setTitle("Gestionar Mascotas");
        } else {
            getSupportActionBar().setTitle("Adopta un Amigo");
        }

        // Carga inicial local
        cargarListaLocal();

        // Sincronizar con la nube (NUEVO)
        sincronizarConNube();
    }

    private void cargarListaLocal() {
        listaMascotas = daoMascota.listarTodos();
        actualizarUI(listaMascotas);
    }

    private void actualizarUI(List<Mascota> lista) {
        if (lista.isEmpty()) {
            recyclerMascotas.setVisibility(View.GONE);
            txtListaVacia.setVisibility(View.VISIBLE);
        } else {
            recyclerMascotas.setVisibility(View.VISIBLE);
            txtListaVacia.setVisibility(View.GONE);
            adaptador = new AdaptadorMascotas(lista, esModoRefugio, tipoUsuario, idUsuario, nombreUsuario);
            recyclerMascotas.setAdapter(adaptador);
        }
    }

    // Método para bajar datos de Supabase y guardarlos en local
    private void sincronizarConNube() {
        new Thread(() -> {
            try {
                // 1. Obtener de la nube
                List<Mascota> mascotasRemotas = supabaseService.getMascotas();

                if (mascotasRemotas != null) {
                    // 2. Guardar en SQLite (Sincronización)
                    for (Mascota m : mascotasRemotas) {
                        // Verificamos si ya existe en local para actualizar o insertar
                        Mascota local = daoMascota.obtenerPorId(m.getIdMascota());
                        if (local == null) {
                            daoMascota.insertar(m);
                        } else {
                            // Opcional: Podrías verificar last_sync para ver cual es más nuevo
                            // Por ahora, actualizamos con lo que viene de la nube
                            daoMascota.actualizar(m);
                        }
                    }

                    // 3. Recargar la lista en el Hilo Principal
                    runOnUiThread(() -> {
                        cargarListaLocal();
                        // Toast.makeText(this, "Lista actualizada de la nube", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarListaLocal(); // Recargar por si volvimos de Editar
    }
}