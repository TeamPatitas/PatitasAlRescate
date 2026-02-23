package com.patitasalrescate.accesoADatos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.patitasalrescate.model.Adoptante;

import java.util.ArrayList;
import java.util.List;

public class DAOAdoptante {

    private BDConstruir dbHelper;

    public DAOAdoptante(Context context) {
        dbHelper = new BDConstruir(context);
    }

    public long insertar(Adoptante adoptante) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_adoptante", adoptante.getIdAdoptante());
        values.put("nombre", adoptante.getNombre());
        values.put("correo", adoptante.getCorreo());
        values.put("password", adoptante.getPassword());
        values.put("num_celular", adoptante.getNumCelular());
        values.put("edad", adoptante.getEdad());
        values.put("sexo", adoptante.getSexo());
        values.put("last_sync", adoptante.getLastSync());

        return db.insert("adoptantes", null, values);
    }

    public List<Adoptante> listarTodos() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Adoptante> adoptantes = new ArrayList<>();
        Cursor cursor = db.query("adoptantes", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Adoptante adopt = new Adoptante();
                adopt.setIdAdoptante(cursor.getString(cursor.getColumnIndexOrThrow("id_adoptante")));
                adopt.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                adopt.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow("correo")));
                adopt.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
                adopt.setNumCelular(cursor.getString(cursor.getColumnIndexOrThrow("num_celular")));
                adopt.setEdad(cursor.getInt(cursor.getColumnIndexOrThrow("edad")));
                adopt.setSexo(cursor.getString(cursor.getColumnIndexOrThrow("sexo")));
                adopt.setLastSync(cursor.getLong(cursor.getColumnIndexOrThrow("last_sync")));
                adoptantes.add(adopt);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return adoptantes;
    }

    public Adoptante login(String correo, String passwordEncriptada) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("adoptantes", null, "correo = ? AND password = ?",
                new String[]{correo, passwordEncriptada}, null, null, null);

        if (cursor.moveToFirst()) {
            Adoptante adopt = new Adoptante();
            adopt.setIdAdoptante(cursor.getString(cursor.getColumnIndexOrThrow("id_adoptante")));
            adopt.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            adopt.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow("correo")));
            adopt.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
            adopt.setNumCelular(cursor.getString(cursor.getColumnIndexOrThrow("num_celular")));
            adopt.setEdad(cursor.getInt(cursor.getColumnIndexOrThrow("edad")));
            adopt.setSexo(cursor.getString(cursor.getColumnIndexOrThrow("sexo")));
            adopt.setLastSync(cursor.getLong(cursor.getColumnIndexOrThrow("last_sync")));
            cursor.close();
            return adopt;
        }
        cursor.close();
        return null;
    }

    public int actualizar(Adoptante adoptante) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", adoptante.getNombre());
        values.put("num_celular", adoptante.getNumCelular());
        values.put("edad", adoptante.getEdad());
        values.put("sexo", adoptante.getSexo());
        values.put("last_sync", adoptante.getLastSync());

        return db.update("adoptantes", values, "id_adoptante = ?",
                new String[]{adoptante.getIdAdoptante()});
    }

    public void eliminar(String idAdoptante) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("adoptantes", "id_adoptante = ?", new String[]{idAdoptante});
    }

    public boolean existeCorreo(String correo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("adoptantes", new String[]{"id_adoptante"},
                "correo = ?", new String[]{correo}, null, null, null);
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }
}