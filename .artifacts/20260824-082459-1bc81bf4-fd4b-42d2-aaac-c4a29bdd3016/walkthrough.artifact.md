# Resumen de Cambios: Imagen por Defecto para Refugios

Se ha implementado una imagen de respaldo para todos los refugios que no cuentan con una fotografía en su perfil.

## Cambios Realizados

### 1. Nuevo Recurso Gráfico
Se descargó y añadió la imagen solicitada al proyecto:
- **Archivo:** [img_default_refugio.png](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/res/drawable/img_default_refugio.png)
- **Descripción:** Imagen de una casita con un perro y un gato.

### 2. Actualización de Adaptador
En [AdaptadorRefugios.java](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/java/com/patitasalrescate/ui/AdaptadorRefugios.java):
- Se reemplazó el icono genérico de Android (`ic_launcher_foreground`) por la nueva imagen.
- Se configuró Glide para usar esta imagen como `placeholder` (mientras carga) y `error` (si el link de la foto falla).

### 3. Actualización de Perfil Detallado
En [ActividadPerfilRefugio.java](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/java/com/patitasalrescate/controllers/management/ActividadPerfilRefugio.java):
- Se añadió la lógica para que, si el refugio no tiene `fotoUrl`, se asigne automáticamente la imagen de la casita a la cabecera del perfil.

### 4. QR de Yape para Donaciones
- Se añadió la imagen [img_yape_default.png](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/res/drawable/img_yape_default.png).
- En [ActividadPerfilRefugio.java](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/java/com/patitasalrescate/controllers/management/ActividadPerfilRefugio.java), se actualizó la sección de donaciones para mostrar este QR de Yape por defecto.

```java
            // Imagen QR por defecto (Yape)
            imgQR.setImageResource(R.drawable.img_yape_default);
```

### 5. Toolbar en Perfil de Mascota
Se añadió una Toolbar a [ActividadPerfilMascota.java](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/java/com/patitasalrescate/controllers/management/ActividadPerfilMascota.java) y su respectivo layout [ly_perfil_mascota.xml](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/res/layout/ly_perfil_mascota.xml):
- **Título:** "Detalle de Mascota".
- **Color:** Marrón (`#5D4037`) con texto blanco para mantener la línea gráfica.
- **Navegación:** Se incluyó la flecha de retroceso funcional que cierra la actividad.

### 6. Imagen por Defecto para Eventos
Se configuró la imagen [evento_default.jpg](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/res/drawable/evento_default.jpg) como respaldo para los eventos:
- **En la lista:** [AdaptadorEventos.java](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/java/com/patitasalrescate/ui/AdaptadorEventos.java) ahora muestra esta imagen si el evento no tiene foto.
- **En el detalle:** [ActividadDetalleEvento.java](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/java/com/patitasalrescate/controllers/management/ActividadDetalleEvento.java) también utiliza esta imagen como fallback y placeholder.

## Verificación Realizada
- [x] Se confirmó la existencia de `evento_default.jpg` en los recursos.
- [x] Se validó la actualización en el adaptador de eventos.
- [x] Se validó la actualización en la actividad de detalle de evento.
- [x] La navegación y visualización de la Toolbar en el perfil de mascota funcionan correctamente.
