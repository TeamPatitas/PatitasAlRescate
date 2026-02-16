package com.patitasalrescate.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Mascota {

    // --- ID ---
    @SerializedName("id_mascota")
    @Expose
    private String idMascota;

    @SerializedName("id_refugio")
    @Expose
    private String idRefugio;

    // --- DATOS BÁSICOS ---
    @SerializedName("nombre")
    @Expose
    private String nombre;

    @SerializedName("especie")
    @Expose
    private String especie;

    @SerializedName("raza")
    @Expose
    private String raza;

    @SerializedName("edad")
    @Expose
    private int edad;

    @SerializedName("temperamento")
    @Expose
    private String temperamento;

    @SerializedName("historia")
    @Expose
    private String historia;

    @SerializedName("fotos")
    @Expose
    private List<String> fotos = new ArrayList<>();

    // --- NUEVO ESTADO (SUPABASE) ---
    @SerializedName("estado")
    @Expose
    private String estado; // DISPONIBLE | EN_PROCESO | ADOPTADO

    // --- ESTADO ANTIGUO (Compatibilidad local) ---
    @SerializedName("es_adoptado")
    @Expose
    private boolean esAdoptado;

    // --- INTERNO (No se envía a la nube) ---
    private transient long lastSync;

    public Mascota() {}

    public Mascota(String idMascota, String idRefugio, String nombre, String especie, String raza,
                   int edad, String temperamento, String historia, List<String> fotos,
                   String estado, boolean esAdoptado, long lastSync) {
        this.idMascota = idMascota;
        this.idRefugio = idRefugio;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.edad = edad;
        this.temperamento = temperamento;
        this.historia = historia;
        this.fotos = fotos != null ? fotos : new ArrayList<>();
        this.estado = estado;
        this.esAdoptado = esAdoptado;
        this.lastSync = lastSync;
    }

    // Getters y Setters

    public String getIdMascota() { return idMascota; }
    public void setIdMascota(String idMascota) { this.idMascota = idMascota; }

    public String getIdRefugio() { return idRefugio; }
    public void setIdRefugio(String idRefugio) { this.idRefugio = idRefugio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getTemperamento() { return temperamento; }
    public void setTemperamento(String temperamento) { this.temperamento = temperamento; }

    public String getHistoria() { return historia; }
    public void setHistoria(String historia) { this.historia = historia; }

    public List<String> getFotos() { return fotos; }
    public void setFotos(List<String> fotos) { this.fotos = fotos; }

    // --- NUEVO ESTADO ---
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isEsAdoptado() { return esAdoptado; }
    public void setEsAdoptado(boolean esAdoptado) { this.esAdoptado = esAdoptado; }

    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
}
