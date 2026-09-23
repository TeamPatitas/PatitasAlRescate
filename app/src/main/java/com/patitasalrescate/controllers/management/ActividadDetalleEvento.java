package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.patitasalrescate.R;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.data.remote.dto.EventResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadDetalleEvento extends AppCompatActivity {

    private ImageView imgDetalle;
    private TextView txtNombre, txtFecha, txtDescripcion;
    private Button btnMapa;
    private FloatingActionButton fabEditar;
    private Evento evento;
    private boolean puedeEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_detalle_evento);

        imgDetalle = findViewById(R.id.img_detalle_evento);
        txtNombre = findViewById(R.id.txt_detalle_nombre_evento);
        txtFecha = findViewById(R.id.txt_detalle_fecha_evento);
        txtDescripcion = findViewById(R.id.txt_detalle_descripcion_evento);
        btnMapa = findViewById(R.id.btn_ver_mapa_evento);
        fabEditar = findViewById(R.id.fab_editar_evento);

        configToolbar();

        evento = (Evento) getIntent().getSerializableExtra("evento_key");

        if (evento != null) {
            txtNombre.setText(evento.getNombre());
            txtFecha.setText(evento.getFecha());
            txtDescripcion.setText(evento.getDescripcion());

            if (evento.getFotoUrl() != null && !evento.getFotoUrl().isEmpty()) {
                Glide.with(this)
                        .load(evento.getFotoUrl())
                        .placeholder(R.drawable.evento_default)
                        .error(R.drawable.evento_default)
                        .centerCrop()
                        .into(imgDetalle);
            } else {
                imgDetalle.setImageResource(R.drawable.evento_default);
            }

            btnMapa.setOnClickListener(v -> verEnMapa());
          
            cargarDatos();
        } else {
            Toast.makeText(this, "Error al cargar el evento", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarDetalleEvento);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // Título se maneja en CollapsingToolbarLayout
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void cargarDatos() {
        txtNombre.setText(evento.getNombre());
        txtFecha.setText(evento.getFecha());
        txtDescripcion.setText(evento.getDescripcion());

        if (evento.getFotoUrl() != null && !evento.getFotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(evento.getFotoUrl())
                    .centerCrop()
                    .into(imgDetalle);
        } else {
            imgDetalle.setImageResource(R.drawable.eventos);
        }

        btnMapa.setOnClickListener(v -> verEnMapa());
    }

    private void configurarSegunRol() {
        fabEditar.setVisibility(puedeEditar ? View.VISIBLE : View.GONE);
        fabEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActividadRegistrarEvento.class);
            intent.putExtra("evento_editar_key", evento);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (evento != null) cargarEventoApi();
    }

    private void cargarEventoApi() {
        ApiApp.client().events.getEventById(evento.getIdEvento()).enqueue(new Callback<EventResponse>() {
            @Override public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ActividadDetalleEvento.this, "No se pudo cargar el evento", Toast.LENGTH_SHORT).show();
                    return;
                }
                evento = ApiApp.event(response.body());
                puedeEditar = Boolean.TRUE.equals(response.body().isYours);
                invalidateOptionsMenu();
                cargarDatos();
                configurarSegunRol();
            }
            @Override public void onFailure(Call<EventResponse> call, Throwable error) {
                if (!isFinishing()) Toast.makeText(ActividadDetalleEvento.this, "Sin conexión con eventos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        if (puedeEditar) menu.add(0, 9001, 0, "Eliminar evento");
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 9001 && evento != null && puedeEditar) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setMessage("¿Eliminar este evento de forma permanente?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Eliminar", (dialog, which) -> eliminarEvento())
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void eliminarEvento() {
        ApiApp.client().events.deleteEvent(evento.getIdEvento()).enqueue(new Callback<String>() {
            @Override public void onResponse(Call<String> call, Response<String> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(ActividadDetalleEvento.this, "Evento eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                } else Toast.makeText(ActividadDetalleEvento.this,
                        "No se pudo eliminar (" + response.code() + ")", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<String> call, Throwable error) {
                if (!isFinishing()) Toast.makeText(ActividadDetalleEvento.this,
                        "Sin conexión al eliminar", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verEnMapa() {
        if (evento.getLatitud() != 0 || evento.getLongitud() != 0) {
            String label = evento.getNombre();
            String uriBegin = "geo:" + evento.getLatitud() + "," + evento.getLongitud();
            String query = evento.getLatitud() + "," + evento.getLongitud() + "(" + label + ")";
            String encodedQuery = Uri.encode(query);
            String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
            Uri uri = Uri.parse(uriString);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(intent);
            } catch (Exception e) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ex) {
                    Toast.makeText(this, "No hay aplicación de mapas instalada", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Ubicación no disponible para este evento", Toast.LENGTH_SHORT).show();
        }
    }
}
