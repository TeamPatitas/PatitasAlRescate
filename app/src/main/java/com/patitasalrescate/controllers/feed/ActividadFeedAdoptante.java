package com.patitasalrescate.controllers.feed;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.lists.ActividadBusquedaPorFiltro;
import com.patitasalrescate.controllers.lists.ActividadListarRefugios;
import com.patitasalrescate.controllers.lists.ActividadMisFavoritos;

import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.controllers.lists.ActividadEventosLista;

public class ActividadFeedAdoptante extends AppCompatActivity {
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_feed_adoptante);

        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        String nombreAdoptante = session.getUserName();
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentFeedAdoptante);

        if(navHost != null) navController = navHost.getNavController();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (nombreAdoptante == null || nombreAdoptante.isEmpty()) {
            nombreAdoptante = "Adoptante Paraguayo";
        }

        Toolbar toolbar = findViewById(R.id.toolbarInicioAdoptante);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setTitle("Adoptante " + nombreAdoptante);
        toolbar.setNavigationOnClickListener(v -> finish());

        BottomNavigationView menu = findViewById(R.id.menuInicioAdoptante);
        menu.setOnItemSelectedListener(item -> {
            Intent i;
            if (item.getItemId() == R.id.itemInicioAdoptante) {
                NavDestination destinoActual = navController.getCurrentDestination();

                if (destinoActual != null && destinoActual.getId() != R.id.fragmentInicioAdoptante) {
                    navController.navigate(R.id.fragmentInicioAdoptante);
                }
                return true;
            }
            if (item.getItemId() == R.id.itemListarMascotasAdoptante) {
                Bundle args = new Bundle();
                args.putBoolean("es_refugio_key", false);
                navController.navigate(R.id.fragmentListarMascotas, args);
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