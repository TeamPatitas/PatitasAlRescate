package com.patitasalrescate.controllers.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.patitasalrescate.R;
import com.patitasalrescate.controllers.feed.ActividadRegistroExitoso;
import com.patitasalrescate.data_access.DAOAdoptante;
import com.patitasalrescate.model.Adoptante;

import java.util.UUID;

public class ActividadRegistrarAdoptante extends AppCompatActivity {
    private EditText etNombre, etCorreo, etPass, etTelefono, etEdad;
    private Spinner spSexo;
    private DAOAdoptante daoAdoptante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.ly_registrar_adoptante);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registrar_adoptante), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar1 = findViewById(R.id.toolbarRegistrarAdoptante);
        setSupportActionBar(toolbar1);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar1.setNavigationOnClickListener(v -> finish());

        daoAdoptante = new DAOAdoptante(this);

        etNombre = findViewById(R.id.rj_text_adopt_nombre);
        etCorreo = findViewById(R.id.rj_text_adopt_correo);
        etPass = findViewById(R.id.rj_text_adopt_password);
        etTelefono = findViewById(R.id.rj_text_adopt_telefono);
        etEdad = findViewById(R.id.rj_text_adopt_edad);
        spSexo = findViewById(R.id.rj_combo_adopt_sexo);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_sexo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSexo.setAdapter(adapter);

        findViewById(R.id.rj_button_registrar_adoptante).setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String edadStr = etEdad.getText().toString().trim();

        int seleccion = spSexo.getSelectedItemPosition();
        if (seleccion == 0) {
            Toast.makeText(this, "Por favor, seleccione un sexo", Toast.LENGTH_SHORT).show();
            return;
        }
        String sexo = spSexo.getSelectedItem().toString().trim();

        if (nombre.isEmpty()) { etNombre.setError("Ingrese su NOMBRE"); return; }
        if (pass.length() < 6) { etPass.setError("Mínimo 6 caracteres"); return; }
        if (correo.isEmpty()) { etCorreo.setError("Ingrese su CORREO"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { etCorreo.setError("Correo inválido"); return; }
        if (daoAdoptante.existeCorreo(correo)) { etCorreo.setError("Correo ya registrado"); return; }
        if (telefono.length() != 9) { etTelefono.setError("Teléfono de 9 dígitos"); return; }
        if (edadStr.isEmpty()) { etEdad.setError("Ingrese su EDAD"); return; }

        int edad;
        try { edad = Integer.parseInt(edadStr); } catch (Exception e) { return; }

        Adoptante nuevoAdoptante = new Adoptante(
                UUID.randomUUID().toString(),
                nombre,
                correo,
                pass,
                telefono,
                edad,
                sexo
        );

        daoAdoptante.insertar(nuevoAdoptante);
        
        Intent intent = new Intent(this, ActividadRegistroExitoso.class);
        intent.putExtra("USUARIO_NOMBRE", nombre);
        startActivity(intent);
        finish();
    }
}
