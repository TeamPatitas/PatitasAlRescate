package com.patitasalrescate.data.repository;

import android.content.Context;

import com.patitasalrescate.data.mock.DAOAdoptante;
import com.patitasalrescate.data.source.IAdoptanteDataSource;

import com.patitasalrescate.model.Adoptante;
import java.util.List;

public class AdoptanteRepository {

    private final IAdoptanteDataSource dataSource;

    public AdoptanteRepository(Context context) {
        // Ahora usamos el Mock
        this.dataSource = new DAOAdoptante(context);

        // Cuando la API real esté lista, solo cambias esta línea:
        // this.dataSource = new ApiAdoptanteDataSource();
    }

    public long insertar(Adoptante adoptante) {
        return dataSource.insertar(adoptante);
    }

    public List<Adoptante> listarTodos() {
        return dataSource.listarTodos();
    }

    public Adoptante login(String correo, String password) {
        return dataSource.login(correo, password);
    }

    public int actualizar(Adoptante adoptante) {
        return dataSource.actualizar(adoptante);
    }

    public void eliminar(String idAdoptante) {
        dataSource.eliminar(idAdoptante);
    }

    public boolean existeCorreo(String correo) {
        return dataSource.existeCorreo(correo);
    }
}