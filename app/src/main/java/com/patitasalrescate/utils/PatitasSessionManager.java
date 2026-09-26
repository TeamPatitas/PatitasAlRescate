package com.patitasalrescate.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class PatitasSessionManager {
    private static PatitasSessionManager instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // Keys
    private static final String PREF_NAME = "PatitasSession";
    public static final String KEY_USER_ID = "id_user_key";
    public static final String KEY_USER_NAME = "name_user_key";
    public static final String KEY_SESSION_MODE = "session_mode";
    public static final String KEY_SHELTER_ID = "shelter_id";
    public static final String KEY_SHELTER_OWNER = "shelter_owner";
    public static final String KEY_AUTH_TOKEN = "auth_token";
    public static final String KEY_SHELTER_REQUEST_AT = "shelter_request_at";

    private PatitasSessionManager(Context ctx) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            prefs = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    ctx.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to create EncryptedSharedPreferences", e);
        }
        editor = prefs.edit();
    }

    public static synchronized PatitasSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new PatitasSessionManager(context);
        }
        return instance;
    }

    public void createSession(String id, String nombre, String session_mode) {
        createSession(id, nombre, session_mode, "");
    }

    public void createSession(String id, String nombre, String session_mode, String shelterId) {
        createSession(id, nombre, session_mode, shelterId, prefs.getBoolean(KEY_SHELTER_OWNER, false));
    }

    public void createSession(String id, String nombre, String session_mode, String shelterId, boolean shelterOwner) {
        editor.putString(KEY_USER_ID, id);
        editor.putString(KEY_USER_NAME, nombre);
        editor.putString(KEY_SESSION_MODE, session_mode);
        editor.putString(KEY_SHELTER_ID, shelterId);
        editor.putBoolean(KEY_SHELTER_OWNER, shelterOwner);
        editor.apply();
    }

    public void setAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, "");
    }

    public boolean hasValidToken() {
        String token = getAuthToken();
        return token != null && !token.isEmpty();
    }

    public String getShelterId() { return prefs.getString(KEY_SHELTER_ID, ""); }
    public boolean canManageShelter() { return prefs.getBoolean(KEY_SHELTER_OWNER, false); }

    public void setShelterId(String shelterId) {
        editor.putString(KEY_SHELTER_ID, shelterId == null ? "" : shelterId);
        editor.apply();
    }

    /** Momento (millis) en que se solicitó/creó el refugio. 0 = desconocido. */
    public void setShelterRequestAt(long millis) {
        editor.putLong(KEY_SHELTER_REQUEST_AT, millis);
        editor.apply();
    }

    public long getShelterRequestAt() { return prefs.getLong(KEY_SHELTER_REQUEST_AT, 0); }

    /** True si la solicitud de refugio tiene menos de 24h. */
    public boolean tieneSolicitudReciente() {
        long at = getShelterRequestAt();
        return at > 0 && System.currentTimeMillis() - at < 24L * 60 * 60 * 1000;
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }
    public String getSessionType() {
        return prefs.getString(KEY_SESSION_MODE, "");
    }
    public boolean isRefugio() {
        return getSessionType().equals("REFUGIO");
    }
    public boolean isAdoptante() {
        return getSessionType().equals("ADOPTANTE");
    }
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
