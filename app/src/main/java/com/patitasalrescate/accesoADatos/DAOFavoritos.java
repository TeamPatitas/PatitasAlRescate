package com.patitasalrescate.accesoADatos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.patitasalrescate.model.Mascota;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DAOFavoritos {
    private BDConstruir dbHelper;

    public DAOFavoritos(Context context) {
        dbHelper = new BDConstruir(context);
    }

    // Agregar favorito
    public long addFavorito(String idAdoptante, String idMascota) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_adoptante", idAdoptante);
        values.put("id_mascota", idMascota);
        values.put("last_sync", System.currentTimeMillis());

        // Usamos insertWithOnConflict con IGNORE para que si ya existe, no de error
        return db.insertWithOnConflict("favoritos", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    // Eliminar favorito
    public void removeFavorito(String idAdoptante, String idMascota) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("favoritos", "id_adoptante = ? AND id_mascota = ?", new String[]{idAdoptante, idMascota});
    }

    // Verificar si ya es favorito
    public boolean esFavorito(String idAdoptante, String idMascota) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("favoritos", new String[]{"id_mascota"},
                "id_adoptante = ? AND id_mascota = ?",
                new String[]{idAdoptante, idMascota}, null, null, null);
        boolean existe = (cursor.getCount() > 0);
        cursor.close();
        return existe;
    }

    // Listar favoritos de un adoptante (JOIN con Mascotas)
    public List<Mascota> getFavoritosPorAdoptante(String idAdoptante) {
        List<Mascota> favoritos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta SQL: Une la tabla Mascotas con Favoritos
        String query = "SELECT m.* FROM mascotas m JOIN favoritos f ON m.id_mascota = f.id_mascota WHERE f.id_adoptante = ?";
        Cursor cursor = db.rawQuery(query, new String[]{idAdoptante});

        if (cursor.moveToFirst()) {
            do {
                Mascota masc = new Mascota();

                // --- MAPEO DE DATOS ACTUALIZADO ---
                masc.setIdMascota(cursor.getString(cursor.getColumnIndexOrThrow("id_mascota")));
                masc.setIdRefugio(cursor.getString(cursor.getColumnIndexOrThrow("id_refugio")));
                masc.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                masc.setEspecie(cursor.getString(cursor.getColumnIndexOrThrow("especie")));
                masc.setRaza(cursor.getString(cursor.getColumnIndexOrThrow("raza")));

                masc.setSexo(cursor.getString(cursor.getColumnIndexOrThrow("sexo")));

                masc.setEdad(cursor.getInt(cursor.getColumnIndexOrThrow("edad")));
                masc.setTemperamento(cursor.getString(cursor.getColumnIndexOrThrow("temperamento")));
                masc.setHistoria(cursor.getString(cursor.getColumnIndexOrThrow("historia")));

                // Conversión de fotos
                String fotosStr = cursor.getString(cursor.getColumnIndexOrThrow("fotos"));
                if (fotosStr != null && !fotosStr.isEmpty()) {
                    masc.setFotos(Arrays.asList(fotosStr.split(",")));
                } else {
                    masc.setFotos(new ArrayList<>());
                }

                String estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"));
                masc.setEstado(estado != null ? estado : "DISPONIBLE");

                masc.setLastSync(cursor.getLong(cursor.getColumnIndexOrThrow("last_sync")));

                favoritos.add(masc);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return favoritos;
    }
}