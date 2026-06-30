package com.patitasalrescate.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PatitasSessionManager {
    private static PatitasSessionManager instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private static final String PREF_NAME = "PatitasSession";

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
}
