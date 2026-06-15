package com.patitasalrescate.data_access;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.patitasalrescate.model.Refugio;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiRefugiosSimulada {


    private static final String BASE_URL = "https://patitasrefugiosapi.onrender.com";





    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public List<Refugio> getRefugios() throws IOException {
        String urlFinal = BASE_URL + "/api/Refugios";

        Log.d("ApiRefugios", "Intentando conectar a: " + urlFinal);

        Request request = new Request.Builder()
                .url(urlFinal)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("ApiRefugios", "Error HTTP: Código " + response.code() + " - " + response.message());
                return null;
            }

            String json = response.body().string();
            Log.d("ApiRefugios", "JSON recibido correctamente: " + json.substring(0, Math.min(json.length(), 300)) + "...");

            Type listType = new TypeToken<List<Refugio>>(){}.getType();
            List<Refugio> lista = gson.fromJson(json, listType);

            Log.d("ApiRefugios", "¡ÉXITO! Se obtuvieron " + lista.size() + " refugios de la API");
            return lista;
        }
    }
}