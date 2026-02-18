package com.patitasalrescate.accesoADatos;

import com.patitasalrescate.model.Adoptante;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.utils.SeguridadUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ApiSimulada {

    public List<Adoptante> getAdoptantesDesdeApi() {
        List<Adoptante> adoptantes = new ArrayList<>();

        Adoptante prueba = new Adoptante();
        prueba.setIdAdoptante(UUID.randomUUID().toString());
        prueba.setNombre("Usuario Prueba");
        prueba.setCorreo("test@patitas.com");
        prueba.setPassword(SeguridadUtils.encriptar("clave123"));

        adoptantes.add(prueba);
        return adoptantes;
    }

    public List<Refugio> getRefugiosDesdeApi() {
        List<Refugio> refugios = new ArrayList<>();

        refugios.add(new Refugio(
                UUID.randomUUID().toString(),
                "Refugio Ejemplo",
                "Dirección 1",
                -12.0464,
                -77.0428,
                "refugio@ejemplo.com",
                SeguridadUtils.encriptar("123456"),
                "999999999",
                "https://picsum.photos/200",
                System.currentTimeMillis()
        ));

        return refugios;
    }

    public List<Mascota> getMascotasDesdeApi() {
        List<Mascota> mascotas = new ArrayList<>();

        List<String> fotos1 = Arrays.asList(
                "https://picsum.photos/200/300",
                "https://picsum.photos/200/301"
        );

        // Mascota 1: Bobby
        mascotas.add(new Mascota(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Bobby",
                "Perro",
                "Labrador",
                "Macho",
                24,
                "Amigable",
                "Historia de rescate",
                fotos1,
                "DISPONIBLE",
                System.currentTimeMillis()
        ));

        List<String> fotos2 = Arrays.asList("https://picsum.photos/200/302");

        // Mascota 2: Michi
        mascotas.add(new Mascota(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Michi",
                "Gato",
                "Persa",
                "Hembra",
                36,
                "Tranquilo",
                "Rescatado de la calle",
                fotos2,
                "DISPONIBLE",
                System.currentTimeMillis()
        ));

        return mascotas;
    }
}