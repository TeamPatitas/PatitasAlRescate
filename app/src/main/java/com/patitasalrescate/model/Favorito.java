package com.patitasalrescate.model;

public class Favorito {
    private String idAdoptante;
    private String idMascota;
    private long lastSync;
    public Favorito() {}
    public Favorito(String idAdoptante, String idMascota, long lastSync) {
        this.idAdoptante = idAdoptante;
        this.idMascota = idMascota;
        this.lastSync = lastSync;
    }
    public String getIdAdoptante() {
        return idAdoptante;
    }
    public void setIdAdoptante(String idAdoptante) {
        this.idAdoptante = idAdoptante;
    }
    public String getIdMascota() {
        return idMascota;
    }
    public void setIdMascota(String idMascota) {
        this.idMascota = idMascota;
    }
    public long getLastSync() {
        return lastSync;
    }
    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }
}