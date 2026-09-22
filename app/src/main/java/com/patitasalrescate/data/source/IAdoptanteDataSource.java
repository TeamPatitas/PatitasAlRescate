package com.patitasalrescate.data.source;

import com.patitasalrescate.model.Adoptante;
import java.util.List;

public interface IAdoptanteDataSource {
    long insertar(Adoptante adoptante);
    List<Adoptante> listarTodos();
    Adoptante login(String correo, String passwordEncriptada);
    int actualizar(Adoptante adoptante);
    void eliminar(String idAdoptante);
    boolean existeCorreo(String correo);
}