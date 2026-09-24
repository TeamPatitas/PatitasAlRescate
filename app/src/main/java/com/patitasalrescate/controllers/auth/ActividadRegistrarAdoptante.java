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
import com.patitasalrescate.data.mock.DAOAdoptante;
import com.patitasalrescate.model.Adoptante;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.data.remote.dto.RegisterRequest;
import com.patitasalrescate.data.remote.dto.AuthResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;

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
        String birthDate = etEdad.getText().toString().trim();

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
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            if (format.parse(birthDate) == null || birthDate.length() != 10) throw new ParseException("Fecha inválida", 0);
        } catch (ParseException error) {
            etEdad.setError("Usa AAAA-MM-DD");
            return;
        }
        String[] names = nombre.split("\\s+", 2);
        if (names.length < 2) { etNombre.setError("Ingresa nombre y apellido"); return; }
        RegisterRequest request = new RegisterRequest();
        request.firstName = names[0];
        request.lastName = names[1];
        request.email = correo;
        request.password = pass;
        request.birthDate = birthDate;
        // El OpenAPI declara un entero; confirmar con backend el orden de estos valores.
        request.gender = seleccion - 1;
        try { if (uriFotoSeleccionada != null) request.photo = ApiApp.upload(this, uriFotoSeleccionada); }
        catch (IOException error) { Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show(); return; }
        findViewById(R.id.rj_button_registrar_adoptante).setEnabled(false);
        ApiApp.client().auth.register(request).enqueue(new Callback<AuthResponse>() {
            @Override public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                findViewById(R.id.rj_button_registrar_adoptante).setEnabled(true);
                if (response.isSuccessful()) {
                    Intent intent = new Intent(ActividadRegistrarAdoptante.this, ActividadRegistroExitoso.class);
                    intent.putExtra("USUARIO_NOMBRE", nombre);
                    startActivity(intent);
                    finish();
                } else Toast.makeText(ActividadRegistrarAdoptante.this,
                        "No se pudo registrar (" + response.code() + ")", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<AuthResponse> call, Throwable error) {
                if (isFinishing() || isDestroyed()) return;
                findViewById(R.id.rj_button_registrar_adoptante).setEnabled(true);
                Toast.makeText(ActividadRegistrarAdoptante.this, "Sin conexión al registrar", Toast.LENGTH_LONG).show();
            }
        });
    }
}
