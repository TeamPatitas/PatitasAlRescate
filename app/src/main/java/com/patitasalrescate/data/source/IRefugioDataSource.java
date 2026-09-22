package com.patitasalrescate.data.source;

import com.patitasalrescate.model.Refugio;

import java.util.List;

public interface IRefugioDataSource {
    List<Refugio> listarTodos();
    Refugio obtenerPorId(String id);
    long insertar(Refugio refugio);
    int actualizar(Refugio refugio);
}
