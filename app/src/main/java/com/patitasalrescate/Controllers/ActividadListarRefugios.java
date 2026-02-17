package com.patitasalrescate.Controllers;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAORefugio;
import com.patitasalrescate.accesoADatos.SupabaseService;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.ui.AdaptadorRefugios;
import java.util.List;

public class ActividadListarRefugios extends AppCompatActivity {

    private RecyclerView recycler;
    private DAORefugio dao;
    private SupabaseService supabase;
    private TextView txtVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_listar_refugios);

        dao = new DAORefugio(this);
        supabase = new SupabaseService();

        Toolbar toolbar = findViewById(R.id.toolbarListarRefugios);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler_refugios);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        txtVacio = findViewById(R.id.txt_refugios_vacio);

        cargarDatos();
        sincronizarConNube();
    }

    private void cargarDatos() {
        List<Refugio> lista = dao.listarTodos();
        if (lista.isEmpty()) {
            txtVacio.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
        } else {
            txtVacio.setVisibility(View.GONE);
            recycler.setVisibility(View.VISIBLE);
            recycler.setAdapter(new AdaptadorRefugios(this, lista));
        }
    }

    private void sincronizarConNube() {
        new Thread(() -> {
            try {
                List<Refugio> nube = supabase.getRefugios();
                if (nube != null) {
                    for (Refugio r : nube) {
                        if (dao.obtenerPorId(r.getIdRefugio()) == null) dao.insertar(r);
                        else dao.actualizar(r);
                    }
                    runOnUiThread(this::cargarDatos);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}