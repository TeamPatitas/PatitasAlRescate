package com.patitasalrescate.controllers.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.patitasalrescate.R;

public class ActividadIngresar extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ly_ingresar);
        Button button_soypersona = findViewById(R.id.rj_button_soypersona);
        Button button_soyasociacion = findViewById(R.id.rj_button_soyasociacion);
        TextView text_yatengocuenta = findViewById(R.id.rj_click_inicia_sesion);

        text_yatengocuenta.setPaintFlags(android.graphics.Paint.UNDERLINE_TEXT_FLAG);

        button_soypersona.setOnClickListener(v->{
            Intent intent = new Intent(ActividadIngresar.this, ActividadRegistrarAdoptante.class);
            startActivity(intent);
        });

        button_soyasociacion.setOnClickListener(v->{
            Intent intent = new Intent(ActividadIngresar.this, ActividadRegistrarOrganizacion.class);
            startActivity(intent);
        });

        text_yatengocuenta.setOnClickListener(v->{
            Intent intent = new Intent(ActividadIngresar.this, ActividadIniciarSesion.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ingresar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}