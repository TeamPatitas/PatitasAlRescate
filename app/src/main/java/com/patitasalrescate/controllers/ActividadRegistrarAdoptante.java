package com.patitasalrescate.controllers;

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
import com.patitasalrescate.data_access.DAOAdoptante;
import com.patitasalrescate.data_access.SupabaseService;
import com.patitasalrescate.model.Adoptante;
import com.patitasalrescate.utils.SeguridadUtils;

import java.util.UUID;

public class ActividadRegistrarAdoptante extends AppCompatActivity {
    private EditText etNombre, etCorreo, etPass, etTelefono, etEdad;
    private Spinner spSexo;
    private DAOAdoptante daoAdoptante;
    private SupabaseService supabaseService;

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
        toolbar1.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        daoAdoptante = new DAOAdoptante(this);
        supabaseService = new SupabaseService();

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
        String passTextoPlano = etPass.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        int seleccion = spSexo.getSelectedItemPosition();
        if (seleccion == 0) {
            Toast.makeText(this, "Por favor, seleccione un sexo", Toast.LENGTH_SHORT).show();
            return;
        }
        String sexo = spSexo.getSelectedItem().toString().trim();

        String edadStr = etEdad.getText().toString().trim();
        String passEncriptada = SeguridadUtils.encriptar(passTextoPlano);

        if (nombre.isEmpty()) {
            etNombre.setError("Aún no ha ingresado su NOMBRE");
            return;
        }
        if (passTextoPlano.length() < 6) {
            etPass.setError("La CONTRASEÑA debe tener al menos 6 dígitos");
            return;
        }
        if (correo.isEmpty()) {
            etCorreo.setError("Ingrese su CORREO");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Ingrese un formato de CORREO válido \n ejemplo: @gmail.com");
            return;
        }
        if (daoAdoptante.existeCorreo(correo)) {
            etCorreo.setError("Este CORREO ya está registrado, Intenta con otro");
            return;
        }
        if (telefono.length() != 9) {
            etTelefono.setError("El TELÉFONO debe contar 9 dígitos");
            return;
        }
        if (edadStr.isEmpty()) {
            etEdad.setError("Ingrese su EDAD");
            return;
        }
        int edad;
        try {
            edad = Integer.parseInt(edadStr);
        } catch (NumberFormatException e) {
            etEdad.setError("Edad inválida");
            return;
        }
        if (edad <= 8 || edad > 115) {
            etEdad.setError("Ingrese una edad válida (8-115)");
            return;
        }

        String idAdoptante = UUID.randomUUID().toString();

        Adoptante nuevoAdoptante = new Adoptante();
        nuevoAdoptante.setIdAdoptante(idAdoptante);
        nuevoAdoptante.setNombre(nombre);
        nuevoAdoptante.setCorreo(correo);
        nuevoAdoptante.setPassword(passEncriptada);
        nuevoAdoptante.setNumCelular(telefono);
        nuevoAdoptante.setEdad(edad);
        nuevoAdoptante.setSexo(sexo);
        nuevoAdoptante.setLastSync(System.currentTimeMillis());

        long resultadoLocal = daoAdoptante.insertar(nuevoAdoptante);

        if (resultadoLocal != -1) {
            Toast.makeText(this, "Guardando...", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                try {
                    boolean exitoNube = supabaseService.insertarAdoptante(nuevoAdoptante);

                    runOnUiThread(() -> {
                        if (exitoNube) {
                            Intent intent = new Intent(ActividadRegistrarAdoptante.this, ActividadRegistroExitoso.class);
                            intent.putExtra("USUARIO_NOMBRE", nombre);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Guardado local OK, pero error en la nube", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Error de conexión a la nube", Toast.LENGTH_SHORT).show());
                }
            }).start();

        } else {
            Toast.makeText(this, "Error crítico al guardar localmente", Toast.LENGTH_SHORT).show();
        }
    }
}