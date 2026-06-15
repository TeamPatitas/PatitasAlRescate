package com.patitasalrescate.controllers.lists;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.controllers.auth.ActividadIniciarSesion;
import com.patitasalrescate.data_access.DAOMascota;
import com.patitasalrescate.data_access.SupabaseService;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.ui.AdaptadorMascotas;

import java.util.ArrayList;
import java.util.List;

public class ActividadBusquedaPorFiltro extends AppCompatActivity {
    private Spinner spFiltro;
    private EditText txtFiltro;
    private Button btnBuscar;
    private RecyclerView recycler;
    private DAOMascota daoMascota;
    private SupabaseService supabase;

    private String idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_busqueda_por_filtro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.busquedafiltro), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spFiltro = findViewById(R.id.spFiltro);
        txtFiltro = findViewById(R.id.txtFiltro);
        btnBuscar = findViewById(R.id.btnBuscar);
        recycler = findViewById(R.id.recycler_mascotas);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        daoMascota = new DAOMascota(this);
        supabase = new SupabaseService();

        idUsuario = getIntent().getStringExtra(
                ActividadIniciarSesion.EXTRA_ID_USUARIO
        );
        ArrayAdapter<String> adapterSpinner =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        new String[]{"Nombre","Especie","Raza","Sexo"}
                );
        spFiltro.setAdapter(adapterSpinner);

        btnBuscar.setOnClickListener(v -> buscar());
    }
    private void buscar() {
        String texto =
                txtFiltro.getText().toString().toLowerCase();
        String tipoFiltro =
                spFiltro.getSelectedItem().toString();
        List<Mascota> lista = daoMascota.listarTodos();
        List<Mascota> resultados = new ArrayList<>();
        for (Mascota m : lista) {
            boolean coincide = false;
            switch (tipoFiltro) {
                case "Nombre":
                    coincide = m.getNombre()
                            .toLowerCase()
                            .contains(texto);
                    break;
                case "Especie":
                    coincide = m.getEspecie()
                            .toLowerCase()
                            .contains(texto);
                    break;
                case "Raza":
                    coincide = m.getRaza()
                            .toLowerCase()
                            .contains(texto);
                    break;
                case "Sexo":
                    coincide = m.getSexo()
                            .toLowerCase()
                            .contains(texto);
                    break;
            }
            if (coincide)
                resultados.add(m);
        }
        AdaptadorMascotas adapter =
                new AdaptadorMascotas(
                        resultados,
                        false,
                        this,
                        idUsuario,
                        "ADOPTANTE",
                        daoMascota,
                        supabase
                );
        recycler.setAdapter(adapter);
    }
}