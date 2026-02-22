package com.patitasalrescate.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Adopcion {
    @SerializedName("id_adopcion")
    @Expose
    private String idAdopcion;

    @SerializedName("id_adoptante")
    @Expose
    private String idAdoptante;

    @SerializedName("id_mascota")
    @Expose
    private String idMascota;

    @SerializedName("id_refugio")
    @Expose
    private String idRefugio;

    @SerializedName("estado")
    @Expose
    private String estado; // Solicitado, Aprobado, Rechazado

    @SerializedName("notas")
    @Expose
    private String notas;
    private transient long lastSync;

    public Adopcion() {}
    public Adopcion(String idAdopcion, String idAdoptante, String idMascota, String idRefugio, String estado, String notas) {
        this.idAdopcion = idAdopcion;
        this.idAdoptante = idAdoptante;
        this.idMascota = idMascota;
        this.idRefugio = idRefugio;
        this.estado = estado;
        this.notas=notas;
        this.lastSync = System.currentTimeMillis();
    }
    public String getIdAdopcion() { return idAdopcion; }
    public void setIdAdopcion(String idAdopcion) { this.idAdopcion = idAdopcion; }

    public String getIdAdoptante() { return idAdoptante; }
    public void setIdAdoptante(String idAdoptante) { this.idAdoptante = idAdoptante; }

    public String getIdMascota() { return idMascota; }
    public void setIdMascota(String idMascota) { this.idMascota = idMascota; }

    public String getIdRefugio() { return idRefugio; }
    public void setIdRefugio(String idRefugio) { this.idRefugio = idRefugio; }


    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
}