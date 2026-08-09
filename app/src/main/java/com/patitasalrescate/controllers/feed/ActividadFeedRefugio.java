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
import com.patitasalrescate.utils.PatitasSessionManager;

public class ActividadFeedRefugio extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_inicio_refugio);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentFeedRefugio);
        if (navHost != null) {
            navController = navHost.getNavController();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        String nombreRefugio = session.getUserName();

        if (nombreRefugio == null || nombreRefugio.isEmpty()) {
            nombreRefugio = "Refugio (Modo Prueba)";
        }

        Toolbar oBarra = findViewById(R.id.toolbarInicioRefugio);
        setSupportActionBar(oBarra);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (getSupportActionBar() != null) {
                if (destination.getId() == R.id.fragmentInicioRefugio) {
                    getSupportActionBar().setTitle("Refugio " + PatitasSessionManager.getInstance(this).getUserName());
                } else if (destination.getId() == R.id.fragmentListarMascotas) {
                    getSupportActionBar().setTitle("Mis Mascotas");
                } else if (destination.getId() == R.id.fragmentRegistrarMascota) {
                    getSupportActionBar().setTitle("Registrar Mascota");
                } else if (destination.getId() == R.id.fragmentEventosLista) {
                    getSupportActionBar().setTitle("Eventos");
                }
            }
        });

        oBarra.setNavigationOnClickListener(v -> finish());

        BottomNavigationView oMenu = findViewById(R.id.menuInicioRefugio);
        oMenu.setOnItemSelectedListener(menuItem -> {
            Intent oIntento = null;
            if (menuItem.getItemId() == R.id.itemInicioRefugio) {
                navigate(R.id.fragmentInicioRefugio);
                return true;
            }
            if (menuItem.getItemId() == R.id.itemRegistrarMascotaRefugio) {
                navigate(R.id.fragmentRegistrarMascota);
                return true;
            }
            if (menuItem.getItemId() == R.id.itemListarMacostaRefugio) {
                Bundle args = new Bundle();
                args.putBoolean("es_refugio_key", true);
                navigate(R.id.fragmentListarMascotas, args);
                return true;
            }

            if (menuItem.getItemId() == R.id.itemEventosRefugio) {
                navigate(R.id.fragmentEventosLista);
                return true;
            }

            return false;
        });
    }

    private void navigate(int id) {
        navigate(id, null);
    }

    private void navigate(int id, Bundle args) {
        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() != id) {
            navController.navigate(id, args);
        }
    }
}