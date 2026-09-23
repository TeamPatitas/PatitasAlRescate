package com.patitasalrescate.utils;

import org.json.JSONObject;
import retrofit2.Response;

/** Reads the API's public error message without exposing request credentials. */
public final class ApiErrors {
    private ApiErrors() { }

    public static String describe(Response<?> response) {
        String fallback = "HTTP " + response.code();
        if (response.errorBody() == null) return fallback;
        try {
            JSONObject body = new JSONObject(response.errorBody().string());
            String message = body.optString("message", "").trim();
            if (message.isEmpty()) return fallback;
            message = message.replaceAll("[\\r\\n\\t]+", " ");
            return fallback + ": " + message.substring(0, Math.min(message.length(), 120));
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
