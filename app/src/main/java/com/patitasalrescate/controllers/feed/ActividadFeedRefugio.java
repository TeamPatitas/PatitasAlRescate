package com.patitasalrescate.controllers.feed;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.patitasalrescate.controllers.management.ActividadPerfilRefugio;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.controllers.auth.ActividadIngresar;

public class ActividadFeedRefugio extends com.patitasalrescate.controllers.base.BaseActivity {

    private static final int[] DESTINOS_MENU = {
            R.id.fragmentInicioRefugio,
            R.id.fragmentListarMascotas,
            R.id.fragmentEventosLista,
            R.id.fragmentFormsRefugio
    };

    private static final int[] ITEMS_MENU = {
            R.id.itemInicioRefugio,
            R.id.itemListarMacostaRefugio,
            R.id.itemEventosRefugio,
            R.id.itemFormsRefugio
    };

    private NavController navController;
    private BottomNavigationView oMenu;
    private Menu menuToolbar;
    private String nombreRefugio;
    private GestureDetector gestureDetector;

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

        oMenu = findViewById(R.id.menuInicioRefugio);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (getSupportActionBar() != null) {
                if (destination.getId() == R.id.fragmentInicioRefugio) {
                    getSupportActionBar().setTitle(tituloInicio());
                } else if (destination.getId() == R.id.fragmentListarMascotas) {
                    getSupportActionBar().setTitle("Mis Mascotas");
                } else if (destination.getId() == R.id.fragmentRegistrarMascota) {
                    getSupportActionBar().setTitle("Nueva Mascota");
                } else if (destination.getId() == R.id.fragmentRegistrarEvento) {
                    getSupportActionBar().setTitle("Nuevo Evento");
                } else if (destination.getId() == R.id.fragmentEventosLista) {
                    getSupportActionBar().setTitle("Mis Eventos");
                } else if (destination.getId() == R.id.fragmentFormsRefugio) {
                    getSupportActionBar().setTitle("Forms");
                }
            }
            int idx = indiceDeDestino(destination.getId());
            if (idx != -1 && oMenu != null) {
                MenuItem item = oMenu.getMenu().findItem(ITEMS_MENU[idx]);
                if (item != null && !item.isChecked()) {
                    item.setChecked(true);
                }
            }
        });

        oBarra.setNavigationOnClickListener(v -> finish());

        // Volver navega entre fragments; solo minimiza si ya está en el inicio (no hay
        // modo anterior al que regresar gracias al CLEAR_TASK del cambio de modo).
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (navController == null || !navController.popBackStack()) {
                    moveTaskToBack(true);
                }
            }
        });

        oMenu.setOnItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.itemInicioRefugio) {
                navigate(R.id.fragmentInicioRefugio);
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

            if (menuItem.getItemId() == R.id.itemFormsRefugio) {
                navigate(R.id.fragmentFormsRefugio);
                return true;
            }

            return false;
        });

        configurarGestoDeslizamiento();
    }

    private void configurarGestoDeslizamiento() {
        float density = getResources().getDisplayMetrics().density;
        final float distanciaMinima = 70f * density;
        final float velocidadMinima = 100f * density;

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                                   float velocityX, float velocityY) {
                if (e1 == null || navController == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY) * 1.4f
                        && Math.abs(diffX) > distanciaMinima
                        && Math.abs(velocityX) > velocidadMinima) {
                    NavDestination actual = navController.getCurrentDestination();
                    if (actual == null) return false;
                    int idx = indiceDeDestino(actual.getId());
                    if (idx == -1) return false;

                    if (diffX < 0 && idx < ITEMS_MENU.length - 1) {
                        oMenu.setSelectedItemId(ITEMS_MENU[idx + 1]);
                        return true;
                    } else if (diffX > 0 && idx > 0) {
                        oMenu.setSelectedItemId(ITEMS_MENU[idx - 1]);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    private int indiceDeDestino(int destinationId) {
        for (int i = 0; i < DESTINOS_MENU.length; i++) {
            if (DESTINOS_MENU[i] == destinationId) return i;
        }
        return -1;
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
            int idxActual = indiceDeDestino(current.getId());
            int idxNuevo = indiceDeDestino(id);
            boolean haciaIzquierda = idxActual != -1 && idxNuevo != -1 && idxNuevo < idxActual;

            NavOptions opciones = new NavOptions.Builder()
                    .setEnterAnim(haciaIzquierda ? R.anim.slide_in_left : R.anim.slide_in_right)
                    .setExitAnim(haciaIzquierda ? R.anim.slide_out_right : R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();
            navController.navigate(id, args, opciones);
        }
    }
}
