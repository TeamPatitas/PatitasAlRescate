package com.patitasalrescate.data_access;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.patitasalrescate.model.Adoptante;

import java.util.ArrayList;
import java.util.List;

public class DAOAdoptante {
    //TODO: Implementar con api
    public DAOAdoptante(Context context) {

    }

    public long insertar(Adoptante adoptante) {
        return 0;
    }

    public List<Adoptante> listarTodos() {
        return new ArrayList<>();
    }

    public Adoptante login(String correo, String passwordEncriptada) {

        return null;
    }

    public int actualizar(Adoptante adoptante) {
        return 0;
    }

    public void eliminar(String idAdoptante) {

    }

    public boolean existeCorreo(String correo) {
        return true;
    }
}