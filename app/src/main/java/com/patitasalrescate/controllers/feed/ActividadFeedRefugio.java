package com.patitasalrescate.controllers.feed;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.ShelterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.patitasalrescate.controllers.management.ActividadPerfilRefugio;
import com.patitasalrescate.controllers.management.ActividadPerfilUsuario;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.controllers.auth.ActividadIngresar;

public class ActividadFeedRefugio extends com.patitasalrescate.controllers.base.BaseActivity {

    private NavController navController;
    private Menu menuToolbar;
    private String nombreRefugio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_inicio_refugio);
        if (!ApiApp.client().session.isAuthenticated()) {
            startActivity(new Intent(this, ActividadIngresar.class));
            finish();
            return;
        }

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
                    getSupportActionBar().setTitle(tituloInicio());
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

        // El botón volver del celular no regresa al modo anterior: minimiza la app.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_refugio, menu);
        menuToolbar = menu;
        cargarAvatarToolbar();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (menuToolbar != null) cargarAvatarToolbar();
    }

    private String tituloInicio() {
        if (nombreRefugio != null && !nombreRefugio.isEmpty()) return "Refugio " + nombreRefugio;
        String respaldo = PatitasSessionManager.getInstance(this).getUserName();
        if (respaldo == null) respaldo = "";
        return "Refugio " + respaldo.trim();
    }

    /** Pone la foto del refugio como icono de "Mi perfil"; sin foto queda ic_perfil. */
    private void cargarAvatarToolbar() {
        if (menuToolbar == null) return;
        MenuItem itemPerfil = menuToolbar.findItem(R.id.action_perfil_refugio);
        if (itemPerfil == null) return;
        String shelterId = PatitasSessionManager.getInstance(this).getShelterId();
        if (shelterId == null || shelterId.isEmpty()) return;
        ApiApp.client().shelters.getShelterById(shelterId).enqueue(new Callback<ShelterResponse>() {
            @Override public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) return;
                if (response.body().name != null && !response.body().name.isEmpty()) {
                    nombreRefugio = response.body().name;
                    if (navController != null && navController.getCurrentDestination() != null
                            && navController.getCurrentDestination().getId() == R.id.fragmentInicioRefugio
                            && getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(tituloInicio());
                    }
                }
                if (response.body().photoUrl == null || response.body().photoUrl.isEmpty()) return;
                int sizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32,
                        getResources().getDisplayMetrics());
                Glide.with(ActividadFeedRefugio.this)
                        .load(response.body().photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_perfil)
                        .into(new CustomTarget<Drawable>(sizePx, sizePx) {
                            @Override public void onResourceReady(@NonNull Drawable resource,
                                    @Nullable Transition<? super Drawable> transition) {
                                itemPerfil.setIcon(resource);
                            }
                            @Override public void onLoadCleared(@Nullable Drawable placeholder) { }
                        });
            }
            @Override public void onFailure(Call<ShelterResponse> call, Throwable error) { }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_perfil_refugio) {
            // BUG FIX: se pasaba getUserId() (id de persona) y GET shelter/{id} devolvía 404.
            String idRefugio = PatitasSessionManager.getInstance(this).getShelterId();
            if (idRefugio == null || idRefugio.isEmpty()) {
                android.widget.Toast.makeText(this, "Aún no tienes un refugio asignado",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(this, ActividadPerfilRefugio.class);
            intent.putExtra("id_refugio_key", idRefugio);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
