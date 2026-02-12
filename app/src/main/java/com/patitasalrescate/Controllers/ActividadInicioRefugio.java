package com.patitasalrescate.Controllers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.patitasalrescate.MainActivity;
import com.patitasalrescate.R;

public class ActividadInicioRefugio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_inicio_refugio);
        BottomNavigationView oMenu = findViewById(R.id.menuInicioRefugio);
        String nombreRefugio = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_NOMBRE_USUARIO);
        if (nombreRefugio == null || nombreRefugio.trim().isEmpty()) {
            // fallback por compatibilidad
            nombreRefugio = getIntent().getStringExtra("nombre_refugio_key");
        }
        if (nombreRefugio == null || nombreRefugio.isEmpty()) {
            nombreRefugio = "Refugio (Modo Prueba)";
        }
        String idRefugio = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO);

        Toolbar oBarra= findViewById(R.id.toolbarInicioRefugio);
        setSupportActionBar(oBarra);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        oBarra.setTitle("Refugio " + nombreRefugio);
        TextView textobienvenida= findViewById(R.id.txtBienvenidoRefugio);
        textobienvenida.setText("Bienvenido refugio: "+ nombreRefugio);
        String finalNombreRefugio = nombreRefugio;
        String finalIdRefugio = idRefugio;

        oMenu.setOnItemSelectedListener(menuItem -> {
            Intent oIntento=null;
            if(menuItem.getItemId()==R.id.itemInicioRefugio){

                return true;
            }
            if(menuItem.getItemId()==R.id.itemRegistrarMascotaRefugio){
                oIntento= new Intent(this, ActividadRegistrarMascota.class);
                oIntento.putExtra("nombre_refugio_key", finalNombreRefugio);
                oIntento.putExtra(ActividadIniciarSesion.EXTRA_TIPO_USUARIO, "REFUGIO");
                oIntento.putExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO, finalIdRefugio);
                oIntento.putExtra(ActividadIniciarSesion.EXTRA_NOMBRE_USUARIO, finalNombreRefugio);
                startActivity(oIntento );
                return true;
            }
            if(menuItem.getItemId()==R.id.itemListarMacostaRefugio){
                oIntento= new Intent(this,ActividadListarMascotas.class);
                oIntento.putExtra("es_refugio_key", true);
                oIntento.putExtra("nombre_refugio_key", finalNombreRefugio);
                oIntento.putExtra(ActividadIniciarSesion.EXTRA_TIPO_USUARIO, "REFUGIO");
                oIntento.putExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO, finalIdRefugio);
                oIntento.putExtra(ActividadIniciarSesion.EXTRA_NOMBRE_USUARIO, finalNombreRefugio);
                startActivity(oIntento );
                return true;
            }
            if(menuItem.getItemId()==R.id.itemSalirRefugio){
                oIntento= new Intent(this, MainActivity.class);
                oIntento.putExtra("nombre_refugio_key", finalNombreRefugio);
                oIntento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(oIntento );
                return true;
            }
            return false;
        });

    }
}