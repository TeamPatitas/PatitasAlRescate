package com.patitasalrescate.data.mock;

import android.content.Context;

import com.patitasalrescate.data.source.IRefugioDataSource;
import com.patitasalrescate.model.Refugio;
import java.util.ArrayList;
import java.util.List;

public class DAORefugio implements IRefugioDataSource {
    private static List<Refugio> mockRefugios = new ArrayList<>();

    static {
        Refugio r1 = new Refugio();
        r1.setIdRefugio("ref1");
        r1.setNombre("Refugio Insano");
        r1.setDireccion("Calle 123");
        r1.setNumCelular("918272136");
        r1.setFotoUrl("https://i.pinimg.com/736x/2f/40/2b/2f402b06b3e2ec7a3f80d8696d8828a7.jpg");
        mockRefugios.add(r1);

        Refugio r2 = new Refugio();
        r2.setIdRefugio("ref2");
        r2.setNombre("Hogar de Huellitas");
        r2.setDireccion("Av. Principal 456");
        r2.setNumCelular("989361714");
        r1.setFotoUrl("https://i.pinimg.com/736x/c8/12/2e/c8122e45a04192a747b9ec9aa902dab9.jpg");
        mockRefugios.add(r2);

        Refugio r3 = new Refugio();
        r1.setIdRefugio("ref1");
        r1.setNombre("Refugio cd");
        r1.setDireccion("Calle 123");
        r1.setNumCelular("918272136");
        mockRefugios.add(r1);
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
}
