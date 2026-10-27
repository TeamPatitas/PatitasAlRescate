package com.patitasalrescate.controllers.management;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Clase de dominio puro (sin dependencias de Android UI) que encapsula la lógica
 * de construcción del PdfDocument de adopción.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Recibir un {@link DatosFormularioAdopcion} con todos los campos del formulario.</li>
 *   <li>Dibujar el documento estructurado: encabezado, secciones, cuestionario, compromisos.</li>
 *   <li>Incrustar la foto de firma si fue adjuntada; si no, dejar el marcador textual.</li>
 *   <li>Guardar el PDF en caché y devolver el {@link File} resultante.</li>
 * </ul>
 *
 * <p>No lanza Intents ni hace llamadas de red — esas responsabilidades quedan en
 * {@link ActividadFormularioAdopcion}.
 */
public final class GeneradorPdfAdopcion {

    private static final String TAG = "GeneradorPdf";

    // Dimensiones de página A4 a 72 DPI (estándar PdfDocument Android)
    private static final int PAGE_WIDTH  = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN      = 40;
    private static final int LINE_SPACING = 16;
    private static final int SECTION_SPACING = 24;

    private GeneradorPdfAdopcion() { /* Clase estática utilitaria */ }

    // ─────────────────────────────────────────────────────────────
    // Punto de entrada público
    // ─────────────────────────────────────────────────────────────

    /**
     * Genera el PDF y lo guarda en la carpeta de caché de la aplicación.
     *
     * @param context  Contexto de la Activity/Application.
     * @param datos    Todos los datos capturados del formulario.
     * @return         El {@link File} guardado, listo para ser compartido via FileProvider.
     * @throws IOException Si no se puede escribir el archivo.
     */
    public static File generarYGuardar(Context context, DatosFormularioAdopcion datos)
            throws IOException {

        PdfDocument document = new PdfDocument();
        Paint paintTitulo    = crearPaint(16, true,  Color.rgb(93, 64, 55));   // marronOscuro
        Paint paintSeccion   = crearPaint(13, true,  Color.rgb(121, 85, 72));  // marronPrimario
        Paint paintCuerpo    = crearPaint(10, false, Color.rgb(33, 33, 33));   // textoPrincipal
        Paint paintGris      = crearPaint(9,  false, Color.rgb(117, 117, 117));// textoSecundario
        Paint paintLinea     = new Paint();
        paintLinea.setColor(Color.rgb(189, 189, 189));
        paintLinea.setStrokeWidth(0.5f);

        // ── PÁGINA 1 ─────────────────────────────────────────────
        PdfDocument.PageInfo pageInfo1 =
                new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page1 = document.startPage(pageInfo1);
        Canvas c = page1.getCanvas();

        int y = MARGIN;

        // Encabezado
        y = dibujarTexto(c, "FORMULARIO DE ADOPCIÓN", MARGIN, y, paintTitulo, PAGE_WIDTH - MARGIN * 2);
        y = dibujarTexto(c, datos.nombreMascota.isEmpty() ? "Mascota" : datos.nombreMascota,
                MARGIN, y + 4, paintSeccion, PAGE_WIDTH - MARGIN * 2);
        y = dibujarTexto(c, "Asociación: " + datos.nombreRefugio, MARGIN, y + 2, paintGris,
                PAGE_WIDTH - MARGIN * 2);
        c.drawLine(MARGIN, y + 6, PAGE_WIDTH - MARGIN, y + 6, paintLinea);
        y += 16;

        // ── Sección 1: Datos Personales ───────────────────────────
        y = dibujarEncabezadoSeccion(c, "1. DATOS PERSONALES DEL ADOPTANTE", y, paintSeccion, paintLinea);
        y = dibujarCampo(c, "Apellidos y Nombres", datos.apellidosNombres, y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Teléfono", datos.telefono, y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Fecha", datos.fecha, y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "DNI", datos.dni, y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Dirección", datos.direccion, y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "E-mail", datos.email, y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Celular", datos.celular, y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Estado Civil", datos.estadoCivil, y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Ciudad", datos.ciudad, y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Referencia Personal", datos.refNombre + " | " + datos.refTelefono,
                y, paintCuerpo, paintGris);

        // ── Sección 2: Cuestionario Sí/No ─────────────────────────
        y = dibujarEncabezadoSeccion(c, "2. CUESTIONARIO SÍ / NO", y, paintSeccion, paintLinea);
        y = dibujarCampo(c, "¿Tiene otros animalitos?",        datos.tieneAnimales,      y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Están esterilizados?",           datos.esterilizados,      y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Ha tenido otros antes?",         datos.tuvoAntes,          y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Acuerdo en visita periódica?",   datos.visitaPeriodica,    y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Todos de acuerdo en adoptar?",   datos.todosAcuerdo,       y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Hay niños?",                     datos.hayNinos,           y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Alérgicos / Asma?",              datos.alergicos,          y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Arrendadores permiten?",         datos.arrendadorPermite,  y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Espacio suficiente?",            datos.espacioSuficiente,  y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Veterinario de cabecera?",       datos.tieneVeterinario,   y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Recursos para gastos?",          datos.tieneRecursos,      y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Compromiso de esterilizar?",     datos.compromisoEsterilizar, y, paintCuerpo, paintGris);

        // Salto a página 2 si queda poco espacio (< 200px)
        if (y > PAGE_HEIGHT - 200) {
            document.finishPage(page1);
            PdfDocument.PageInfo pageInfo2 =
                    new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create();
            page1 = document.startPage(pageInfo2);
            c = page1.getCanvas();
            y = MARGIN;
        }

        // ── Sección 3: Preguntas Abiertas ─────────────────────────
        y = dibujarEncabezadoSeccion(c, "3. PREGUNTAS ABIERTAS", y, paintSeccion, paintLinea);
        y = dibujarCampo(c, "¿Por qué desea adoptar?",               datos.porQueAdoptar,   y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Cuáles animales tiene?",               datos.cualesAnimales,  y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Qué pasó con los anteriores?",         datos.quePasoAntes,    y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Edades de los niños",                    datos.edadesNinos,     y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "En caso de mudanza",                     datos.mudanza,         y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "En caso de ruptura familiar",            datos.rupturaFamiliar, y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Años promedio de vida",                  datos.aniosVida,       y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Visión en 5 años",                       datos.vision5Anios,    y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "¿Dónde dormirá?",                       datos.dondeDormira,    y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Horas solo al día",                      datos.horasSolo,       y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Ante comportamiento no deseado",         datos.comportamiento,  y, paintCuerpo, paintGris);
        y = dibujarCampo(c, "Veterinario (nombre y teléfono)",        datos.veterinarioInfo, y, paintCuerpo, paintGris);

        // ── Sección 4: Gastos y Cuidados ──────────────────────────
        if (y > PAGE_HEIGHT - 160) {
            document.finishPage(page1);
            PdfDocument.PageInfo pInfo =
                    new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, document.getPages().size() + 1).create();
            page1 = document.startPage(pInfo);
            c = page1.getCanvas();
            y = MARGIN;
        }
        y = dibujarEncabezadoSeccion(c, "4. GASTOS Y CUIDADOS", y, paintSeccion, paintLinea);
        y = dibujarCampo(c, "Gastos mensuales estimados (S/.)", datos.gastosMensuales, y, paintCuerpo, paintGris);
        if (!datos.cuidados.isEmpty()) {
            y = dibujarCampo(c, "Cuidados comprometidos", datos.cuidados, y, paintCuerpo, paintGris);
        }

        // ── Sección 5: Compromisos y Firma ────────────────────────
        if (y > PAGE_HEIGHT - 220) {
            document.finishPage(page1);
            PdfDocument.PageInfo pInfo =
                    new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, document.getPages().size() + 1).create();
            page1 = document.startPage(pInfo);
            c = page1.getCanvas();
            y = MARGIN;
        }
        y = dibujarEncabezadoSeccion(c, "5. COMPROMISOS Y FIRMA", y, paintSeccion, paintLinea);
        y = dibujarCampo(c, "Acepta condiciones", datos.aceptaCondiciones ? "SÍ" : "NO",
                y, paintCuerpo, paintGris);

        y += SECTION_SPACING;

        // Firma: foto opcional o marcador de texto
        if (datos.uriFirma != null) {
            y = dibujarFirmaDesdeUri(context, c, datos.uriFirma, y, paintGris);
        } else {
            y = dibujarTexto(c, "[Firma no adjuntada]", MARGIN, y, paintGris, PAGE_WIDTH - MARGIN * 2);
        }

        document.finishPage(page1);

        // ── Guardar en caché ──────────────────────────────────────
        File cacheDir = new File(context.getCacheDir(), "pdfs_adopcion");
        if (!cacheDir.exists()) cacheDir.mkdirs();

        // Nombre único por mascota + timestamp para evitar colisiones
        String nombreArchivo = "adopcion_"
                + datos.nombreMascota.replaceAll("[^a-zA-Z0-9]", "_")
                + "_" + System.currentTimeMillis() + ".pdf";
        File archivoPdf = new File(cacheDir, nombreArchivo);

        try (FileOutputStream fos = new FileOutputStream(archivoPdf)) {
            document.writeTo(fos);
        } finally {
            document.close();
        }

        Log.d(TAG, "PDF generado: " + archivoPdf.getAbsolutePath()
                + " (" + archivoPdf.length() / 1024 + " KB)");
        return archivoPdf;
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers de dibujo
    // ─────────────────────────────────────────────────────────────

    private static Paint crearPaint(int sp, boolean negrita, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(sp);
        p.setFakeBoldText(negrita);
        return p;
    }

    /** Dibuja texto con salto de línea automático y devuelve el nuevo y. */
    private static int dibujarTexto(Canvas c, String texto, int x, int y, Paint paint, int maxAncho) {
        if (texto == null || texto.isEmpty()) return y;
        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();
        int yActual = y + (int) paint.getTextSize();

        for (String palabra : palabras) {
            String candidato = lineaActual.length() == 0
                    ? palabra : lineaActual + " " + palabra;
            float ancho = paint.measureText(candidato);
            if (ancho > maxAncho && lineaActual.length() > 0) {
                c.drawText(lineaActual.toString(), x, yActual, paint);
                yActual += LINE_SPACING;
                lineaActual = new StringBuilder(palabra);
            } else {
                lineaActual = new StringBuilder(candidato);
            }
        }
        if (lineaActual.length() > 0) {
            c.drawText(lineaActual.toString(), x, yActual, paint);
            yActual += LINE_SPACING;
        }
        return yActual;
    }

    private static int dibujarEncabezadoSeccion(Canvas c, String titulo, int y,
                                                 Paint paintSeccion, Paint paintLinea) {
        y += SECTION_SPACING;
        y = dibujarTexto(c, titulo, MARGIN, y, paintSeccion, PAGE_WIDTH - MARGIN * 2);
        c.drawLine(MARGIN, y + 2, PAGE_WIDTH - MARGIN, y + 2, paintLinea);
        return y + 8;
    }

    private static int dibujarCampo(Canvas c, String etiqueta, String valor,
                                     int y, Paint paintCuerpo, Paint paintGris) {
        y = dibujarTexto(c, etiqueta + ":", MARGIN, y, paintGris, PAGE_WIDTH - MARGIN * 2);
        String v = (valor == null || valor.trim().isEmpty()) ? "—" : valor.trim();
        y = dibujarTexto(c, "  " + v, MARGIN, y, paintCuerpo, PAGE_WIDTH - MARGIN * 2);
        return y + 4;
    }

    /** Intenta cargar y dibujar la foto de firma. Si falla, dibuja el marcador textual. */
    private static int dibujarFirmaDesdeUri(Context context, Canvas c, Uri uriFirma,
                                             int y, Paint paintGris) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uriFirma)) {
            if (inputStream == null) throw new IOException("InputStream nulo para " + uriFirma);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) throw new IOException("No se pudo decodificar la imagen de firma");

            // Reescalar para caber en el PDF manteniendo proporción
            int maxAncho = PAGE_WIDTH - MARGIN * 2;
            int maxAlto  = 120;
            float ratio  = Math.min((float) maxAncho / bitmap.getWidth(),
                                    (float) maxAlto  / bitmap.getHeight());
            int anchoDst = (int) (bitmap.getWidth()  * ratio);
            int altoDst  = (int) (bitmap.getHeight() * ratio);

            Bitmap escalado = Bitmap.createScaledBitmap(bitmap, anchoDst, altoDst, true);
            bitmap.recycle();

            c.drawBitmap(escalado, MARGIN, y, null);
            escalado.recycle();
            y += altoDst + LINE_SPACING;
        } catch (IOException e) {
            Log.w(TAG, "No se pudo cargar la foto de firma: " + e.getMessage());
            y = (int) (y + new Paint().getTextSize() * 2);
            c.drawText("[Error al cargar la firma adjuntada]", MARGIN, y, paintGris);
            y += LINE_SPACING * 2;
        }
        return y;
    }

    // ─────────────────────────────────────────────────────────────
    // Modelo de datos del formulario (DTO interno)
    // ─────────────────────────────────────────────────────────────

    /**
     * Contiene todos los datos capturados del formulario de adopción.
     * Puro POJO — sin dependencias de Android UI.
     */
    public static final class DatosFormularioAdopcion {
        // Contexto del documento
        public String nombreMascota  = "";
        public String nombreRefugio  = "";
        public String telefonoRefugio = "";

        // Sección 1: Datos personales
        public String apellidosNombres = "";
        public String telefono         = "";
        public String fecha            = "";
        public String dni              = "";
        public String direccion        = "";
        public String email            = "";
        public String celular          = "";
        public String estadoCivil      = "";
        public String ciudad           = "";
        public String refNombre        = "";
        public String refTelefono      = "";

        // Sección 2: Cuestionario Sí/No
        public String tieneAnimales        = "";
        public String esterilizados        = "";
        public String tuvoAntes            = "";
        public String visitaPeriodica      = "";
        public String todosAcuerdo         = "";
        public String hayNinos             = "";
        public String alergicos            = "";
        public String arrendadorPermite    = "";
        public String espacioSuficiente    = "";
        public String tieneVeterinario     = "";
        public String tieneRecursos        = "";
        public String compromisoEsterilizar = "";

        // Sección 3: Preguntas abiertas
        public String porQueAdoptar   = "";
        public String cualesAnimales  = "";
        public String quePasoAntes    = "";
        public String edadesNinos     = "";
        public String mudanza         = "";
        public String rupturaFamiliar = "";
        public String aniosVida       = "";
        public String vision5Anios    = "";
        public String dondeDormira    = "";
        public String horasSolo       = "";
        public String comportamiento  = "";
        public String veterinarioInfo = "";

        // Sección 4: Gastos y cuidados
        public String gastosMensuales = "";
        public String cuidados        = ""; // lista separada por comas

        // Sección 5: Compromisos
        public boolean aceptaCondiciones = false;
        public Uri     uriFirma          = null; // null = sin firma adjuntada
    }
}
