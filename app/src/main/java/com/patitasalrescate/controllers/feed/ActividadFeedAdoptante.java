package com.patitasalrescate.controllers.feed;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.lists.ActividadBusquedaPorFiltro;
import com.patitasalrescate.controllers.lists.ActividadListarMascotas;
import com.patitasalrescate.controllers.lists.ActividadListarRefugios;
import com.patitasalrescate.controllers.lists.ActividadMisFavoritos;
import com.patitasalrescate.controllers.auth.ActividadIniciarSesion;

import com.patitasalrescate.controllers.lists.ActividadEventosLista;

public class ActividadFeedAdoptante extends AppCompatActivity {
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_feed_adoptante);
        String nombreAdoptante = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_NOMBRE_USUARIO);
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentFeedAdoptante);

        if(navHost != null) navController = navHost.getNavController();


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

            if (item.getItemId() == R.id.itemEventosAdoptante) {
                i = new Intent(this, ActividadEventosLista.class);
                startActivity(i);
                return true;
            }

            return false;
        });
    }
}