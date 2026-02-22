package com.patitasalrescate.model;

import com.google.gson.annotations.SerializedName;

public class Refugio {

    @SerializedName(value = "id_refugio", alternate = {"idRefugio", "IdRefugio"})
    private String idRefugio;

    private String nombre;
    private String direccion;
    private double latitud;
    private double longitud;
    private String correo;

    @SerializedName("password")
    private String password;

    @SerializedName(value = "num_celular", alternate = {"numCelular"})
    private String numCelular;

    @SerializedName(value = "foto", alternate = {"fotoUrl", "FotoUrl"})
    private String fotoUrl;

    private transient long lastSync;

    public Refugio() {}

    public Refugio(String idRefugio, String nombre, String direccion, double latitud, double longitud,
                   String correo, String password, String numCelular, String fotoUrl, long lastSync) {
        this.idRefugio = idRefugio;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.correo = correo;
        this.password = password;
        this.numCelular = numCelular;
        this.fotoUrl = fotoUrl;
        this.lastSync = lastSync;
    }

    // Getters y Setters
    public String getIdRefugio() { return idRefugio; }
    public void setIdRefugio(String idRefugio) { this.idRefugio = idRefugio; }

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

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNumCelular() { return numCelular; }
    public void setNumCelular(String numCelular) { this.numCelular = numCelular; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
}