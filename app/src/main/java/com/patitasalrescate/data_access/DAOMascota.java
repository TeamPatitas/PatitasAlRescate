package com.patitasalrescate.data_access;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.patitasalrescate.model.Mascota;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DAOMascota {
    //TODO: Implementar api
    public DAOMascota(Context context) {

    }

    public long insertar(Mascota mascota) {
        return 0;
    }

    public int actualizar(Mascota mascota) {
        return 0;
    }

    public Mascota obtenerPorId(String idMascota) {
        return new Mascota();
    }

    public List<Mascota> listarPorRefugio(String idRefugio) {
        return new ArrayList<>();
    }

    public List<Mascota> listarDisponibles() {
        return new ArrayList<>();
    }

    public List<Mascota> listarTodos() {
        return new ArrayList<>();
    }

    private Mascota cursorToMascota(Cursor cursor) {
        return new Mascota();
    }
}