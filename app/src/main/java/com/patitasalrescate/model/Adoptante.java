package com.patitasalrescate.model;

import com.google.gson.annotations.SerializedName;

import com.google.gson.annotations.SerializedName;

public class Adoptante {

    @SerializedName("id_adoptante")
    private String idAdoptante;
    private String nombre;
    private String correo;

    @SerializedName("password")
    private String password;
    @SerializedName("num_celular")
    private String numCelular;
    private int edad;
    private String sexo;

    public Adoptante() {}

    public Adoptante(String idAdoptante, String nombre, String correo, String password,
                     String numCelular, int edad, String sexo, long lastSync) {
        this.idAdoptante = idAdoptante;
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
        this.numCelular = numCelular;
        this.edad = edad;
        this.sexo = sexo;
    }
    public String getIdAdoptante() { return idAdoptante; }
    public void setIdAdoptante(String idAdoptante) { this.idAdoptante = idAdoptante; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNumCelular() { return numCelular; }
    public void setNumCelular(String numCelular) { this.numCelular = numCelular; }
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
}