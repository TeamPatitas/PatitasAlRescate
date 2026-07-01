package com.patitasalrescate.controllers.auth;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.patitasalrescate.R;
import com.patitasalrescate.data_access.DAORefugio;
import com.patitasalrescate.model.Refugio;

import java.util.UUID;

public class ActividadRegistrarOrganizacion extends AppCompatActivity {
    private EditText txtNombre, txtDireccion, txtTelefono, txtCorreo, txtPassword;
    private Button btnGuardar;
    private DAORefugio daoRefugio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_registrar_organizacion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registrar_organizacion_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarRegistrarOrganizacion);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        daoRefugio = new DAORefugio(this);

        txtNombre = findViewById(R.id.rj_text_org_nombre);
        txtDireccion = findViewById(R.id.rj_text_org_direccion);
        txtTelefono = findViewById(R.id.rj_text_org_telefono);
        txtCorreo = findViewById(R.id.rj_text_org_correo);
        txtPassword = findViewById(R.id.rj_text_org_password);
        btnGuardar = findViewById(R.id.rj_button_registrar_organizacion);

        btnGuardar.setOnClickListener(v -> procesarRegistro());
    }

    private void procesarRegistro() {
        String nombre = txtNombre.getText().toString().trim();
        String direccion = txtDireccion.getText().toString().trim();
        String telefono = txtTelefono.getText().toString().trim();
        String correo = txtCorreo.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if (nombre.isEmpty()) { txtNombre.setError("Ingrese el nombre"); return; }
        if (direccion.isEmpty()) { txtDireccion.setError("Ingrese la dirección"); return; }
        if (telefono.length() != 9) { txtTelefono.setError("Ingrese un teléfono válido de 9 dígitos"); return; }
        if (correo.isEmpty()) { txtCorreo.setError("Ingrese el correo"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { txtCorreo.setError("Correo inválido"); return; }
        if (password.length() < 6) { txtPassword.setError("Mínimo 6 caracteres"); return; }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando organización (Demo)...");
        progressDialog.show();

        Refugio nuevoRefugio = new Refugio();
        nuevoRefugio.setIdRefugio(UUID.randomUUID().toString());
        nuevoRefugio.setNombre(nombre);
        nuevoRefugio.setDireccion(direccion);
        nuevoRefugio.setCorreo(correo);
        nuevoRefugio.setPassword(password);
        nuevoRefugio.setNumCelular(telefono);

        daoRefugio.insertar(nuevoRefugio);
        
        progressDialog.dismiss();
        Toast.makeText(this, "¡Refugio registrado exitosamente (Demo)!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
