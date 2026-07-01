package com.patitasalrescate.controllers.feed;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.patitasalrescate.MainActivity;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.management.ActividadRegistrarMascota;
import com.patitasalrescate.controllers.auth.ActividadIniciarSesion;
import com.patitasalrescate.controllers.lists.ActividadListarMascotas;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.controllers.lists.ActividadEventosLista;

public class ActividadInicioRefugio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_inicio_refugio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        BottomNavigationView oMenu = findViewById(R.id.menuInicioRefugio);



        PatitasSessionManager session = PatitasSessionManager.getInstance(this);
        String nombreRefugio = session.getUserName();
        String idRefugio = session.getUserId();

        if (nombreRefugio == null || nombreRefugio.isEmpty()) {
            nombreRefugio = "Refugio (Modo Prueba)";
        }

        Toolbar oBarra= findViewById(R.id.toolbarInicioRefugio);
        setSupportActionBar(oBarra);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        oBarra.setTitle("Refugio " + nombreRefugio);
        TextView textobienvenida= findViewById(R.id.txtBienvenidoRefugio);
        textobienvenida.setText("Bienvenido refugio: "+ nombreRefugio);
        textobienvenida.setGravity(Gravity.CENTER);
        String finalNombreRefugio = nombreRefugio;
        String finalIdRefugio = idRefugio;

        oMenu.setOnItemSelectedListener(menuItem -> {
            Intent oIntento=null;
            if(menuItem.getItemId()==R.id.itemInicioRefugio){

                return true;
            }
            if(menuItem.getItemId()==R.id.itemRegistrarMascotaRefugio){
                oIntento= new Intent(this, ActividadRegistrarMascota.class);
                startActivity(oIntento );
                return true;
            }
            if(menuItem.getItemId()==R.id.itemListarMacostaRefugio){
                oIntento= new Intent(this, ActividadListarMascotas.class);
                oIntento.putExtra("es_refugio_key", true);
                startActivity(oIntento );
                return true;
            }

            if (menuItem.getItemId() == R.id.itemEventosRefugio) {
                oIntento = new Intent(this, ActividadEventosLista.class);
                startActivity(oIntento);
                return true;
            }

            if(menuItem.getItemId()==R.id.itemSalirRefugio){
                session.logout();
                oIntento= new Intent(this, MainActivity.class);
                oIntento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(oIntento );
                return true;
            }
            return false;
        });
    }
}