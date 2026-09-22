package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.auth.ActividadIngresar;
import com.patitasalrescate.controllers.feed.ActividadFeedAdoptante;
import com.patitasalrescate.data_access.DAORefugio;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.ui.AdaptadorEventos;
import com.patitasalrescate.utils.PatitasSessionManager;

import java.util.ArrayList;
import java.util.List;

public class ActividadPerfilRefugio extends AppCompatActivity {

    private ImageView imgFoto, imgQR;
    private TextView txtNombre, txtDireccion, txtContacto, txtNoEventos;
    private RecyclerView recyclerEventos;
    private DAORefugio daoRefugio;
    private String idRefugio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_perfil_refugio);

        idRefugio = getIntent().getStringExtra("id_refugio_key");
        daoRefugio = new DAORefugio(this);

        initViews();
        configToolbar();
        configListeners();

        if (idRefugio != null) {
            cargarDatosRefugio();
        } else {
            Toast.makeText(this, "Error al cargar refugio", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        imgFoto = findViewById(R.id.img_foto_refugio_perfil);
        imgQR = findViewById(R.id.img_qr_donacion);
        txtNombre = findViewById(R.id.txt_nombre_refugio_perfil);
        txtDireccion = findViewById(R.id.txt_direccion_refugio_perfil);
        txtContacto = findViewById(R.id.txt_contacto_refugio_perfil);
        txtNoEventos = findViewById(R.id.txt_no_eventos);
        recyclerEventos = findViewById(R.id.recycler_eventos_refugio);

        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));
    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarPerfilRefugio);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalles del Refugio");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void cargarDatosRefugio() {
        Refugio refugio = daoRefugio.obtenerPorId(idRefugio);
        if (refugio != null) {
            txtNombre.setText(refugio.getNombre());
            txtDireccion.setText(refugio.getDireccion());
            txtContacto.setText("Contacto: " + (refugio.getNumCelular() != null ? refugio.getNumCelular() : "No disponible"));

            if (refugio.getFotoUrl() != null && !refugio.getFotoUrl().isEmpty()) {
                Glide.with(this).load(refugio.getFotoUrl()).centerCrop().into(imgFoto);
            }

            // Cargando eventos de prueba
            cargarEventosPrueba();
            
            // Imagen QR por defecto (Simulando donación)
            imgQR.setImageResource(R.drawable.bg_circle_image); // Reemplazar con QR real si existe
        }
    }

    private void cargarEventosPrueba() {
        List<Evento> eventos = new ArrayList<>();
        eventos.add(new Evento("101", "Feria de Adopción Local", "12 Nov", "¡Te esperamos!", ""));
        eventos.add(new Evento("102", "Gran Colecta de Alimentos", "20 Nov", "Apóyanos con comida para los peluditos.", ""));

        if (eventos.isEmpty()) {
            txtNoEventos.setVisibility(View.VISIBLE);
            recyclerEventos.setVisibility(View.GONE);
        } else {
            txtNoEventos.setVisibility(View.GONE);
            recyclerEventos.setVisibility(View.VISIBLE);
            AdaptadorEventos adaptador = new AdaptadorEventos(eventos, this);
            recyclerEventos.setAdapter(adaptador);
        }
    }

    private void configListeners() {
        Button btnCambiarModo = findViewById(R.id.btnCambiarModoAdoptante);
        btnCambiarModo.setOnClickListener(v -> {
            startActivity(new Intent(this, ActividadFeedAdoptante.class));
            finish();
        });

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> {
            PatitasSessionManager.getInstance(this).logout();
            Intent intent = new Intent(this, ActividadIngresar.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}