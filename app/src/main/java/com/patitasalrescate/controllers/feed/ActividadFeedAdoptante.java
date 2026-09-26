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

import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.UserResponse;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.controllers.auth.ActividadIngresar;
import com.patitasalrescate.controllers.management.ActividadPerfilUsuario;

import androidx.activity.OnBackPressedCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadFeedAdoptante extends com.patitasalrescate.controllers.base.BaseActivity {
    private NavController navController;
    private Menu menuToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_feed_adoptante);
        if (!ApiApp.client().session.isAuthenticated()) {
            startActivity(new Intent(this, ActividadIngresar.class));
            finish();
            return;
        }

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

        // El botón volver del celular no regresa al modo anterior: minimiza la app.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_adoptante, menu);
        menuToolbar = menu;
        cargarAvatarToolbar();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (menuToolbar != null) cargarAvatarToolbar();
    }

    /** Si el usuario tiene foto, la pone como icono de "Mi perfil"; si no, queda ic_perfil. */
    private void cargarAvatarToolbar() {
        if (menuToolbar == null) return;
        MenuItem itemPerfil = menuToolbar.findItem(R.id.action_perfil);
        if (itemPerfil == null) return;
        ApiApp.client().admin.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null
                        || response.body().photoUrl == null || response.body().photoUrl.isEmpty()) return;
                int sizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32,
                        getResources().getDisplayMetrics());
                Glide.with(ActividadFeedAdoptante.this)
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
            @Override public void onFailure(Call<UserResponse> call, Throwable error) { }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            navigate(R.id.fragmentBusqueda);
            return true;
        }
        if (item.getItemId() == R.id.action_perfil) {
            startActivity(new Intent(this, ActividadPerfilUsuario.class));
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
