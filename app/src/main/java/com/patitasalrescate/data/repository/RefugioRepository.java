package com.patitasalrescate.data.repository;

import android.content.Context;
import com.patitasalrescate.data.source.IRefugioDataSource;
import com.patitasalrescate.data.mock.DAORefugio;
import com.patitasalrescate.model.Refugio;
import java.util.List;

public class RefugioRepository {

    private final IRefugioDataSource dataSource;

    public RefugioRepository(Context context) {
        // Ahora usamos el Mock (DAORefugio)
        this.dataSource = new DAORefugio(context);

        // Cuando la API real esté lista, solo cambias esta línea:
        // this.dataSource = new ApiRefugioDataSource();
    }

    public List<Refugio> listarTodos() {
        return dataSource.listarTodos();
    }

    public Refugio obtenerPorId(String id) {
        return dataSource.obtenerPorId(id);
    }

    public long insertar(Refugio refugio) {
        return dataSource.insertar(refugio);
    }

    public int actualizar(Refugio refugio) {
        return dataSource.actualizar(refugio);
    }

}
