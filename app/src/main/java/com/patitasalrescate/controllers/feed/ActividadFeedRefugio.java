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
import com.patitasalrescate.MainActivity;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.management.ActividadRegistrarMascota;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.controllers.lists.ActividadEventosLista;

public class ActividadFeedRefugio extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_inicio_refugio);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentFeedRefugio);
        if (navHost != null) {
            navController = navHost.getNavController();
            navController.navigate(R.id.fragmentInicioRefugio);
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        oBarra.setTitle("Refugio " + nombreRefugio);
        oBarra.setNavigationOnClickListener(v -> finish());

        BottomNavigationView oMenu = findViewById(R.id.menuInicioRefugio);
        oMenu.setOnItemSelectedListener(menuItem -> {
            Intent oIntento = null;
            if (menuItem.getItemId() == R.id.itemInicioRefugio) {
                NavDestination current = navController.getCurrentDestination();
                if (current != null && current.getId() != R.id.fragmentInicioRefugio) {
                    navController.navigate(R.id.fragmentInicioRefugio);
                }
                return true;
            }
            if (menuItem.getItemId() == R.id.itemRegistrarMascotaRefugio) {
                oIntento = new Intent(this, ActividadRegistrarMascota.class);
                startActivity(oIntento);
                return true;
            }
            if (menuItem.getItemId() == R.id.itemListarMacostaRefugio) {
                Bundle args = new Bundle();
                args.putBoolean("es_refugio_key", true);
                navController.navigate(R.id.fragmentListarMascotas, args);
                return true;
            }

            if (menuItem.getItemId() == R.id.itemEventosRefugio) {
                oIntento = new Intent(this, ActividadEventosLista.class);
                startActivity(oIntento);
                return true;
            }

            if (menuItem.getItemId() == R.id.itemSalirRefugio) {
                session.logout();
                oIntento = new Intent(this, MainActivity.class);
                oIntento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(oIntento);
                return true;
            }
            return false;
        });
    }
}