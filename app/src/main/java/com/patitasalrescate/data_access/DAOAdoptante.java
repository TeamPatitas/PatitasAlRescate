package com.patitasalrescate.data_access;

import android.content.Context;
import com.patitasalrescate.model.Adoptante;
import java.util.ArrayList;
import java.util.List;

public class DAOAdoptante {
    private static List<Adoptante> mockAdoptantes = new ArrayList<>();

    static {
        mockAdoptantes.add(new Adoptante("xd", "Luis", "ad", "pass123", "987654321", 25, "Masculino"));
    }

    public DAOAdoptante(Context context) {}

    public long insertar(Adoptante adoptante) {
        mockAdoptantes.add(adoptante);
        return 1;
    }

    public List<Adoptante> listarTodos() {
        return new ArrayList<>(mockAdoptantes);
    }

    public Adoptante login(String correo, String passwordEncriptada) {
        for (Adoptante a : mockAdoptantes) {
            if (a.getCorreo().equals(correo) && a.getPassword().equals(passwordEncriptada)) return a;
        }
        return null;
    }

    public int actualizar(Adoptante adoptante) {
        for (int i = 0; i < mockAdoptantes.size(); i++) {
            if (mockAdoptantes.get(i).getIdAdoptante().equals(adoptante.getIdAdoptante())) {
                mockAdoptantes.set(i, adoptante);
                return 1;
            }
        }
        return 0;
    }

    public void eliminar(String idAdoptante) {
        mockAdoptantes.removeIf(a -> a.getIdAdoptante().equals(idAdoptante));
    }

    public boolean existeCorreo(String correo) {
        for (Adoptante a : mockAdoptantes) {
            if (a.getCorreo().equalsIgnoreCase(correo)) return true;
        }
        return false;
    }
}
