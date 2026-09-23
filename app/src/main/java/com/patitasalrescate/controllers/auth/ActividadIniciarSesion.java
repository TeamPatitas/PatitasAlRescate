package com.patitasalrescate.controllers.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.patitasalrescate.R;
import com.patitasalrescate.controllers.feed.ActividadFeedAdoptante;
import com.patitasalrescate.controllers.feed.ActividadFeedRefugio;
import com.patitasalrescate.data.remote.dto.AuthResponse;
import com.patitasalrescate.data.remote.dto.LoginRequest;
import com.patitasalrescate.data.remote.dto.UserResponse;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.ApiErrors;
import com.patitasalrescate.utils.PatitasSessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadIniciarSesion extends AppCompatActivity {
    private EditText textCorreo, textPassword;
    private Button button_Ingresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ly_inicia_sesion);
        Toolbar toolbar = findViewById(R.id.tollbariniciarsesion);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        textCorreo = findViewById(R.id.rj_text_correr_inisesion);
        textPassword = findViewById(R.id.rj_text_pass_inisesion);
        button_Ingresar = findViewById(R.id.rj_button_ingresar_inisesion);
        button_Ingresar.setOnClickListener(v -> ejecutarLogin());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.iniciarsesion), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void ejecutarLogin() {
        String correo = textCorreo.getText().toString().trim();
        String pass = textPassword.getText().toString().trim();
        if (correo.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        button_Ingresar.setEnabled(false);
        LoginRequest request = new LoginRequest();
        request.email = correo;
        request.password = pass;
        ApiApp.client().auth.login(request).enqueue(new Callback<AuthResponse>() {
            @Override public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null || response.body().token == null) {
                    button_Ingresar.setEnabled(true);
                    Toast.makeText(ActividadIniciarSesion.this,
                            response.code() == 400 || response.code() == 401
                                    ? "Credenciales incorrectas"
                                    : "No se pudo iniciar sesión (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                ApiApp.client().session.authenticate(response.body());
                ApiApp.client().admin.getCurrentUser().enqueue(new Callback<UserResponse>() {
                    @Override public void onResponse(Call<UserResponse> call, Response<UserResponse> userResponse) {
                        if (isFinishing() || isDestroyed()) return;
                        button_Ingresar.setEnabled(true);
                        UserResponse user = userResponse.body();
                        if (userResponse.code() == 400) {
                            crearSesionLimitada(correo);
                            Toast.makeText(ActividadIniciarSesion.this,
                                    "Perfil no disponible (" + ApiErrors.describe(userResponse)
                                            + "); acceso limitado", Toast.LENGTH_LONG).show();
                            irAPantallaPrincipal();
                            return;
                        }
                        if (!userResponse.isSuccessful() || user == null || user.id == null) {
                            ApiApp.client().session.logout();
                            Toast.makeText(ActividadIniciarSesion.this,
                                    "No se pudo cargar el perfil (" + ApiErrors.describe(userResponse) + ")",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        String nombre = ((user.firstName == null ? "" : user.firstName) + " "
                                + (user.lastName == null ? "" : user.lastName)).trim();
                        boolean owner = user.roles != null && user.roles.contains("ShelterOwner") && user.shelterId != null;
                        PatitasSessionManager.getInstance(ActividadIniciarSesion.this)
                                .createSession(user.id, nombre, owner ? "REFUGIO" : "ADOPTANTE",
                                        user.shelterId == null ? "" : user.shelterId, owner);
                        irAPantallaPrincipal();
                    }
                    @Override public void onFailure(Call<UserResponse> call, Throwable error) {
                        if (isFinishing() || isDestroyed()) return;
                        button_Ingresar.setEnabled(true);
                        ApiApp.client().session.logout();
                        Toast.makeText(ActividadIniciarSesion.this,
                                "No se pudo cargar el perfil: " + error.getClass().getSimpleName(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override public void onFailure(Call<AuthResponse> call, Throwable error) {
                if (isFinishing() || isDestroyed()) return;
                button_Ingresar.setEnabled(true);
                Toast.makeText(ActividadIniciarSesion.this, "Sin conexión con la API", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void crearSesionLimitada(String correo) {
        String nombre = correo.contains("@") ? correo.substring(0, correo.indexOf('@')) : correo;
        PatitasSessionManager.getInstance(this).createSession("", nombre, "ADOPTANTE", "", false);
    }

    public void irAPantallaPrincipal() {
        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        Intent intent = new Intent(this, session.isRefugio() ? ActividadFeedRefugio.class : ActividadFeedAdoptante.class);
        startActivity(intent);
        finish();
    }
}
