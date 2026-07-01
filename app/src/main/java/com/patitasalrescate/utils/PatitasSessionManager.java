package com.patitasalrescate.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PatitasSessionManager {
    private static PatitasSessionManager instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // Keys xd
    private static final String PREF_NAME = "PatitasSession";
    public static final String KEY_USER_ID = "id_user_key";
    public static final String KEY_USER_NAME = "name_user_key";
    public static final String KEY_USER_TYPE = "type_user_key"; // "ADOPTANTE" o "REFUGIO"
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

    public void createSession(String id, String nombre, String tipo) {
        editor.putString(KEY_USER_ID, id);
        editor.putString(KEY_USER_NAME, nombre);
        editor.putString(KEY_USER_TYPE, tipo);
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }
    public String getUserType() {
        return prefs.getString(KEY_USER_TYPE, "");
    }
    public boolean isRefugio() {
        return "REFUGIO".equalsIgnoreCase(getUserType());
    }
    public boolean isAdoptante() {
        return "ADOPTANTE".equalsIgnoreCase(getUserType());
    }
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
