package com.patitasalrescate.data_access;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.patitasalrescate.model.Adopcion;

public class DAOAdopcion {
    private BDConstruir dbHelper;

    public DAOAdopcion(Context context) {
        dbHelper = new BDConstruir(context);
    }

    public long insertar(Adopcion adopcion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_adopcion", adopcion.getIdAdopcion());
        values.put("id_adoptante", adopcion.getIdAdoptante());
        values.put("id_mascota", adopcion.getIdMascota());
        values.put("id_refugio", adopcion.getIdRefugio());
        values.put("estado", adopcion.getEstado());
        values.put("notas", adopcion.getNotas());
        values.put("last_sync", adopcion.getLastSync());

        return db.insert("adopciones", null, values);
    }
}