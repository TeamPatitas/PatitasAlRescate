package com.patitasalrescate.utils;

import android.content.Context;
import android.content.SharedPreferences;

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

    private PatitasSessionManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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

    public String getShelterId() { return prefs.getString(KEY_SHELTER_ID, ""); }
    public boolean canManageShelter() { return prefs.getBoolean(KEY_SHELTER_OWNER, false); }

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
