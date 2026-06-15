package com.patitasalrescate.controllers.feed;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.patitasalrescate.MainActivity;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.lists.ActividadBusquedaPorFiltro;
import com.patitasalrescate.controllers.lists.ActividadListarMascotas;
import com.patitasalrescate.controllers.lists.ActividadListarRefugios;
import com.patitasalrescate.controllers.lists.ActividadMisFavoritos;
import com.patitasalrescate.controllers.auth.ActividadIniciarSesion;

public class ActividadInicioAdoptante extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_inicio_adoptante);
        String nombreAdoptante = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_NOMBRE_USUARIO);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        if (nombreAdoptante == null || nombreAdoptante.trim().isEmpty()) {
            nombreAdoptante = getIntent().getStringExtra("nombre_adoptante_key");
        }

        if (nombreAdoptante == null || nombreAdoptante.isEmpty()) {
            nombreAdoptante = "Adoptante (Modo Prueba)";
        }

        String idAdoptante = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO);

        Toolbar toolbar = findViewById(R.id.toolbarInicioAdoptante);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setTitle("Adoptante " + nombreAdoptante);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView txt = findViewById(R.id.txtBienvenidoAdoptante);
        txt.setText("Bienvenido " + nombreAdoptante);
        txt.setGravity(Gravity.CENTER);
        BottomNavigationView menu = findViewById(R.id.menuInicioAdoptante);
        String finalNombreAdoptante = nombreAdoptante;
        String finalIdAdoptante = idAdoptante;
        menu.setOnItemSelectedListener(item -> {
            Intent i;
            if (item.getItemId() == R.id.itemInicioAdoptante) return true;
            if (item.getItemId() == R.id.itemListarMascotasAdoptante) {
                i = new Intent(this, ActividadListarMascotas.class);
                i.putExtra("es_refugio_key", false);
                i.putExtra("nombre_adoptante_key", finalNombreAdoptante);
                i.putExtra(ActividadIniciarSesion.EXTRA_TIPO_USUARIO, "ADOPTANTE");
                i.putExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO, finalIdAdoptante);
                i.putExtra(ActividadIniciarSesion.EXTRA_NOMBRE_USUARIO, finalNombreAdoptante);
                startActivity(i);
                return true;
            }
            if(item.getItemId()==R.id.itemListarRefugios){
                i= new Intent(this, ActividadListarRefugios.class);
                startActivity(i);
                return true;
            }
            if(item.getItemId()==R.id.itemBuscarFiltro){
                i= new Intent(this, ActividadBusquedaPorFiltro.class);
                startActivity(i);
                return true;
            }
            if (item.getItemId() == R.id.itemFavoritosAdoptante) {
                i = new Intent(this, ActividadMisFavoritos.class);
                i.putExtra(ActividadIniciarSesion.EXTRA_TIPO_USUARIO, "ADOPTANTE");
                i.putExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO, finalIdAdoptante);
                startActivity(i);
                return true;
            }

            if (item.getItemId() == R.id.itemSalirAdoptante) {
                i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                return true;
            }
            return false;
        });
    }
}