package com.patitasalrescate.accesoADatos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BDConstruir extends SQLiteOpenHelper {

    private static final String DB_NAME = "patitas_db";
    private static final int DB_VERSION = 11;
    public BDConstruir(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE refugios (id_refugio TEXT PRIMARY KEY, nombre TEXT NOT NULL, direccion TEXT, latitud REAL, longitud REAL, correo TEXT, password TEXT, num_celular TEXT, foto TEXT, last_sync INTEGER)");

        db.execSQL("CREATE TABLE mascotas (" +
                "id_mascota TEXT PRIMARY KEY, " +
                "id_refugio TEXT, " +
                "nombre TEXT, " +
                "especie TEXT NOT NULL, " +
                "raza TEXT, " +
                "sexo TEXT, " +
                "edad INTEGER, " +
                "temperamento TEXT, " +
                "historia TEXT, " +
                "fotos TEXT, " +
                "estado TEXT DEFAULT 'DISPONIBLE', " +
                "last_sync INTEGER" +
                ")");

        db.execSQL("CREATE TABLE adoptantes (id_adoptante TEXT PRIMARY KEY, nombre TEXT, correo TEXT, password TEXT, num_celular TEXT, edad INTEGER, sexo TEXT, last_sync INTEGER)");
        db.execSQL("CREATE TABLE favoritos (id_adoptante TEXT, id_mascota TEXT, last_sync INTEGER, PRIMARY KEY (id_adoptante, id_mascota))");
        db.execSQL("CREATE TABLE adopciones (" +
                "id_adopcion TEXT PRIMARY KEY, " +
                "id_adoptante TEXT, " +
                "id_mascota TEXT, " +
                "id_refugio TEXT, " +
                "fecha_adopcion TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "estado TEXT, " +
                "notas TEXT, " +
                "last_sync INTEGER" +
                ")");    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS adopciones");
        db.execSQL("DROP TABLE IF EXISTS favoritos");
        db.execSQL("DROP TABLE IF EXISTS mascotas");
        db.execSQL("DROP TABLE IF EXISTS adoptantes");
        db.execSQL("DROP TABLE IF EXISTS refugios");
        onCreate(db);
    }
}