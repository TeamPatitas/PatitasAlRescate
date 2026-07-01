package com.patitasalrescate.data_access;

import android.content.Context;
import com.patitasalrescate.model.Mascota;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DAOMascota {
    private static List<Mascota> mockMascotas = new ArrayList<>();

    static {
        mockMascotas.add(new Mascota("1", "ref1", "Firulais", "Perro", "Labrador", "Macho", 24, "Juguetón", "Rescatado de la calle", Arrays.asList("https://images.dog.ceo/breeds/labrador/n02099712_1150.jpg"), "DISPONIBLE", 0));
        mockMascotas.add(new Mascota("2", "ref1", "Luna", "Perro", "Golden Retriever", "Hembra", 12, "Tranquila", "Buscando un hogar amoroso", Arrays.asList("https://images.dog.ceo/breeds/retriever-golden/n02099601_3167.jpg"), "DISPONIBLE", 0));
        mockMascotas.add(new Mascota("3", "ref2", "Michi", "Gato", "Siamés", "Macho", 6, "Curioso", "Muy cariñoso", Arrays.asList("https://placekitten.com/200/300"), "DISPONIBLE", 0));
        mockMascotas.add(new Mascota("4", "ref2", "Pelusa", "Gato", "Persa", "Hembra", 18, "Diva", "Le gusta dormir mucho", Arrays.asList("https://placekitten.com/201/301"), "ADOPTADO", 0));
    }

    public DAOMascota(Context context) {}

    public long insertar(Mascota mascota) {
        mockMascotas.add(mascota);
        return 1;
    }

    public int actualizar(Mascota mascota) {
        for (int i = 0; i < mockMascotas.size(); i++) {
            if (mockMascotas.get(i).getIdMascota().equals(mascota.getIdMascota())) {
                mockMascotas.set(i, mascota);
                return 1;
            }
        }
        return 0;
    }

    public Mascota obtenerPorId(String idMascota) {
        for (Mascota m : mockMascotas) {
            if (m.getIdMascota().equals(idMascota)) return m;
        }
        return null;
    }

    public List<Mascota> listarPorRefugio(String idRefugio) {
        List<Mascota> result = new ArrayList<>();
        for (Mascota m : mockMascotas) {
            if (m.getIdRefugio().equals(idRefugio)) result.add(m);
        }
        return result;
    }

    public List<Mascota> listarDisponibles() {
        List<Mascota> result = new ArrayList<>();
        for (Mascota m : mockMascotas) {
            if ("DISPONIBLE".equals(m.getEstado())) result.add(m);
        }
        return result;
    }

    public List<Mascota> listarTodos() {
        return new ArrayList<>(mockMascotas);
    }
}
