package com.patitasalrescate.controllers.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.feed.ActividadRegistroExitoso;
import com.patitasalrescate.data_access.DAOAdoptante;
import com.patitasalrescate.model.Adoptante;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ActividadRegistrarAdoptante extends AppCompatActivity {
    private EditText etNombre, etCorreo, etPass, etTelefono, etEdad;
    private Spinner spSexo;
    private DAOAdoptante daoAdoptante;

    private ShapeableImageView imgAvatar;
    private Uri uriFotoSeleccionada;
    private ActivityResultLauncher<Intent> launcherGaleria;

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
        imgAvatar = findViewById(R.id.img_preview_adoptante);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_sexo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSexo.setAdapter(adapter);

        configurarLauncherFoto();
        findViewById(R.id.btn_seleccionar_foto_adoptante).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            launcherGaleria.launch(intent);
        });

        findViewById(R.id.rj_button_registrar_adoptante).setOnClickListener(v -> registrarUsuario());
    }

    private void configurarLauncherFoto() {
        launcherGaleria = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                Uri uriSeleccionada = result.getData().getData();
                Uri uriLocal = copiarImagenAAlmacenamientoInterno(uriSeleccionada);
                if (uriLocal != null) {
                    uriFotoSeleccionada = uriLocal;
                    Glide.with(this).load(uriFotoSeleccionada).centerCrop().into(imgAvatar);
                } else {
                    Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Uri copiarImagenAAlmacenamientoInterno(Uri uriOrigen) {
        try (InputStream in = getContentResolver().openInputStream(uriOrigen)) {
            if (in == null) return null;

            File carpetaFotos = new File(getFilesDir(), "avatares");
            if (!carpetaFotos.exists()) carpetaFotos.mkdirs();

            File archivoDestino = new File(carpetaFotos, "avatar_" + UUID.randomUUID() + ".jpg");
            try (OutputStream out = new FileOutputStream(archivoDestino)) {
                byte[] buffer = new byte[4096];
                int leido;
                while ((leido = in.read(buffer)) != -1) {
                    out.write(buffer, 0, leido);
                }
            }
            return Uri.fromFile(archivoDestino);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
                sexo,
                uriFotoSeleccionada != null ? uriFotoSeleccionada.toString() : null
        );

        daoAdoptante.insertar(nuevoAdoptante);
        
        Intent intent = new Intent(this, ActividadRegistroExitoso.class);
        intent.putExtra("USUARIO_NOMBRE", nombre);
        startActivity(intent);
        finish();
    }
}
