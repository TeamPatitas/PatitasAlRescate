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

    private ApiApp() { }

    public static ApiClient client() { return API; }

    public static UploadFile upload(Context context, Uri uri) throws IOException {
        String mime = context.getContentResolver().getType(uri);
        if (mime == null) mime = "application/octet-stream";
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
}
