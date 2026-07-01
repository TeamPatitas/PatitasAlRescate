package com.patitasalrescate.data_access;

import android.content.Context;
import com.patitasalrescate.model.Mascota;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DAOFavoritos {
    private static Map<String, List<String>> mockFavoritos = new HashMap<>();
    private DAOMascota daoMascota;

    public DAOFavoritos(Context context) {
        daoMascota = new DAOMascota(context);
    }

    public long addFavorito(String idAdoptante, String idMascota) {
        List<String> favs = mockFavoritos.getOrDefault(idAdoptante, new ArrayList<>());
        if (!favs.contains(idMascota)) {
            favs.add(idMascota);
            mockFavoritos.put(idAdoptante, favs);
            return 1;
        }
        return 0;
    }

    public void removeFavorito(String idAdoptante, String idMascota) {
        List<String> favs = mockFavoritos.get(idAdoptante);
        if (favs != null) {
            favs.remove(idMascota);
        }
    }

    public boolean esFavorito(String idAdoptante, String idMascota) {
        List<String> favs = mockFavoritos.get(idAdoptante);
        return favs != null && favs.contains(idMascota);
    }

    public List<Mascota> getFavoritosPorAdoptante(String idAdoptante) {
        List<Mascota> result = new ArrayList<>();
        List<String> ids = mockFavoritos.get(idAdoptante);
        if (ids != null) {
            for (String id : ids) {
                Mascota m = daoMascota.obtenerPorId(id);
                if (m != null) result.add(m);
            }
        }
        return result;
    }
}
