package com.patitasalrescate.Controllers;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAOFavoritos;
import com.patitasalrescate.model.Mascota;

import java.util.ArrayList;
import java.util.List;

public class ActividadMisFavoritos extends AppCompatActivity {

    private ListView listFavoritos;
    private DAOFavoritos daoFavoritos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_mis_favoritos);

        listFavoritos = findViewById(R.id.listFavoritos);
        daoFavoritos = new DAOFavoritos(this);

        String idAdoptante = "ID_ADOPTANTE_DE_PRUEBA"; // luego se pasa real

        List<Mascota> favoritos = daoFavoritos.getFavoritosPorAdoptante(idAdoptante);
        List<String> datos = new ArrayList<>();

        for (Mascota m : favoritos) {
            datos.add(m.getNombre() + " - " + m.getEspecie());
        }

        listFavoritos.setAdapter(
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        datos)
        );
    }
}
