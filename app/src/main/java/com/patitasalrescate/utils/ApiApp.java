package com.patitasalrescate.utils;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.patitasalrescate.data.remote.ApiClient;
import com.patitasalrescate.data.remote.dto.*;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.model.Refugio;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/** Shared HTTP session and mappings between the server contract and existing UI models. */
public final class ApiApp {
    private static final ApiClient API = new ApiClient();
    private static android.content.Context APP_CTX;

    private ApiApp() { }

    public static ApiClient client() { return API; }

    /** Llamar una vez al arrancar (MainActivity) para caché y estado de red. */
    public static void init(android.content.Context ctx) {
        APP_CTX = ctx.getApplicationContext();
    }

    /** Contexto para caché/red. Público porque lo usa el interceptor (otro paquete). */
    public static android.content.Context appContext() { return APP_CTX; }

    public static boolean esOnline() {
        android.content.Context ctx = APP_CTX;
        if (ctx == null) return true;
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
                ctx.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        if (cm == null) return true;
        android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && (caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                || caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
                || caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    /** False + aviso si no hay internet (para bloquear mutaciones offline). */
    public static boolean exigirOnline(android.content.Context ui) {
        if (esOnline()) return true;
        android.widget.Toast.makeText(ui, "Sin internet: tus cambios no se guardarán",
                android.widget.Toast.LENGTH_LONG).show();
        return false;
    }

    public static UploadFile upload(Context context, Uri uri) throws IOException {
        String mime = context.getContentResolver().getType(uri);
        if (mime == null || mime.equals("application/octet-stream")) {
            // Los Uri file:// no resuelven tipo en el ContentResolver: deducir por extensión.
            String extUrl = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extUrl != null && !extUrl.isEmpty()) {
                String porExt = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(extUrl.toLowerCase(java.util.Locale.US));
                if (porExt != null) mime = porExt;
            }
        }
        if (!"image/jpeg".equals(mime) && !"image/png".equals(mime) && !"image/webp".equals(mime)) {
            throw new IOException("Tipo de imagen no permitido: " + mime + ". Use jpeg/png/webp.");
        }
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        String name = "photo" + (ext == null ? "" : "." + ext);
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) throw new IOException("No se pudo abrir la imagen");
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                if (out.size() + count > 10 * 1024 * 1024) throw new IOException("Imagen mayor a 10 MB");
                out.write(buffer, 0, count);
            }
            return new UploadFile(name, RequestBody.create(MediaType.get(mime), out.toByteArray()));
        }
    }

    public static Mascota pet(PetSummaryResponse p) {
        Mascota m = new Mascota();
        m.setIdMascota(p.id);
        m.setNombre(p.name);
        m.setFotos(p.photos == null ? new ArrayList<>() : p.photos);
        m.setEstado(Boolean.TRUE.equals(p.available) ? "DISPONIBLE" : "NO_DISPONIBLE");
        return m;
    }

    public static Mascota pet(PetResponse p) {
        Mascota m = new Mascota();
        m.setIdMascota(p.id);
        m.setIdRefugio(p.shelterId);
        m.setNombre(p.name);
        m.setEspecie(p.specie == null ? "" : p.specie.name());
        m.setRaza(p.breed);
        m.setSexo(p.gender == null ? "" : p.gender.name());
        m.setTemperamento(p.temperament);
        m.setHistoria(p.story);
        m.setFotos(p.photos == null ? new ArrayList<>() : p.photos);
        m.setEstado(Boolean.TRUE.equals(p.available) ? "DISPONIBLE" : "NO_DISPONIBLE");
        return m;
    }

    public static Refugio shelter(ShelterSummaryResponse s) {
        Refugio r = new Refugio();
        r.setIdRefugio(s.id);
        r.setNombre(s.name);
        r.setFotoUrl(s.photoUrl);
        return r;
    }

    public static Refugio shelter(ShelterResponse s) {
        Refugio r = new Refugio();
        r.setIdRefugio(s.id);
        r.setNombre(s.name);
        r.setDireccion(s.address);
        r.setNumCelular(s.phoneNumber);
        r.setFotoUrl(s.photoUrl);
        if (s.latitude != null) r.setLatitud(s.latitude);
        if (s.longitude != null) r.setLongitud(s.longitude);
        return r;
    }

    public static Evento event(EventSummaryResponse e) {
        return new Evento(e.id, e.name, e.eventDate, null, e.photoUrl);
    }

    public static Evento event(EventResponse e) {
        Evento event = new Evento(e.id, e.name, e.eventDate, e.description, e.photoUrl);
        try { if (e.latitude != null) event.setLatitud(Double.parseDouble(e.latitude)); } catch (NumberFormatException ignored) { }
        try { if (e.longitude != null) event.setLongitud(Double.parseDouble(e.longitude)); } catch (NumberFormatException ignored) { }
        return event;
    }

    /** "2026-09-30T00:25:00-05:00" -> "30, Sept 2026, a las 12:25 am". Si no parsea, devuelve el original. */
    public static String fechaBonita(String iso) {
        if (iso == null || iso.trim().isEmpty()) return "";
        String[] formatos = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"};
        for (String formato : formatos) {
            try {
                java.text.SimpleDateFormat entrada =
                        new java.text.SimpleDateFormat(formato, java.util.Locale.US);
                entrada.setLenient(false);
                java.util.Date fecha = entrada.parse(iso.trim());
                if (fecha == null) continue;
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(fecha);
                String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                        "Jul", "Ago", "Sept", "Oct", "Nov", "Dic"};
                int hora12 = cal.get(java.util.Calendar.HOUR);
                if (hora12 == 0) hora12 = 12;
                String ampm = cal.get(java.util.Calendar.AM_PM)
                        == java.util.Calendar.AM ? "am" : "pm";
                return cal.get(java.util.Calendar.DAY_OF_MONTH) + ", "
                        + meses[cal.get(java.util.Calendar.MONTH)] + " "
                        + cal.get(java.util.Calendar.YEAR) + ", a las "
                        + hora12 + ":"
                        + String.format(java.util.Locale.US, "%02d",
                        cal.get(java.util.Calendar.MINUTE)) + " " + ampm;
            } catch (Exception ignored) { }
        }
        return iso;
    }
}
