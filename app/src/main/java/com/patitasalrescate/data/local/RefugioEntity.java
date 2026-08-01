package com.patitasalrescate.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "refugios")
public class RefugioEntity {

    @PrimaryKey
    @NonNull                  // ← Esta es la corrección principal
    private String idRefugio;

    private String nombre;
    private String direccion;
    private double latitud;
    private double longitud;
    private String correo;
    private String numCelular;
    private String fotoUrl;
    private long lastSync;

    public RefugioEntity() {}

    public RefugioEntity(com.patitasalrescate.model.Refugio refugio) {
        this.idRefugio = refugio.getIdRefugio() != null ? refugio.getIdRefugio() : "";
        this.nombre = refugio.getNombre();
        this.direccion = refugio.getDireccion();
        this.latitud = refugio.getLatitud();
        this.longitud = refugio.getLongitud();
        this.correo = refugio.getCorreo();
        this.numCelular = refugio.getNumCelular();
        this.fotoUrl = refugio.getFotoUrl();
        this.lastSync = refugio.getLastSync() != 0 ? refugio.getLastSync() : System.currentTimeMillis();
    }

    // Getters y Setters
    @NonNull
    public String getIdRefugio() { return idRefugio; }
    public void setIdRefugio(@NonNull String idRefugio) { this.idRefugio = idRefugio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getNumCelular() { return numCelular; }
    public void setNumCelular(String numCelular) { this.numCelular = numCelular; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
}