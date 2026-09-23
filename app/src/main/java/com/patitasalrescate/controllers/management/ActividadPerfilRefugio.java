package com.patitasalrescate.controllers.management;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.data.mock.DAORefugio;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.ui.AdaptadorEventos;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.ApiPages;
import com.patitasalrescate.data.remote.dto.ShelterResponse;
import com.patitasalrescate.data.remote.dto.EventSummaryResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        ApiApp.client().shelters.getShelterById(idRefugio).enqueue(new Callback<ShelterResponse>() {
            @Override public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ActividadPerfilRefugio.this, "No se pudo cargar el refugio", Toast.LENGTH_SHORT).show();
                    return;
                }
                Refugio refugio = ApiApp.shelter(response.body());
            txtNombre.setText(refugio.getNombre());
            txtDireccion.setText(refugio.getDireccion());
            txtContacto.setText("Contacto: no disponible en la API");

            if (refugio.getFotoUrl() != null && !refugio.getFotoUrl().isEmpty()) {
                Glide.with(ActividadPerfilRefugio.this).load(refugio.getFotoUrl())
                        .placeholder(R.drawable.img_default_refugio)
                        .error(R.drawable.img_default_refugio)
                        .centerCrop().into(imgFoto);
            } else {
                imgFoto.setImageResource(R.drawable.img_default_refugio);
            }

            cargarEventos();
            
            // Imagen QR por defecto (Yape)
            imgQR.setImageResource(R.drawable.img_yape_default);
            }
            @Override public void onFailure(Call<ShelterResponse> call, Throwable error) {
                if (!isFinishing()) Toast.makeText(ActividadPerfilRefugio.this, "Sin conexión con refugios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarEventos(List<Evento> eventos) {
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

    private void cargarEventos() {
        ApiPages.load(page -> ApiApp.client().events.getAllEvents(page, 50),
                data -> data.totalPages, data -> data.items,
                items -> {
                    if (isFinishing() || isDestroyed()) return;
                    List<Evento> eventos = new ArrayList<>();
                    for (EventSummaryResponse item : items)
                        if (idRefugio.equals(item.shelterId)) eventos.add(ApiApp.event(item));
                    mostrarEventos(eventos);
                }, error -> {
                    if (!isFinishing()) txtNoEventos.setText("No se pudieron cargar los eventos");
                });
    }
}
