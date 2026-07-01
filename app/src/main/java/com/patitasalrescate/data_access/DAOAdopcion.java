package com.patitasalrescate.data_access;

import android.content.Context;
import com.patitasalrescate.model.Adopcion;
import java.util.ArrayList;
import java.util.List;

public class DAOAdopcion {
    private static List<Adopcion> mockAdopciones = new ArrayList<>();

    public DAOAdopcion(Context context) {}

    public long insertar(Adopcion adopcion) {
        mockAdopciones.add(adopcion);
        return 1;
    }

    public List<Adopcion> listarPorRefugio(String idRefugio) {
        List<Adopcion> result = new ArrayList<>();
        for (Adopcion a : mockAdopciones) {
            if (a.getIdRefugio().equals(idRefugio)) result.add(a);
        }
        return result;
    }
}
