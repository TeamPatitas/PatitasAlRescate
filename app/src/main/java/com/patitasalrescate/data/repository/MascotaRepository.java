package com.patitasalrescate.data.repository;

import android.content.Context;

import com.patitasalrescate.data.mock.DAOMascota;
import com.patitasalrescate.data.source.IMascotaDataSource;
import com.patitasalrescate.model.Mascota;
import java.util.List;

public class MascotaRepository {

    private final IMascotaDataSource dataSource;

    public MascotaRepository(Context context) {
        // Ahora usamos el Mock
        this.dataSource = new DAOMascota(context);

        // Cuando la API real esté lista, solo cambias esta línea:
        // this.dataSource = new ApiMascotaDataSource();
    }

    public List<Mascota> listarTodos() {
        return dataSource.listarTodos();
    }

    public List<Mascota> listarDisponibles() {
        return dataSource.listarDisponibles();
    }

    public List<Mascota> listarPorRefugio(String idRefugio) {
        return dataSource.listarPorRefugio(idRefugio);
    }

    public Mascota obtenerPorId(String id) {
        return dataSource.obtenerPorId(id);
    }

    public long insertar(Mascota mascota) {
        return dataSource.insertar(mascota);
    }

    public int actualizar(Mascota mascota) {
        return dataSource.actualizar(mascota);
    }
}
