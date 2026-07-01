package com.patitasalrescate.data_access;

import android.content.Context;
import com.patitasalrescate.model.Refugio;
import java.util.ArrayList;
import java.util.List;

public class DAORefugio {
    private static List<Refugio> mockRefugios = new ArrayList<>();

    static {
        Refugio r1 = new Refugio();
        r1.setIdRefugio("ref1");
        r1.setNombre("Refugio Insano");
        r1.setDireccion("Calle 123");
        r1.setNumCelular("987654321");
        r1.setCorreo("re");
        r1.setPassword("pass123");
        mockRefugios.add(r1);

        Refugio r2 = new Refugio();
        r2.setIdRefugio("ref2");
        r2.setNombre("Hogar de Huellitas");
        r2.setDireccion("Av. Principal 456");
        r2.setNumCelular("912345678");
        r2.setCorreo("ref2@demo.com");
        r2.setPassword("pass123");
        mockRefugios.add(r2);
    }

    public DAORefugio(Context context) {}

    public long insertar(Refugio refugio) {
        mockRefugios.add(refugio);
        return 1;
    }

    public int actualizar(Refugio refugio) {
        for (int i = 0; i < mockRefugios.size(); i++) {
            if (mockRefugios.get(i).getIdRefugio().equals(refugio.getIdRefugio())) {
                mockRefugios.set(i, refugio);
                return 1;
            }
        }
        return 0;
    }

    public Refugio obtenerPorId(String idRefugio) {
        for (Refugio r : mockRefugios) {
            if (r.getIdRefugio().equals(idRefugio)) return r;
        }
        return null;
    }

    public List<Refugio> listarTodos() {
        return new ArrayList<>(mockRefugios);
    }

    public Refugio login(String correo, String pass) {
        for (Refugio r : mockRefugios) {
            if (r.getCorreo().equals(correo) && r.getPassword().equals(pass)) return r;
        }
        return null;
    }
}
