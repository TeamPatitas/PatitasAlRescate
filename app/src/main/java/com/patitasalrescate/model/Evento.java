package com.patitasalrescate.model;

import java.io.Serializable;

public class Evento implements Serializable {
    private String idEvento;
    private String nombre;
    private String fecha;
    private String descripcion;
    private String fotoUrl;

    public Evento() {}

    public Evento(String idEvento, String nombre, String fecha, String descripcion, String fotoUrl) {
        this.idEvento = idEvento;
        this.nombre = nombre;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.fotoUrl = fotoUrl;
    }

    public String getIdEvento() { return idEvento; }
    public void setIdEvento(String idEvento) { this.idEvento = idEvento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}