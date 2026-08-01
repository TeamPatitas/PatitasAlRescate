package com.patitasalrescate.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "adoptantes")
public class AdoptanteEntity {

    @PrimaryKey
    @NonNull
    private String idAdoptante;

    private String nombre;
    private String correo;
    private String numCelular;
    private int edad;
    private String sexo;
    private String password;
    private long lastSync;

    public AdoptanteEntity() {}

    public AdoptanteEntity(com.patitasalrescate.model.Adoptante adoptante) {
        this.idAdoptante = adoptante.getIdAdoptante() != null ? adoptante.getIdAdoptante() : "";
        this.nombre = adoptante.getNombre();
        this.correo = adoptante.getCorreo();
        this.numCelular = adoptante.getNumCelular();
        this.edad = adoptante.getEdad();
        this.sexo = adoptante.getSexo();
        this.password = adoptante.getPassword();
        this.lastSync = System.currentTimeMillis();
    }

    @NonNull
    public String getIdAdoptante() { return idAdoptante; }
    public void setIdAdoptante(@NonNull String idAdoptante) { this.idAdoptante = idAdoptante; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getNumCelular() { return numCelular; }
    public void setNumCelular(String numCelular) { this.numCelular = numCelular; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
}