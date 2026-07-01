package com.patitasalrescate.data_access;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.patitasalrescate.model.Mascota;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DAOFavoritos {

    // TODO: Implementar api

    public DAOFavoritos(Context context) {

    }
    public long addFavorito(String idAdoptante, String idMascota) {
        return 0;
    }
    public void removeFavorito(String idAdoptante, String idMascota) {

    }
    public boolean esFavorito(String idAdoptante, String idMascota) {
        return false;
    }
    public List<String> obtenerIdsFavoritos(String idAdoptante) {

        return new ArrayList<>();
    }
    public List<Mascota> getFavoritosPorAdoptante(String idAdoptante) {
        return new ArrayList<>();
    }
}