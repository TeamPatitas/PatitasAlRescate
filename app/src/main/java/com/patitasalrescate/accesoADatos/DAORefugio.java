package com.patitasalrescate.accesoADatos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.patitasalrescate.model.Refugio;

import java.util.ArrayList;
import java.util.List;

public class DAORefugio {
    private BDConstruir dbHelper;

    public DAORefugio(Context context) {
        dbHelper = new BDConstruir(context);
    }

    public long insertar(Refugio refugio) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_refugio", refugio.getIdRefugio());
        values.put("nombre", refugio.getNombre());
        values.put("direccion", refugio.getDireccion());
        values.put("latitud", refugio.getLatitud());
        values.put("longitud", refugio.getLongitud());
        values.put("correo", refugio.getCorreo());
        values.put("password", refugio.getPassword());
        values.put("num_celular", refugio.getNumCelular());
        values.put("foto", refugio.getFotoUrl());
        values.put("last_sync", refugio.getLastSync());

        return db.insert("refugios", null, values);
    }

    public int actualizar(Refugio refugio) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", refugio.getNombre());
        values.put("direccion", refugio.getDireccion());
        values.put("latitud", refugio.getLatitud());
        values.put("longitud", refugio.getLongitud());
        values.put("correo", refugio.getCorreo());
        values.put("num_celular", refugio.getNumCelular());
        values.put("foto", refugio.getFotoUrl());
        values.put("last_sync", refugio.getLastSync());

        return db.update("refugios", values, "id_refugio = ?",
                new String[]{refugio.getIdRefugio()});
    }

    public Refugio obtenerPorId(String idRefugio) {
        if (idRefugio == null) return null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Refugio ref = null;
        Cursor cursor = db.query("refugios", null, "id_refugio = ?", new String[]{idRefugio}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            ref = new Refugio();
            ref.setIdRefugio(cursor.getString(cursor.getColumnIndexOrThrow("id_refugio")));
            ref.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            ref.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow("direccion")));
            ref.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow("latitud")));
            ref.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow("longitud")));
            ref.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow("correo")));
            ref.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
            ref.setNumCelular(cursor.getString(cursor.getColumnIndexOrThrow("num_celular")));
            ref.setFotoUrl(cursor.getString(cursor.getColumnIndexOrThrow("foto")));
            ref.setLastSync(cursor.getLong(cursor.getColumnIndexOrThrow("last_sync")));
        }
        if (cursor != null) cursor.close();
        return ref;
    }

    public List<Refugio> listarTodos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Refugio> refugios = new ArrayList<>();
        Cursor cursor = db.query("refugios", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Refugio ref = new Refugio();
                ref.setIdRefugio(cursor.getString(cursor.getColumnIndexOrThrow("id_refugio")));
                ref.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                ref.setDireccion(cursor.getString(cursor.getColumnIndexOrThrow("direccion")));
                ref.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow("latitud")));
                ref.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow("longitud")));
                ref.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow("correo")));
                ref.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
                ref.setNumCelular(cursor.getString(cursor.getColumnIndexOrThrow("num_celular")));
                ref.setFotoUrl(cursor.getString(cursor.getColumnIndexOrThrow("foto")));
                ref.setLastSync(cursor.getLong(cursor.getColumnIndexOrThrow("last_sync")));
                refugios.add(ref);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return refugios;
    }

    public Refugio login(String correo, String passwordEncriptada) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("refugios", null, "correo = ? AND password = ?",
                new String[]{correo, passwordEncriptada}, null, null, null);

        if (cursor.moveToFirst()) {
            Refugio ref = new Refugio();
            ref.setIdRefugio(cursor.getString(cursor.getColumnIndexOrThrow("id_refugio")));  // ← String
            ref.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            ref.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow("correo")));
            ref.setFotoUrl(cursor.getString(cursor.getColumnIndexOrThrow("foto")));
            cursor.close();
            return ref;
        }
        cursor.close();
        return null;
    }
    public void eliminar(String idRefugio) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("refugios", "id_refugio = ?", new String[]{idRefugio});
    }
}