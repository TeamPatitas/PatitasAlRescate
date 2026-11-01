package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.base.BaseActivity;

import java.util.List;
import java.util.Locale;

public class SelectLocation extends BaseActivity implements OnMapReadyCallback {

    public static final String EXTRA_LATITUD = "latitud";
    public static final String EXTRA_LONGITUD = "longitud";
    public static final String EXTRA_DIRECCION = "direccion";

    private double latitudSeleccionada = -7.1617;
    private double longitudSeleccionada = -78.5128;
    private String direccionSeleccionada = "";
    private GoogleMap mMap;
    private Marker marcadorActual;
    private TextView txtCoordenadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_select_location);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_select_location), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarSelectLocation);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Seleccionar Ubicación");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        txtCoordenadas = findViewById(R.id.txt_coordenadas_seleccionadas);

        if (getIntent() != null) {
            latitudSeleccionada = getIntent().getDoubleExtra(EXTRA_LATITUD, latitudSeleccionada);
            longitudSeleccionada = getIntent().getDoubleExtra(EXTRA_LONGITUD, longitudSeleccionada);
        }
        resolverDireccion(latitudSeleccionada, longitudSeleccionada);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnConfirmar = findViewById(R.id.btn_confirmar_ubicacion);
        if (btnConfirmar != null) {
            btnConfirmar.setOnClickListener(v -> {
                Intent data = new Intent();
                data.putExtra(EXTRA_LATITUD, latitudSeleccionada);
                data.putExtra(EXTRA_LONGITUD, longitudSeleccionada);
                data.putExtra(EXTRA_DIRECCION, direccionSeleccionada);
                setResult(RESULT_OK, data);
                finish();
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        LatLng posicionInicial = new LatLng(latitudSeleccionada, longitudSeleccionada);
        marcadorActual = mMap.addMarker(new MarkerOptions()
                .position(posicionInicial)
                .title("Ubicación del evento")
                .draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionInicial, 15f));

        mMap.setOnMapClickListener(latLng -> {
            latitudSeleccionada = latLng.latitude;
            longitudSeleccionada = latLng.longitude;
            if (marcadorActual != null) {
                marcadorActual.setPosition(latLng);
            } else {
                marcadorActual = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Ubicación del evento")
                        .draggable(true));
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            resolverDireccion(latitudSeleccionada, longitudSeleccionada);
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) { }

            @Override
            public void onMarkerDrag(@NonNull Marker marker) { }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                LatLng pos = marker.getPosition();
                latitudSeleccionada = pos.latitude;
                longitudSeleccionada = pos.longitude;
                resolverDireccion(latitudSeleccionada, longitudSeleccionada);
            }
        });
    }

    private void resolverDireccion(double lat, double lng) {
        final String respaldo = String.format(Locale.US, "%.5f, %.5f", lat, lng);
        if (txtCoordenadas != null) {
            txtCoordenadas.setText("📍 Buscando dirección...");
        }
        new Thread(() -> {
            String direccion = null;
            try {
                Geocoder geocoder = new Geocoder(SelectLocation.this, new Locale("es", "PE"));
                List<Address> direcciones = geocoder.getFromLocation(lat, lng, 1);
                if (direcciones != null && !direcciones.isEmpty()) {
                    Address dir = direcciones.get(0);
                    String linea = dir.getAddressLine(0);
                    if (linea != null && !linea.trim().isEmpty()) {
                        direccion = linea.trim();
                    }
                }
            } catch (Exception ignored) { }

            final String resultado = (direccion != null && !direccion.isEmpty()) ? direccion : respaldo;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                direccionSeleccionada = resultado;
                if (txtCoordenadas != null) {
                    txtCoordenadas.setText("📍 " + resultado);
                }
            });
        }).start();
    }
}
