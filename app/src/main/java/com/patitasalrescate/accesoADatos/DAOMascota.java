package com.patitasalrescate.accesoADatos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.patitasalrescate.model.Mascota;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DAOMascota {
    private BDConstruir dbHelper;

    public DAOMascota(Context context) {
        dbHelper = new BDConstruir(context);
    }

    public long insertar(Mascota mascota) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_mascota", mascota.getIdMascota());
        values.put("id_refugio", mascota.getIdRefugio());
        values.put("nombre", mascota.getNombre());
        values.put("especie", mascota.getEspecie());
        values.put("raza", mascota.getRaza());
        values.put("sexo", mascota.getSexo());
        values.put("edad", mascota.getEdad());
        values.put("temperamento", mascota.getTemperamento());
        values.put("historia", mascota.getHistoria());
        values.put("fotos", String.join(",", mascota.getFotos()));
        values.put("estado", mascota.getEstado() != null ? mascota.getEstado() : "DISPONIBLE");
        values.put("last_sync", mascota.getLastSync());
        return db.insert("mascotas", null, values);
    }

    public int actualizar(Mascota mascota) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", mascota.getNombre());
        values.put("especie", mascota.getEspecie());
        values.put("raza", mascota.getRaza());
        values.put("sexo", mascota.getSexo());
        values.put("edad", mascota.getEdad());
        values.put("temperamento", mascota.getTemperamento());
        values.put("historia", mascota.getHistoria());
        values.put("estado", mascota.getEstado());
        values.put("last_sync", System.currentTimeMillis());
        return db.update("mascotas", values, "id_mascota = ?", new String[]{mascota.getIdMascota()});
    }

    // ... (Métodos de consulta listarPorRefugio, obtenerPorId, listarDisponibles IGUAL QUE ANTES) ...
    public Mascota obtenerPorId(String idMascota) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("mascotas", null, "id_mascota = ?", new String[]{idMascota}, null, null, null);
        Mascota m = null;
        if (c.moveToFirst()) m = cursorToMascota(c);
        c.close();
        return m;
    }

    public List<Mascota> listarPorRefugio(String idRefugio) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Mascota> lista = new ArrayList<>();
        Cursor c = db.query("mascotas", null, "id_refugio = ?", new String[]{idRefugio}, null, null, null);
        if (c.moveToFirst()) { do { lista.add(cursorToMascota(c)); } while (c.moveToNext()); }
        c.close();
        return lista;
    }

    public List<Mascota> listarDisponibles() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Mascota> lista = new ArrayList<>();
        Cursor c = db.query("mascotas", null, "estado = 'DISPONIBLE'", null, null, null, null);
        if (c.moveToFirst()) { do { lista.add(cursorToMascota(c)); } while (c.moveToNext()); }
        c.close();
        return lista;
    }

    private Mascota cursorToMascota(Cursor cursor) {
        Mascota m = new Mascota();
        m.setIdMascota(cursor.getString(cursor.getColumnIndexOrThrow("id_mascota")));
        m.setIdRefugio(cursor.getString(cursor.getColumnIndexOrThrow("id_refugio")));
        m.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
        m.setEspecie(cursor.getString(cursor.getColumnIndexOrThrow("especie")));
        m.setRaza(cursor.getString(cursor.getColumnIndexOrThrow("raza")));
        m.setSexo(cursor.getString(cursor.getColumnIndexOrThrow("sexo")));
        m.setEdad(cursor.getInt(cursor.getColumnIndexOrThrow("edad")));
        m.setTemperamento(cursor.getString(cursor.getColumnIndexOrThrow("temperamento")));
        m.setHistoria(cursor.getString(cursor.getColumnIndexOrThrow("historia")));
        String f = cursor.getString(cursor.getColumnIndexOrThrow("fotos"));
        m.setFotos(f != null && !f.isEmpty() ? Arrays.asList(f.split(",")) : new ArrayList<>());
        m.setEstado(cursor.getString(cursor.getColumnIndexOrThrow("estado")));
        m.setLastSync(cursor.getLong(cursor.getColumnIndexOrThrow("last_sync")));
        return m;
    }
}