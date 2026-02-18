package com.patitasalrescate.Controllers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.ApiRefugiosSimulada;
import com.patitasalrescate.accesoADatos.DAORefugio;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.ui.AdaptadorRefugios;

import java.util.List;

public class ActividadListarRefugios extends AppCompatActivity {

    private RecyclerView recycler;
    private DAORefugio dao;
    private TextView txtVacio;
    private ApiRefugiosSimulada apiSimulada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_listar_refugios);

        dao = new DAORefugio(this);
        apiSimulada = new ApiRefugiosSimulada();

        Toolbar toolbar = findViewById(R.id.toolbarListarRefugios);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler_refugios);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        txtVacio = findViewById(R.id.txt_refugios_vacio);

        cargarDatosLocal();
        sincronizarConApiSimulada();   // ← Llamamos a la API simulada
    }

    private void cargarDatosLocal() {
        List<Refugio> lista = dao.listarTodos();
        actualizarUI(lista);
    }

    private void actualizarUI(List<Refugio> lista) {
        if (lista.isEmpty()) {
            recycler.setVisibility(View.GONE);
            txtVacio.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            txtVacio.setVisibility(View.GONE);
            recycler.setAdapter(new AdaptadorRefugios(this, lista));
        }
    }

    private void sincronizarConApiSimulada() {
        new Thread(() -> {
            try {
                List<Refugio> refugiosApi = apiSimulada.getRefugios();

                if (refugiosApi != null && !refugiosApi.isEmpty()) {
                    Log.d("ApiRefugios", "Se obtuvieron " + refugiosApi.size() + " refugios de la API simulada");

                    for (Refugio r : refugiosApi) {
                        if (dao.obtenerPorId(r.getIdRefugio()) == null) {
                            dao.insertar(r);
                        } else {
                            dao.actualizar(r);
                        }
                    }

                    // Recargar la lista en el hilo principal
                    runOnUiThread(this::cargarDatosLocal);
                } else {
                    Log.e("ApiRefugios", "No se obtuvieron datos de la API simulada");
                }
            } catch (Exception e) {
                Log.e("ApiRefugios", "Error al conectar con API simulada: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "No se pudo conectar con la API simulada", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}