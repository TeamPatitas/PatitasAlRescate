package com.patitasalrescate.data_access;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.patitasalrescate.model.Refugio;

import java.util.ArrayList;
import java.util.List;

public class DAORefugio {
    //TODO: Implementar api
    public DAORefugio(Context context) {

    }

    public long insertar(Refugio refugio) {
       return 0;
    }

    public int actualizar(Refugio refugio) {
        return 0;
    }

    public Refugio obtenerPorId(String idRefugio) {
        return new Refugio();
    }

    public List<Refugio> listarTodos() {
        return new ArrayList<>();
    }
}