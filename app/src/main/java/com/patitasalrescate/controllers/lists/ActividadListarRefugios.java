package com.patitasalrescate.controllers.lists;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.patitasalrescate.R;
import com.patitasalrescate.data_access.DAORefugio;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.ui.AdaptadorRefugios;
import java.util.List;

public class ActividadListarRefugios extends AppCompatActivity {
    private RecyclerView recycler;
    private DAORefugio dao;
    private TextView txtVacio;

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
}
