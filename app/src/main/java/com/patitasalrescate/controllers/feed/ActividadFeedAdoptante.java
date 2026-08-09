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
import androidx.navigation.NavOptions;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.patitasalrescate.R;
import com.patitasalrescate.utils.PatitasSessionManager;

public class ActividadFeedAdoptante extends AppCompatActivity {
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_feed_adoptante);

        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentFeedAdoptante);

        if(navHost != null) navController = navHost.getNavController();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarInicioAdoptante);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                
                if (destination.getId() == R.id.fragmentInicioAdoptante) {
                    getSupportActionBar().setTitle("Inicio");
                } else if (destination.getId() == R.id.fragmentListarMascotas) {
                    getSupportActionBar().setTitle("Mascotas");
                } else if (destination.getId() == R.id.fragmentListarRefugios) {
                    getSupportActionBar().setTitle("Refugios");
                } else if (destination.getId() == R.id.fragmentEventosLista) {
                    getSupportActionBar().setTitle("Eventos");
                }
            }
        });

        BottomNavigationView menu = findViewById(R.id.menuInicioAdoptante);
        menu.setOnItemSelectedListener(item -> {
            Intent i;
            if (item.getItemId() == R.id.itemInicioAdoptante) {
                navigate(R.id.fragmentInicioAdoptante);
                return true;
            }

            if (item.getItemId() == R.id.itemListarMascotasAdoptante) {
                navigate(R.id.fragmentListarMascotas);
                return true;
            }

            if(item.getItemId()==R.id.itemListarRefugios){
                navigate(R.id.fragmentListarRefugios);
                return true;
            }

            if (item.getItemId() == R.id.itemEventosAdoptante) {
                navigate(R.id.fragmentEventosLista);
                return true;
            }

            return false;
        });

        int destinoExtra = getIntent().getIntExtra("navegarA", -1);
        if (destinoExtra != -1) {
            navigate(destinoExtra);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_adoptante, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            navigate(R.id.fragmentBusqueda);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void navigate(int id) {
        NavDestination destinoActual = navController.getCurrentDestination();

        if (destinoActual != null && destinoActual.getId() != id) {
            NavOptions opciones = new NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();
            navController.navigate(id, null, opciones);
        }
    }
}