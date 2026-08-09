package com.patitasalrescate.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(tableName = "mascotas")
public class MascotaEntity {

    @PrimaryKey
    @NonNull
    private String idMascota;

    private String idRefugio;
    private String nombre;
    private String especie;
    private String raza;
    private String sexo;
    private int edad;
    private String temperamento;
    private String historia;
    private String fotos;           // Guardamos como String (separado por comas)
    private String estado;
    private long lastSync;

    public MascotaEntity() {}

    public MascotaEntity(com.patitasalrescate.model.Mascota mascota) {
        this.idMascota = mascota.getIdMascota() != null ? mascota.getIdMascota() : "";
        this.idRefugio = mascota.getIdRefugio();
        this.nombre = mascota.getNombre();
        this.especie = mascota.getEspecie();
        this.raza = mascota.getRaza();
        this.sexo = mascota.getSexo();
        this.edad = mascota.getEdad();
        this.temperamento = mascota.getTemperamento();
        this.historia = mascota.getHistoria();
        this.fotos = String.join(",", mascota.getFotos() != null ? mascota.getFotos() : new ArrayList<>());
        this.estado = mascota.getEstado();
        this.lastSync = System.currentTimeMillis();
    }

    @NonNull
    public String getIdMascota() { return idMascota; }
    public void setIdMascota(@NonNull String idMascota) { this.idMascota = idMascota; }

    public String getIdRefugio() { return idRefugio; }
    public void setIdRefugio(String idRefugio) { this.idRefugio = idRefugio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getTemperamento() { return temperamento; }
    public void setTemperamento(String temperamento) { this.temperamento = temperamento; }

    public String getHistoria() { return historia; }
    public void setHistoria(String historia) { this.historia = historia; }

    public String getFotos() { return fotos; }
    public void setFotos(String fotos) { this.fotos = fotos; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
}