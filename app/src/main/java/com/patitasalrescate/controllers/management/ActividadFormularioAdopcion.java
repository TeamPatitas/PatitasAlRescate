package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.patitasalrescate.R;
import com.patitasalrescate.controllers.base.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity del formulario de adopción formal.
 *
 * <p>Recibe vía Intent:
 * <ul>
 *   <li>{@code "nombre_mascota"}   — nombre de la mascota a adoptar</li>
 *   <li>{@code "nombre_refugio"}   — nombre de la asociación</li>
 *   <li>{@code "telefono_refugio"} — número de WhatsApp de la asociación (ej: "51987654321")</li>
 * </ul>
 *
 * <p>Flujo interno:
 * <ol>
 *   <li>El usuario llena el formulario.</li>
 *   <li>Opcionalmente adjunta una foto de su firma desde galería.</li>
 *   <li>Presiona "GENERAR Y ENVIAR" → se valida → se genera el PDF local.</li>
 *   <li>Se lanza un Intent a com.whatsapp con el PDF adjunto via FileProvider.</li>
 * </ol>
 */
public class ActividadFormularioAdopcion extends BaseActivity {

    private static final String TAG = "FormularioAdopcion";

    // ── Keys del Intent entrante ──────────────────────────────────
    public static final String KEY_NOMBRE_MASCOTA   = "nombre_mascota";
    public static final String KEY_NOMBRE_REFUGIO   = "nombre_refugio";
    public static final String KEY_TELEFONO_REFUGIO  = "telefono_refugio";

    // ── Autoridad del FileProvider (debe coincidir con el Manifest) ─
    private static final String FILE_PROVIDER_AUTHORITY =
            "com.patitasalrescate.fileprovider";

    // ── Datos de contexto ──────────────────────────────────────────
    private String nombreMascota;
    private String nombreRefugio;
    private String telefonoRefugio;

    // ── URI de la firma adjuntada (null = no adjuntada) ────────────
    private Uri uriFirmaSeleccionada = null;

    // ── ActivityResultLauncher para la galería ────────────────────
    private ActivityResultLauncher<Intent> launcherFirma;

    // ── Referencias a vistas ──────────────────────────────────────
    // Sección 1: Datos personales
    private TextInputEditText etApellidosNombres, etTelefono, etFecha;
    private TextInputEditText etDni, etDireccion, etEmail, etCelular;
    private TextInputEditText etEstadoCivil, etCiudad, etRefNombre, etRefTelefono;
    private TextInputLayout   tilDni, tilCelular;

    // Sección 2: Cuestionario Sí/No
    // Cada pregunta reutiliza item_pregunta_si_no (lbl + RadioGroup con rb_si / rb_no)
    private View preguntaTieneAnimales, preguntaEsterilizados, preguntaTuvoAntes;
    private View preguntaVisitaPeriodica, preguntaTodosAcuerdo, preguntaHayNinos;
    private View preguntaAlergicos, preguntaEspacioSuficiente;
    private View preguntaVeterinario, preguntaRecursos, preguntaEsterilizar;
    private RadioGroup rgArrendador;

    // Sección 3: Preguntas abiertas
    private TextInputEditText etPorQueAdoptar, etCualesAnimales, etQuePasoAntes;
    private TextInputEditText etEdadesNinos, etMudanza, etRupturaFamiliar;
    private TextInputEditText etAniosVida, etVision5, etDondeDormira;
    private TextInputEditText etComportamiento, etVeterinarioInfo;
    private Spinner spinnerHorasSolo;

    // Sección 4: Gastos y cuidados
    private Spinner spinnerGastos;
    private CheckBox cbVisitasVet, cbVacunacion, cbPaseos, cbCollarId;
    private CheckBox cbDesparasitacion, cbCepillado, cbArenero, cbCroquetas, cbAguaLimpia;

    // Sección 5: Compromisos y firma
    private CheckBox cbAceptaCondiciones;
    private ImageView imgPreviewFirma;
    private TextView  txtEstadoFirma;

    // ─────────────────────────────────────────────────────────────
    // onCreate
    // ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_formulario_adopcion);

        // Leer extras del Intent
        nombreMascota   = getIntent().getStringExtra(KEY_NOMBRE_MASCOTA);
        nombreRefugio   = getIntent().getStringExtra(KEY_NOMBRE_REFUGIO);
        telefonoRefugio = getIntent().getStringExtra(KEY_TELEFONO_REFUGIO);
        if (nombreMascota   == null) nombreMascota   = "";
        if (nombreRefugio   == null) nombreRefugio   = "";
        if (telefonoRefugio == null) telefonoRefugio = "";

        registrarLauncherFirma();
        initViews();
        configurarToolbar();
        configurarSpinners();
        configurarPreguntasSiNo();
        configurarBotonAdjuntarFirma();
        configurarBotonGenerar();
    }

    // ─────────────────────────────────────────────────────────────
    // ActivityResultLauncher — galería para foto de firma
    // ─────────────────────────────────────────────────────────────

    /**
     * Se registra ANTES de onCreate (o en onCreate antes de setContentView).
     * Permite al usuario seleccionar una imagen de la galería como firma manuscrita.
     */
    private void registrarLauncherFirma() {
        launcherFirma = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK
                            || result.getData() == null
                            || result.getData().getData() == null) return;

                    Uri uriSeleccionada = result.getData().getData();

                    // Validar que sea imagen
                    String mime = null;
                    try { mime = getContentResolver().getType(uriSeleccionada); }
                    catch (Exception ignored) { }

                    if (mime == null || (!mime.startsWith("image/"))) {
                        Toast.makeText(this, "Solo se permiten imágenes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    uriFirmaSeleccionada = uriSeleccionada;
                    mostrarPreviewFirma(uriSeleccionada);
                    Log.d(TAG, "Firma seleccionada: " + uriSeleccionada);
                }
        );
    }

    private void mostrarPreviewFirma(Uri uri) {
        imgPreviewFirma.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).fitCenter().into(imgPreviewFirma);
        txtEstadoFirma.setText("✓ Firma adjuntada correctamente (opcional)");
    }

    // ─────────────────────────────────────────────────────────────
    // Inicialización de vistas
    // ─────────────────────────────────────────────────────────────

    private void initViews() {
        // Sección 1
        etApellidosNombres = findViewById(R.id.et_apellidos_nombres);
        etTelefono         = findViewById(R.id.et_telefono);
        etFecha            = findViewById(R.id.et_fecha);
        tilDni             = findViewById(R.id.til_dni);
        etDni              = findViewById(R.id.et_dni);
        etDireccion        = findViewById(R.id.et_direccion);
        etEmail            = findViewById(R.id.et_email);
        tilCelular         = findViewById(R.id.til_celular);
        etCelular          = findViewById(R.id.et_celular);
        etEstadoCivil      = findViewById(R.id.et_estado_civil);
        etCiudad           = findViewById(R.id.et_ciudad);
        etRefNombre        = findViewById(R.id.et_ref_nombre);
        etRefTelefono      = findViewById(R.id.et_ref_telefono);

        // Sección 2 (views padre de los <include>)
        preguntaTieneAnimales   = findViewById(R.id.pregunta_tiene_animales);
        preguntaEsterilizados   = findViewById(R.id.pregunta_esterilizados);
        preguntaTuvoAntes       = findViewById(R.id.pregunta_tuvo_antes);
        preguntaVisitaPeriodica = findViewById(R.id.pregunta_visita_periodica);
        preguntaTodosAcuerdo    = findViewById(R.id.pregunta_todos_acuerdo);
        preguntaHayNinos        = findViewById(R.id.pregunta_hay_ninos);
        preguntaAlergicos       = findViewById(R.id.pregunta_alergicos);
        preguntaEspacioSuficiente = findViewById(R.id.pregunta_espacio_suficiente);
        preguntaVeterinario     = findViewById(R.id.pregunta_veterinario);
        preguntaRecursos        = findViewById(R.id.pregunta_recursos);
        preguntaEsterilizar     = findViewById(R.id.pregunta_esterilizar);
        rgArrendador            = findViewById(R.id.rg_arrendador);

        // Sección 3
        etPorQueAdoptar    = findViewById(R.id.et_por_que_adoptar);
        etCualesAnimales   = findViewById(R.id.et_cuales_animales);
        etQuePasoAntes     = findViewById(R.id.et_que_paso_anteriores);
        etEdadesNinos      = findViewById(R.id.et_edades_ninos);
        etMudanza          = findViewById(R.id.et_mudanza);
        etRupturaFamiliar  = findViewById(R.id.et_ruptura_familiar);
        etAniosVida        = findViewById(R.id.et_anios_vida);
        etVision5          = findViewById(R.id.et_vision_5_anios);
        etDondeDormira     = findViewById(R.id.et_donde_dormira);
        spinnerHorasSolo   = findViewById(R.id.spinner_horas_solo);
        etComportamiento   = findViewById(R.id.et_comportamiento);
        etVeterinarioInfo  = findViewById(R.id.et_veterinario_info);

        // Sección 4
        spinnerGastos    = findViewById(R.id.spinner_gastos);
        cbVisitasVet     = findViewById(R.id.cb_visitas_vet);
        cbVacunacion     = findViewById(R.id.cb_vacunacion);
        cbPaseos         = findViewById(R.id.cb_paseos);
        cbCollarId       = findViewById(R.id.cb_collar_id);
        cbDesparasitacion= findViewById(R.id.cb_desparasitacion);
        cbCepillado      = findViewById(R.id.cb_cepillado);
        cbArenero        = findViewById(R.id.cb_arenero);
        cbCroquetas      = findViewById(R.id.cb_croquetas);
        cbAguaLimpia     = findViewById(R.id.cb_agua_limpia);

        // Sección 5
        cbAceptaCondiciones = findViewById(R.id.cb_acepto_condiciones);
        imgPreviewFirma     = findViewById(R.id.img_preview_firma);
        txtEstadoFirma      = findViewById(R.id.txt_estado_firma);
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_formulario);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String titulo = nombreMascota.isEmpty()
                    ? "Formulario de Adopción"
                    : "Adoptar a " + nombreMascota;
            getSupportActionBar().setTitle(titulo);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // ─────────────────────────────────────────────────────────────
    // Configurar spinners
    // ─────────────────────────────────────────────────────────────

    private void configurarSpinners() {
        // Spinner de horas solo
        String[] horasSoloOpciones = {
                "Seleccionar...", "Menos de 2 horas", "2 - 4 horas",
                "4 - 6 horas", "6 - 8 horas", "Más de 8 horas"
        };
        ArrayAdapter<String> adapterHoras = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, horasSoloOpciones);
        adapterHoras.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHorasSolo.setAdapter(adapterHoras);

        // Spinner de gastos mensuales
        String[] gastosOpciones = {
                "Seleccionar...", "S/. 20 – 30", "S/. 30 – 40",
                "S/. 40 – 50", "S/. 50 – 70", "Más de S/. 70"
        };
        ArrayAdapter<String> adapterGastos = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, gastosOpciones);
        adapterGastos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGastos.setAdapter(adapterGastos);
    }

    // ─────────────────────────────────────────────────────────────
    // Configurar etiquetas de las preguntas Sí/No via <include>
    // ─────────────────────────────────────────────────────────────

    private void configurarPreguntasSiNo() {
        configurarLblPregunta(preguntaTieneAnimales,   "¿Tiene otros animalitos?");
        configurarLblPregunta(preguntaEsterilizados,   "¿Están esterilizados?");
        configurarLblPregunta(preguntaTuvoAntes,       "¿Ha tenido otros antes?");
        configurarLblPregunta(preguntaVisitaPeriodica, "¿Está de acuerdo en una visita periódica?");
        configurarLblPregunta(preguntaTodosAcuerdo,    "¿Todos en casa están de acuerdo en adoptar?");
        configurarLblPregunta(preguntaHayNinos,        "¿Hay niños en el hogar?");
        configurarLblPregunta(preguntaAlergicos,       "¿Alguien en casa es alérgico o tiene asma?");
        configurarLblPregunta(preguntaEspacioSuficiente, "¿Cuenta con espacio suficiente para el animal?");
        configurarLblPregunta(preguntaVeterinario,     "¿Tiene veterinario de cabecera?");
        configurarLblPregunta(preguntaRecursos,        "¿Tiene recursos económicos para los gastos?");
        configurarLblPregunta(preguntaEsterilizar,     "¿Se compromete a esterilizar al animal?");
    }

    /** Asigna el texto de la etiqueta al TextView del <include>. */
    private void configurarLblPregunta(View viewPregunta, String etiqueta) {
        if (viewPregunta == null) return;
        TextView lbl = viewPregunta.findViewById(R.id.lbl_pregunta);
        if (lbl != null) lbl.setText(etiqueta);
    }

    // ─────────────────────────────────────────────────────────────
    // Botón adjuntar firma
    // ─────────────────────────────────────────────────────────────

    private void configurarBotonAdjuntarFirma() {
        findViewById(R.id.btn_adjuntar_firma).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES,
                    new String[]{"image/jpeg", "image/png", "image/webp"});
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            launcherFirma.launch(intent);
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Botón Generar y Enviar
    // ─────────────────────────────────────────────────────────────

    private void configurarBotonGenerar() {
        findViewById(R.id.btn_generar_enviar_pdf).setOnClickListener(v -> {
            if (!validarFormulario()) return;
            generarPdfYEnviarPorWhatsApp();
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Validaciones
    // ─────────────────────────────────────────────────────────────

    /**
     * Valida todos los campos obligatorios.
     * @return true si el formulario es válido para enviar.
     */
    private boolean validarFormulario() {
        boolean valido = true;

        // Nombre obligatorio
        if (texto(etApellidosNombres).isEmpty()) {
            etApellidosNombres.setError("Requerido");
            valido = false;
        }

        // DNI: exactamente 8 dígitos numéricos
        String dni = texto(etDni);
        if (dni.isEmpty()) {
            tilDni.setError("El DNI es requerido");
            valido = false;
        } else if (!dni.matches("\\d{8}")) {
            tilDni.setError("El DNI debe tener exactamente 8 dígitos numéricos");
            valido = false;
        } else {
            tilDni.setError(null);
        }

        // Celular: exactamente 9 dígitos
        String celular = texto(etCelular);
        if (celular.isEmpty()) {
            tilCelular.setError("El Celular es requerido");
            valido = false;
        } else if (!celular.matches("\\d{9}")) {
            tilCelular.setError("El Celular debe tener exactamente 9 dígitos");
            valido = false;
        } else {
            tilCelular.setError(null);
        }

        // Acepto condiciones: obligatorio
        if (!cbAceptaCondiciones.isChecked()) {
            Toast.makeText(this,
                    "Debes aceptar las condiciones para continuar", Toast.LENGTH_SHORT).show();
            valido = false;
        }

        if (!valido) {
            Toast.makeText(this, "Por favor corrige los campos marcados en rojo",
                    Toast.LENGTH_SHORT).show();
        }
        return valido;
    }

    // ─────────────────────────────────────────────────────────────
    // Recolección de datos, generación de PDF y envío por WhatsApp
    // ─────────────────────────────────────────────────────────────

    private void generarPdfYEnviarPorWhatsApp() {
        // 1. Recolectar todos los datos en el DTO
        GeneradorPdfAdopcion.DatosFormularioAdopcion datos = recolectarDatos();

        // 2. Bloquear botón mientras se genera
        View btnGenerar = findViewById(R.id.btn_generar_enviar_pdf);
        btnGenerar.setEnabled(false);

        // 3. Generar PDF en hilo de IO (no bloquear UI)
        new Thread(() -> {
            try {
                File archivoPdf = GeneradorPdfAdopcion.generarYGuardar(this, datos);
                runOnUiThread(() -> {
                    btnGenerar.setEnabled(true);
                    enviarPdfPorWhatsApp(archivoPdf, telefonoRefugio, nombreMascota);
                });
            } catch (IOException e) {
                Log.e(TAG, "Error al generar PDF: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    btnGenerar.setEnabled(true);
                    Toast.makeText(this,
                            "Error al generar el PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Recolecta todos los valores del formulario en un DatosFormularioAdopcion.
     */
    private GeneradorPdfAdopcion.DatosFormularioAdopcion recolectarDatos() {
        GeneradorPdfAdopcion.DatosFormularioAdopcion d =
                new GeneradorPdfAdopcion.DatosFormularioAdopcion();

        // Contexto
        d.nombreMascota   = nombreMascota;
        d.nombreRefugio   = nombreRefugio;
        d.telefonoRefugio = telefonoRefugio;

        // Sección 1
        d.apellidosNombres = texto(etApellidosNombres);
        d.telefono         = texto(etTelefono);
        d.fecha            = texto(etFecha);
        d.dni              = texto(etDni);
        d.direccion        = texto(etDireccion);
        d.email            = texto(etEmail);
        d.celular          = texto(etCelular);
        d.estadoCivil      = texto(etEstadoCivil);
        d.ciudad           = texto(etCiudad);
        d.refNombre        = texto(etRefNombre);
        d.refTelefono      = texto(etRefTelefono);

        // Sección 2: RadioGroups Sí/No
        d.tieneAnimales         = leerSiNo(preguntaTieneAnimales);
        d.esterilizados         = leerSiNo(preguntaEsterilizados);
        d.tuvoAntes             = leerSiNo(preguntaTuvoAntes);
        d.visitaPeriodica       = leerSiNo(preguntaVisitaPeriodica);
        d.todosAcuerdo          = leerSiNo(preguntaTodosAcuerdo);
        d.hayNinos              = leerSiNo(preguntaHayNinos);
        d.alergicos             = leerSiNo(preguntaAlergicos);
        d.espacioSuficiente     = leerSiNo(preguntaEspacioSuficiente);
        d.tieneVeterinario      = leerSiNo(preguntaVeterinario);
        d.tieneRecursos         = leerSiNo(preguntaRecursos);
        d.compromisoEsterilizar = leerSiNo(preguntaEsterilizar);

        // Arrendador: grupo especial con 3 opciones
        int arrendadorId = rgArrendador.getCheckedRadioButtonId();
        if      (arrendadorId == R.id.rb_arrendador_si)         d.arrendadorPermite = "Sí";
        else if (arrendadorId == R.id.rb_arrendador_no)         d.arrendadorPermite = "No";
        else if (arrendadorId == R.id.rb_arrendador_casa_propia) d.arrendadorPermite = "Casa propia";
        else                                                      d.arrendadorPermite = "No seleccionado";

        // Sección 3
        d.porQueAdoptar   = texto(etPorQueAdoptar);
        d.cualesAnimales  = texto(etCualesAnimales);
        d.quePasoAntes    = texto(etQuePasoAntes);
        d.edadesNinos     = texto(etEdadesNinos);
        d.mudanza         = texto(etMudanza);
        d.rupturaFamiliar = texto(etRupturaFamiliar);
        d.aniosVida       = texto(etAniosVida);
        d.vision5Anios    = texto(etVision5);
        d.dondeDormira    = texto(etDondeDormira);
        d.horasSolo       = spinnerHorasSolo.getSelectedItem() != null
                ? spinnerHorasSolo.getSelectedItem().toString() : "";
        d.comportamiento  = texto(etComportamiento);
        d.veterinarioInfo = texto(etVeterinarioInfo);

        // Sección 4
        d.gastosMensuales = spinnerGastos.getSelectedItem() != null
                ? spinnerGastos.getSelectedItem().toString() : "";

        List<String> cuidadosSeleccionados = new ArrayList<>();
        if (cbVisitasVet.isChecked())      cuidadosSeleccionados.add("Visitas al veterinario");
        if (cbVacunacion.isChecked())      cuidadosSeleccionados.add("Vacunación");
        if (cbPaseos.isChecked())          cuidadosSeleccionados.add("Paseos con correa");
        if (cbCollarId.isChecked())        cuidadosSeleccionados.add("Collar de identificación");
        if (cbDesparasitacion.isChecked()) cuidadosSeleccionados.add("Desparasitación");
        if (cbCepillado.isChecked())       cuidadosSeleccionados.add("Cepillado");
        if (cbArenero.isChecked())         cuidadosSeleccionados.add("Arenero");
        if (cbCroquetas.isChecked())       cuidadosSeleccionados.add("Croquetas");
        if (cbAguaLimpia.isChecked())      cuidadosSeleccionados.add("Agua limpia");
        d.cuidados = String.join(", ", cuidadosSeleccionados);

        // Sección 5
        d.aceptaCondiciones = cbAceptaCondiciones.isChecked();
        d.uriFirma          = uriFirmaSeleccionada; // null si no se adjuntó

        return d;
    }

    // ─────────────────────────────────────────────────────────────
    // Intent a WhatsApp con PDF adjunto via FileProvider
    // ─────────────────────────────────────────────────────────────

    /**
     * Genera el Intent explícito hacia com.whatsapp, adjunta el PDF mediante
     * FileProvider y abre el chat directamente con el número de la asociación.
     *
     * @param archivoPdf      Archivo PDF generado localmente.
     * @param telefono        Número de WhatsApp (formato internacional, sin +, ej: "51987654321").
     * @param nombreMascota   Para el mensaje de texto de acompañamiento.
     */
    private void enviarPdfPorWhatsApp(File archivoPdf, String telefono, String nombreMascota) {
        // Obtener Uri segura via FileProvider
        Uri uriPdf;
        try {
            uriPdf = FileProvider.getUriForFile(
                    this, FILE_PROVIDER_AUTHORITY, archivoPdf);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "FileProvider error: " + e.getMessage(), e);
            Toast.makeText(this,
                    "Error al preparar el archivo para enviar: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        String mensaje = "Hola, adjunto mi formulario de adopción para "
                + (nombreMascota.isEmpty() ? "la mascota" : nombreMascota)
                + ". Por favor revísenlo. ¡Muchas gracias!";

        // Intent explícito a WhatsApp para abrir chat directo
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage("com.whatsapp");
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uriPdf);
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);

        // Número de teléfono: solo si no está vacío
        if (!telefono.isEmpty()) {
            // WhatsApp acepta el número via URI en el caso de ACTION_VIEW,
            // pero para ACTION_SEND con adjunto usamos el Intent normal y
            // el usuario selecciona el chat. Si el número tiene formato válido
            // usamos el deeplink de WhatsApp para abrir el chat directo.
            intent.putExtra("jid", telefono.replaceAll("[^0-9]", "") + "@s.whatsapp.net");
        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Verificar que WhatsApp esté instalado
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this,
                    "WhatsApp no está instalado en este dispositivo", Toast.LENGTH_LONG).show();
            return;
        }

        startActivity(intent);
        Log.d(TAG, "Intent WhatsApp lanzado con PDF: " + archivoPdf.getName());
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    /** Obtiene el texto de un TextInputEditText de forma segura. */
    private String texto(TextInputEditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }

    /**
     * Lee el valor Sí/No de un <include> item_pregunta_si_no.
     * El view padre contiene un RadioGroup con id @id/rg_si_no,
     * cuyos RadioButtons tienen id @id/rb_si y @id/rb_no.
     */
    private String leerSiNo(View viewPregunta) {
        if (viewPregunta == null) return "No seleccionado";
        RadioGroup rg = viewPregunta.findViewById(R.id.rg_si_no);
        if (rg == null) return "No seleccionado";
        int checked = rg.getCheckedRadioButtonId();
        if      (checked == R.id.rb_si) return "Sí";
        else if (checked == R.id.rb_no) return "No";
        else                            return "No seleccionado";
    }
}
