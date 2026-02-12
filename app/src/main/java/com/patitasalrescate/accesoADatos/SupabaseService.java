package com.patitasalrescate.accesoADatos;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.patitasalrescate.model.Adoptante;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.model.Refugio;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseService {

    private static final String SUPABASE_URL = "https://sjbuliztalqmsquunnsv.supabase.co";
    // TU ANON KEY (Mantenla segura)
    private static final String ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNqYnVsaXp0YWxxbXNxdXVubnN2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzAwODMwOTEsImV4cCI6MjA4NTY1OTA5MX0.SmTZWaSdO0OFTmHgM4VeBZyErc1O_MQO1be8pKJahoI";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private Request.Builder baseRequest(String endpoint) {
        return new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + endpoint)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer " + ANON_KEY)
                .addHeader("Content-Type", "application/json");
    }

    // --- SUBIDA DE FOTOS ---
    public String subirFoto(byte[] imagenBytes, String nombreArchivo) {
        // Asegúrate de que el bucket se llame 'imagenes-refugio' en Supabase Storage
        String urlStorage = SUPABASE_URL + "/storage/v1/object/imagenes-refugio/" + nombreArchivo;
        RequestBody requestBody = RequestBody.create(imagenBytes, MediaType.parse("image/jpeg"));

        Request request = new Request.Builder()
                .url(urlStorage)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer " + ANON_KEY)
                .addHeader("Content-Type", "image/jpeg")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return SUPABASE_URL + "/storage/v1/object/public/imagenes-refugio/" + nombreArchivo;
            } else {
                android.util.Log.e("SupabaseFoto", "Error subiendo foto: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- INSERTAR REFUGIO (REGISTRO) ---
    public boolean insertarRefugio(Refugio refugio) throws IOException {
        String json = gson.toJson(refugio);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        Request request = baseRequest("refugios") // La tabla se llama 'refugios'
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // ESTO TE DIRÁ EL ERROR EXACTO EN EL LOGCAT
                String errorBody = response.body() != null ? response.body().string() : "Sin mensaje";
                android.util.Log.e("SupabaseError", "❌ CÓDIGO: " + response.code());
                android.util.Log.e("SupabaseError", "❌ MENSAJE: " + errorBody);
                android.util.Log.e("SupabaseError", "❌ JSON ENVIADO: " + json);
                return false;
            }
            android.util.Log.d("SupabaseExito", "✅ Refugio subido correctamente");
            return true;
        }
    }

    // --- OTROS MÉTODOS (Mascotas, Adoptantes...) ---
    public List<Mascota> getMascotas() throws IOException {
        Request request = baseRequest("mascotas?select=*").build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Type listType = new TypeToken<List<Mascota>>(){}.getType();
                return gson.fromJson(response.body().string(), listType);
            }
        }
        return null;
    }

    public boolean insertarMascota(Mascota mascota) throws IOException {
        String json = gson.toJson(mascota);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        Request request = baseRequest("mascotas")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                Log.e("SupabaseMascota", "Error al insertar mascota: Código " + response.code() + " - " + response.message());
                Log.e("SupabaseMascota", "JSON enviado: " + json);
                Log.e("SupabaseMascota", "Respuesta de error: " + errorBody);
                return false;
            }
            Log.d("SupabaseMascota", "Mascota insertada OK: " + mascota.getIdMascota());
            return true;
        }
    }

    public boolean insertarAdoptante(Adoptante adoptante) throws IOException {
        String json = gson.toJson(adoptante);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = baseRequest("adoptantes").post(body).build();
        try (Response response = client.newCall(request).execute()) { return response.isSuccessful(); }
    }

    // Obtener todos los adoptantes de Supabase (para sincronización)
    public List<Adoptante> getAdoptantes() throws IOException {
        Request request = baseRequest("adoptantes?select=*").build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Type listType = new TypeToken<List<Adoptante>>(){}.getType();
                return gson.fromJson(response.body().string(), listType);
            }
        }
        return null;
    }

    // Obtener todos los refugios de Supabase (para sincronización)
    public List<Refugio> getRefugios() throws IOException {
        Request request = baseRequest("refugios?select=*").build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Type listType = new TypeToken<List<Refugio>>(){}.getType();
                return gson.fromJson(response.body().string(), listType);
            }
        }
        return null;
    }

    // Login remoto para adoptante (consulta Supabase)
    public Adoptante loginAdoptanteRemoto(String correo, String passwordEncriptada) throws IOException {
        Request request = baseRequest("adoptantes?correo=eq." + correo + "&password=eq." + passwordEncriptada + "&select=*").build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Type listType = new TypeToken<List<Adoptante>>(){}.getType();
                List<Adoptante> resultados = gson.fromJson(response.body().string(), listType);
                if (!resultados.isEmpty()) {
                    return resultados.get(0);
                }
            }
        }
        return null;
    }

    // Login remoto para refugio (consulta Supabase)
    public Refugio loginRefugioRemoto(String correo, String passwordEncriptada) throws IOException {
        Request request = baseRequest("refugios?correo=eq." + correo + "&password=eq." + passwordEncriptada + "&select=*").build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                Type listType = new TypeToken<List<Refugio>>(){}.getType();
                List<Refugio> resultados = gson.fromJson(response.body().string(), listType);
                if (!resultados.isEmpty()) {
                    return resultados.get(0);
                }
            }
        }
        return null;
    }

    // --- ACTUALIZACIONES (PATCH) ---

    /**
     * Cambia el estado de adoptado en Supabase.
     * Requiere que tu tabla se llame "mascotas" y la PK/filtro sea "id_mascota".
     */
    public boolean actualizarEstadoAdoptado(String idMascota, boolean esAdoptado) throws IOException {
        if (idMascota == null || idMascota.trim().isEmpty()) return false;

        String json = "{\"es_adoptado\":" + (esAdoptado ? "true" : "false") + "}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        // OJO: el filtro asume que el campo en Supabase es id_mascota
        String endpoint = "mascotas?id_mascota=eq." + idMascota;

        Request request = baseRequest(endpoint)
                .patch(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                Log.e("SupabaseMascota", "Error PATCH es_adoptado: Código " + response.code() + " - " + response.message());
                Log.e("SupabaseMascota", "Respuesta: " + errorBody);
                return false;
            }
            return true;
        }
    }

    /**
     * Actualiza datos editables de una mascota en Supabase.
     * (No modifica fotos ni id_refugio; solo campos del perfil)
     */
    public boolean actualizarMascota(Mascota mascota) throws IOException {
        if (mascota == null || mascota.getIdMascota() == null || mascota.getIdMascota().trim().isEmpty()) return false;

        // Armamos un JSON parcial con campos editables
        String json = "{"
                + "\"nombre\":\"" + escapeJson(mascota.getNombre()) + "\","
                + "\"especie\":\"" + escapeJson(mascota.getEspecie()) + "\","
                + "\"raza\":\"" + escapeJson(mascota.getRaza()) + "\","
                + "\"edad\":" + mascota.getEdad() + ","
                + "\"temperamento\":\"" + escapeJson(mascota.getTemperamento()) + "\","
                + "\"historia\":\"" + escapeJson(mascota.getHistoria()) + "\""
                + "}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        String endpoint = "mascotas?id_mascota=eq." + mascota.getIdMascota();

        Request request = baseRequest(endpoint)
                .patch(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                Log.e("SupabaseMascota", "Error PATCH mascota: Código " + response.code() + " - " + response.message());
                Log.e("SupabaseMascota", "JSON enviado: " + json);
                Log.e("SupabaseMascota", "Respuesta: " + errorBody);
                return false;
            }
            return true;
        }
    }

    /** Escape simple para evitar romper JSON si hay comillas o saltos de línea */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

}