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
import com.patitasalrescate.controllers.feed.ActividadInicioRefugio;
import com.patitasalrescate.data_access.DAOAdoptante;
import com.patitasalrescate.data_access.DAORefugio;
import com.patitasalrescate.model.Adoptante;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.utils.PatitasSessionManager;

public class ActividadIniciarSesion extends AppCompatActivity {
    private EditText textCorreo, textPassword;
    private Button button_Ingresar;
    private DAOAdoptante daoAdoptante;
    private DAORefugio daoRefugio;

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
        daoAdoptante = new DAOAdoptante(this);
        daoRefugio = new DAORefugio(this);
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

        Adoptante a = daoAdoptante.login(correo, pass);
        if (a != null) {
            PatitasSessionManager.getInstance(this).createSession(a.getIdAdoptante(), a.getNombre(), "ADOPTANTE");
            irAPantallaPrincipal();
            return;
        }

        Refugio r = daoRefugio.login(correo, pass);
        if (r != null) {
            PatitasSessionManager.getInstance(this).createSession(r.getIdRefugio(), r.getNombre(), "REFUGIO");
            irAPantallaPrincipal();
            return;
        }

        Toast.makeText(this, "Credenciales incorrectas (Demo: ad@demo.com / pass123)", Toast.LENGTH_SHORT).show();
    }

    public void irAPantallaPrincipal() {
        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        boolean esAdoptante = session.isAdoptante();
        Intent intent = new Intent(this, esAdoptante ? ActividadFeedAdoptante.class : ActividadInicioRefugio.class);
        startActivity(intent);
        finish();
    }
}
