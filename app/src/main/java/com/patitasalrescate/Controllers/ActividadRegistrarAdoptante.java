package com.patitasalrescate.Controllers;

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
import com.patitasalrescate.accesoADatos.DAOAdoptante;
import com.patitasalrescate.accesoADatos.SupabaseService;
import com.patitasalrescate.model.Adoptante;
import com.patitasalrescate.utils.SeguridadUtils;

import java.util.UUID;

public class ActividadRegistrarAdoptante extends AppCompatActivity {

    // Variables de interfaz
    private EditText etNombre, etCorreo, etPass, etTelefono, etEdad;
    private Spinner spSexo;

    // Variables de lógica
    private DAOAdoptante daoAdoptante;
    private SupabaseService supabaseService; // ¡ESTO ES LO QUE NO PODEMOS PERDER!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Configuración Visual (EdgeToEdge) - Traído del Código 2
        EdgeToEdge.enable(this);
        setContentView(R.layout.ly_registrar_adoptante);

        // 2. Manejo de Insets (Para que el Layout no se corte) - Traído del Código 2
        // Esto asegura que el ID "registrar_adoptante" de tu XML respete los márgenes del celular
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registrar_adoptante), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 3. Configuración del Toolbar
        Toolbar toolbar1 = findViewById(R.id.toolbarRegistrarAdoptante);
        setSupportActionBar(toolbar1);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar1.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // 4. Inicialización de Clases de Datos (DAO y Supabase)
        daoAdoptante = new DAOAdoptante(this);
        supabaseService = new SupabaseService();

        // 5. Vincular Vistas
        etNombre = findViewById(R.id.rj_text_adopt_nombre);
        etCorreo = findViewById(R.id.rj_text_adopt_correo);
        etPass = findViewById(R.id.rj_text_adopt_password);
        etTelefono = findViewById(R.id.rj_text_adopt_telefono);
        etEdad = findViewById(R.id.rj_text_adopt_edad);
        spSexo = findViewById(R.id.rj_combo_adopt_sexo);

        // 6. Configurar Spinner (Combo box)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_sexo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSexo.setAdapter(adapter);

        // 7. Botón de Registro
        findViewById(R.id.rj_button_registrar_adoptante).setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        // --- AQUI USAMOS LA LÓGICA DEL CODIGO 1 (QUE ESTABA BIEN) ---

        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String passTextoPlano = etPass.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        // Validación Sexo
        int seleccion = spSexo.getSelectedItemPosition();
        if (seleccion == 0) {
            Toast.makeText(this, "Por favor, seleccione un sexo", Toast.LENGTH_SHORT).show();
            return;
        }
        String sexo = spSexo.getSelectedItem().toString().trim();

        String edadStr = etEdad.getText().toString().trim();
        String passEncriptada = SeguridadUtils.encriptar(passTextoPlano);

        // Validaciones Generales
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

        // Validación Edad Numérica
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

        // --- GENERACIÓN DE ID Y OBJETO (CLAVE PARA SUPABASE) ---
        String idAdoptante = UUID.randomUUID().toString();

        Adoptante nuevoAdoptante = new Adoptante();
        nuevoAdoptante.setIdAdoptante(idAdoptante); // Importante: Asignar el UUID generado
        nuevoAdoptante.setNombre(nombre);
        nuevoAdoptante.setCorreo(correo);
        nuevoAdoptante.setPassword(passEncriptada);
        nuevoAdoptante.setNumCelular(telefono);
        nuevoAdoptante.setEdad(edad);
        nuevoAdoptante.setSexo(sexo);
        nuevoAdoptante.setLastSync(System.currentTimeMillis());

        // 1. Guardar Localmente (SQLite)
        long resultadoLocal = daoAdoptante.insertar(nuevoAdoptante);

        if (resultadoLocal != -1) {
            // Si guardó en local, intentamos subir a la nube
            Toast.makeText(this, "Guardando...", Toast.LENGTH_SHORT).show();

            // 2. Guardar en Supabase (Hilo secundario para no congelar la app)
            new Thread(() -> {
                try {
                    boolean exitoNube = supabaseService.insertarAdoptante(nuevoAdoptante);

                    // Volver al hilo principal para mostrar el mensaje final
                    runOnUiThread(() -> {
                        if (exitoNube) {
                            Intent intent = new Intent(ActividadRegistrarAdoptante.this, ActividadRegistroExitoso.class);
                            intent.putExtra("USUARIO_NOMBRE", nombre); // Pasamos el nombre para el Splash
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Guardado local OK, pero error en la nube", Toast.LENGTH_LONG).show();
                            // Aquí podrías decidir si cerrar o no, por seguridad cerramos
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