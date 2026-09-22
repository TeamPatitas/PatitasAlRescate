package com.patitasalrescate.data.source;

import com.patitasalrescate.model.Mascota;
import java.util.List;

public interface IMascotaDataSource {
    long insertar(Mascota mascota);
    int actualizar(Mascota mascota);
    Mascota obtenerPorId(String idMascota);
    List<Mascota> listarPorRefugio(String idRefugio);
    List<Mascota> listarDisponibles();
    List<Mascota> listarTodos();
}