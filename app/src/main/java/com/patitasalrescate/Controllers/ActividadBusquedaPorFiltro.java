package com.patitasalrescate.Controllers;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAOMascota;
import com.patitasalrescate.model.Mascota;

import java.util.ArrayList;
import java.util.List;

public class ActividadBusquedaPorFiltro extends AppCompatActivity {

    private EditText txtFiltro;
    private Button btnBuscar;
    private ListView listResultados;
    private DAOMascota daoMascota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_busqueda_por_filtro);

        txtFiltro = findViewById(R.id.txtFiltro);
        btnBuscar = findViewById(R.id.btnBuscar);
        listResultados = findViewById(R.id.listResultados);

        daoMascota = new DAOMascota(this);

        btnBuscar.setOnClickListener(v -> buscar());
    }

    private void buscar() {
        String filtro = txtFiltro.getText().toString().toLowerCase();
        List<Mascota> mascotas = daoMascota.listarTodos();
        List<String> resultados = new ArrayList<>();

        for (Mascota m : mascotas) {
            if (m.getEspecie().toLowerCase().contains(filtro) ||
                    m.getRaza().toLowerCase().contains(filtro)) {

                resultados.add(m.getNombre() + " - " + m.getEspecie());
            }
        }

        listResultados.setAdapter(
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        resultados)
        );
    }
}
