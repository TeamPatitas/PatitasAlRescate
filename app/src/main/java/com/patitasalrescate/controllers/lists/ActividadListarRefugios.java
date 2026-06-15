package com.patitasalrescate.controllers.lists;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.patitasalrescate.data_access.ApiRefugiosSimulada;
import com.patitasalrescate.data_access.DAORefugio;
import com.patitasalrescate.data_access.SupabaseService;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.ui.AdaptadorRefugios;
import java.util.List;

public class ActividadListarRefugios extends AppCompatActivity {
    private RecyclerView recycler;
    private DAORefugio dao;
    private SupabaseService supabase;
    private ApiRefugiosSimulada apiSimulada;
    private TextView txtVacio;

    private static boolean yaSincronizado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_listar_refugios);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        dao = new DAORefugio(this);
        supabase = new SupabaseService();
        apiSimulada = new ApiRefugiosSimulada();

        Toolbar toolbar = findViewById(R.id.toolbarListarRefugios);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler_refugios);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        txtVacio = findViewById(R.id.txt_refugios_vacio);

        cargarDatosLocal();

        if (!yaSincronizado) {
            sincronizarFuentesExternas();
            yaSincronizado = true;
        }
    }

    private void cargarDatosLocal() {
        List<Refugio> lista = dao.listarTodos();
        actualizarUI(lista);
    }

    private void actualizarUI(List<Refugio> lista) {
        if (lista == null || lista.isEmpty()) {
            recycler.setVisibility(View.GONE);
            txtVacio.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            txtVacio.setVisibility(View.GONE);
            recycler.setAdapter(new AdaptadorRefugios(this, lista));
        }
    }

    private void sincronizarFuentesExternas() {
        new Thread(() -> {
            Log.d("RefugiosSync", "=== Iniciando sincronización completa ===");

            // Supabase
            try {
                List<Refugio> deSupabase = supabase.getRefugios();
                if (deSupabase != null) {
                    Log.d("RefugiosSync", "Supabase devolvió " + deSupabase.size() + " refugios");
                    for (Refugio r : deSupabase) {
                        sincronizarRefugioLocal(r);
                    }
                }
            } catch (Exception e) {
                Log.e("RefugiosSync", "Error Supabase: " + e.getMessage());
            }

            // API simulada (Render)
            try {
                List<Refugio> deApi = apiSimulada.getRefugios();
                if (deApi != null) {
                    Log.d("RefugiosSync", "API Render devolvió " + deApi.size() + " refugios");
                    for (Refugio r : deApi) {
                        sincronizarRefugioLocal(r);
                    }
                }
            } catch (Exception e) {
                Log.e("RefugiosSync", "Error API Render: " + e.getMessage());
            }

            runOnUiThread(() -> {
                cargarDatosLocal();
                Toast.makeText(this, "Sincronización completa", Toast.LENGTH_SHORT).show();
            });

        }).start();
    }

    private void sincronizarRefugioLocal(Refugio r) {
        if (r == null || r.getIdRefugio() == null || r.getIdRefugio().isEmpty()) return;

        if (dao.obtenerPorId(r.getIdRefugio()) == null) {
            dao.insertar(r);
            Log.d("RefugiosSync", "→ Insertado nuevo: " + r.getNombre());
        } else {
            dao.actualizar(r);
            Log.d("RefugiosSync", "→ Actualizado: " + r.getNombre());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        yaSincronizado = false;
    }
}