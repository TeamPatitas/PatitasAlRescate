package com.patitasalrescate.Controllers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAORefugio;
import com.patitasalrescate.accesoADatos.SupabaseService;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.utils.SeguridadUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ActividadRegistrarOrganizacion extends AppCompatActivity {

    private EditText txtNombre, txtDireccion, txtTelefono, txtCorreo, txtPassword;
    private ImageView imgPreview;
    private Button btnGuardar, btnSeleccionarFoto;
    private ImageButton btnAbrirMapa;

    private DAORefugio daoRefugio;
    private SupabaseService supabaseService;
    private Uri uriImagenSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_registrar_organizacion);

        Toolbar toolbar = findViewById(R.id.toolbarRegistrarOrganizacion);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        daoRefugio = new DAORefugio(this);
        supabaseService = new SupabaseService();

        txtNombre = findViewById(R.id.rj_text_org_nombre);
        txtDireccion = findViewById(R.id.rj_text_org_direccion);
        txtTelefono = findViewById(R.id.rj_text_org_telefono);
        txtCorreo = findViewById(R.id.rj_text_org_correo);
        txtPassword = findViewById(R.id.rj_text_org_password);

        imgPreview = findViewById(R.id.img_preview_refugio);
        btnSeleccionarFoto = findViewById(R.id.btn_seleccionar_foto);
        btnAbrirMapa = findViewById(R.id.btn_abrir_mapa);
        btnGuardar = findViewById(R.id.rj_button_registrar_organizacion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registrar_organizacion_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActivityResultLauncher<Intent> launcherGaleria = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uriImagenSeleccionada = result.getData().getData();
                        if (uriImagenSeleccionada != null) {
                            Glide.with(this).load(uriImagenSeleccionada).circleCrop().into(imgPreview);
                        }
                    }
                }
        );

        btnSeleccionarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcherGaleria.launch(intent);
        });

        // Botón mapa para previsualizar dirección
        btnAbrirMapa.setOnClickListener(v -> {
            String direccionRaw = txtDireccion.getText().toString().trim();
            if (!direccionRaw.isEmpty()) {

                String direccionLimpia = direccionRaw
                        .replaceAll("(?i)\\b e \\b", " y ")
                        .replace("/", " y ");

                String direccionBuscada = direccionLimpia + ", Cajamarca, Perú";

                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(direccionBuscada));

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
                }
            } else {
                txtDireccion.setError("Ingrese una dirección primero");
            }
        });

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
        if (telefono.length()!=9) { txtTelefono.setError("Ingrese un teléfono válido de 9 dígitos"); return; }
        if (correo.isEmpty()) { txtCorreo.setError("Ingrese el correo"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { txtCorreo.setError("Correo inválido"); return; }
        if (password.length() < 6) { txtPassword.setError("Mínimo 6 caracteres"); return; }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando organización...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            String urlFinalFoto = "https://picsum.photos/200";

            // 1. Subir Foto
            if (uriImagenSeleccionada != null) {
                try {
                    byte[] imagenBytes = getBytesFromUri(uriImagenSeleccionada);
                    if (imagenBytes != null) {
                        String nombreArchivo = "refugio_" + System.currentTimeMillis() + ".jpg";
                        String urlSubida = supabaseService.subirFoto(imagenBytes, nombreArchivo);
                        if (urlSubida != null) urlFinalFoto = urlSubida;
                    }
                } catch (IOException e) { e.printStackTrace(); }
            }

            double latCalculada = 0.0;
            double lonCalculada = 0.0;
            try {
                Geocoder geocoder = new Geocoder(ActividadRegistrarOrganizacion.this, Locale.getDefault());

                String direccionLimpia = direccion
                        .replaceAll("(?i)\\b e \\b", " y ")
                        .replace("/", " y ");

                String direccionExacta = direccionLimpia + ", Cajamarca, Perú";
                List<Address> addresses = geocoder.getFromLocationName(direccionExacta, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    latCalculada = addresses.get(0).getLatitude();
                    lonCalculada = addresses.get(0).getLongitude();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 3. Preparar Datos
            String passwordEncriptada = SeguridadUtils.encriptar(password);
            String idRefugio = UUID.randomUUID().toString();

            Refugio nuevoRefugio = new Refugio(
                    idRefugio,
                    nombre,
                    direccion,
                    latCalculada,
                    lonCalculada,
                    correo,
                    passwordEncriptada,
                    telefono,
                    urlFinalFoto,
                    System.currentTimeMillis()
            );

            // 4. Subir a Supabase
            boolean subidoANube = false;
            String mensajeError = "";
            try {
                subidoANube = supabaseService.insertarRefugio(nuevoRefugio);
                if (!subidoANube) mensajeError = "Error al subir a la nube";
            } catch (Exception e) {
                e.printStackTrace();
                mensajeError = e.getMessage();
            }

            // 5. Guardar Local y UI
            Refugio finalRefugio = nuevoRefugio;
            boolean finalSubidoANube = subidoANube;
            String finalMensajeError = mensajeError;
            double finalLat = latCalculada;

            runOnUiThread(() -> {
                long resultado = daoRefugio.insertar(finalRefugio);
                progressDialog.dismiss();

                if (resultado != -1) {
                    if (finalSubidoANube) {
                        String msg = finalLat != 0 ? "¡Registrado y ubicado en mapa!" : "Registrado (Ubicación por dirección)";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Solo Local. Error Nube: " + finalMensajeError, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Error al guardar localmente", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private byte[] getBytesFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) byteBuffer.write(buffer, 0, len);
        inputStream.close();
        return byteBuffer.toByteArray();
    }
}