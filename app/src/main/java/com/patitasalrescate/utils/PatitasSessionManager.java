package com.patitasalrescate.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class PatitasSessionManager {
    private static final String TAG = "PatitasSession";
    private static PatitasSessionManager instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    // El token va en prefs plano separado: el commit del almacén cifrado puede fallar
    // en silencio según el keystore del dispositivo y el token "sobrevivía" al logout.
    private SharedPreferences tokenPrefs;

    // Keys
    private static final String PREF_NAME = "PatitasSession";
    private static final String TOKEN_PREF_NAME = "PatitasToken";
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
        tokenPrefs = ctx.getApplicationContext()
                .getSharedPreferences(TOKEN_PREF_NAME, Context.MODE_PRIVATE);
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
        tokenPrefs.edit().putString(KEY_AUTH_TOKEN, token == null ? "" : token).commit();
    }

    public String getAuthToken() {
        String viejo = prefs.getString(KEY_AUTH_TOKEN, "");
        if (viejo != null && !viejo.isEmpty()
                && tokenPrefs.getString(KEY_AUTH_TOKEN, "").isEmpty()) {
            tokenPrefs.edit().putString(KEY_AUTH_TOKEN, viejo).commit();
            prefs.edit().remove(KEY_AUTH_TOKEN).apply();
        }
        return tokenPrefs.getString(KEY_AUTH_TOKEN, "");
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

    public void setShelterRequestAt(long millis) {
        editor.putLong(KEY_SHELTER_REQUEST_AT, millis);
        editor.apply();
    }

    public long getShelterRequestAt() { return prefs.getLong(KEY_SHELTER_REQUEST_AT, 0); }

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
    public boolean logout() {
        boolean tokenOk = tokenPrefs.edit().clear().commit();
        boolean prefsOk = prefs.edit().clear().commit();
        // Modo siempre definido tras salir: evita estados vacíos que confunden a los fragments.
        boolean modoOk = prefs.edit().putString(KEY_SESSION_MODE, "ADOPTANTE").commit();
        editor = prefs.edit();
        boolean limpio = tokenOk && prefsOk && modoOk && !hasValidToken() && getUserId().isEmpty()
                && isAdoptante();
        android.util.Log.d(TAG, "logout limpio=" + limpio
                + " tokenVálido=" + hasValidToken() + " tokenOk=" + tokenOk + " prefsOk=" + prefsOk);
        return limpio;
    }
}
