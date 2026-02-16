package com.patitasalrescate.Controllers;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAORefugio;
import com.patitasalrescate.model.Refugio;

import java.util.ArrayList;
import java.util.List;

public class ActividadListarRefugios extends AppCompatActivity {

    private ListView listRefugios;
    private DAORefugio daoRefugio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_listar_refugios);

        listRefugios = findViewById(R.id.listRefugios);
        daoRefugio = new DAORefugio(this);

        List<Refugio> refugios = daoRefugio.listarTodos();
        List<String> datos = new ArrayList<>();

        for (Refugio r : refugios) {
            datos.add(r.getNombre() + "\n" + r.getDireccion());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                datos
        );

        listRefugios.setAdapter(adapter);
    }
}
