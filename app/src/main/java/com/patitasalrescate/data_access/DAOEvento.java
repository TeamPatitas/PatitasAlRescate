package com.patitasalrescate.data_access;

import android.content.Context;
import com.patitasalrescate.model.Evento;
import java.util.ArrayList;
import java.util.List;

public class DAOEvento {
    private static final List<Evento> mockEventos = new ArrayList<>();

    static {
        mockEventos.add(new Evento("1", "Gran Campaña de Adopción", "20 de Octubre, 2023", "Ven a conocer a tu futuro mejor amigo en el Parque Central.", "", -7.1589, -78.5147));
        mockEventos.add(new Evento("2", "Colecta de Alimento", "25 de Octubre, 2023", "Estaremos recibiendo donaciones de alimento para perros y gatos.", "", -7.1565, -78.5168));
        mockEventos.add(new Evento("3", "Charla sobre Tenencia Responsable", "2 de Noviembre, 2023", "Aprende todo lo necesario para cuidar a tu mascota.", "", -7.1522, -78.5095));
    }

    public DAOEvento(Context context) {
        // Constructor para consistencia con otros DAOs
    }

    public List<Evento> listarTodos() {
        return new ArrayList<>(mockEventos);
    }

    public long insertar(Evento e) {
        mockEventos.add(e);
        return 1;
    }

    public boolean actualizar(Evento e) {
        for (int i = 0; i < mockEventos.size(); i++) {
            if (mockEventos.get(i).getIdEvento().equals(e.getIdEvento())) {
                mockEventos.set(i, e);
                return true;
            }
        }
        return false;
    }
    
    public Evento obtenerPorId(String id) {
        for (Evento e : mockEventos) {
            if (e.getIdEvento().equals(id)) return e;
        }
        return null;
    }
}
