package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.patitasalrescate.R;
import com.patitasalrescate.controllers.auth.ActividadIngresar;
import com.patitasalrescate.controllers.feed.ActividadFeedAdoptante;
import com.patitasalrescate.controllers.feed.ActividadFeedRefugio;
import com.patitasalrescate.data_access.DAOAdoptante;
import com.patitasalrescate.model.Adoptante;
import com.patitasalrescate.utils.PatitasSessionManager;

public class ActividadPerfilUsuario extends AppCompatActivity {

    private DAOAdoptante daoAdoptante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_perfil_usuario);

        Toolbar toolbar = findViewById(R.id.toolbarPerfilUsuario);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        PatitasSessionManager sesion = PatitasSessionManager.getInstance(this);
        daoAdoptante = new DAOAdoptante(this);
        Adoptante adoptante = daoAdoptante.obtenerPorId(sesion.getUserId());

        TextView txtNombre = findViewById(R.id.txtNombreUsuario);
        TextView txtTipo = findViewById(R.id.txtTipoUsuario);
        TextView txtCorreo = findViewById(R.id.txtCorreoUsuario);
        TextView txtCelular = findViewById(R.id.txtCelularUsuario);

        txtTipo.setText(sesion.isRefugio() ? "Refugio" : "Adoptante");

        if (adoptante != null) {
            txtNombre.setText(adoptante.getNombre());
            txtCorreo.setText(adoptante.getCorreo());
            txtCelular.setText(adoptante.getNumCelular());
        }

        Button btnFavoritos = findViewById(R.id.btnVerFavoritos);
        btnFavoritos.setOnClickListener(v -> {
            Intent i = new Intent(this, ActividadFeedAdoptante.class);
            i.putExtra("navegarA", R.id.fragmentFavoritos);
            startActivity(i);
            finish();
        });

        Button btnCambiarModo = findViewById(R.id.btnCambiarModoRefugio);
        btnCambiarModo.setOnClickListener(v -> {
            Intent i = new Intent(this, ActividadFeedRefugio.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> {
            sesion.logout();
            Intent i = new Intent(this, ActividadIngresar.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}