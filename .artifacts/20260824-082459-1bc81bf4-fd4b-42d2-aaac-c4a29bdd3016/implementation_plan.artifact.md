# Implementación de Toolbar en ActividadPerfilMascota

Este plan detalla los cambios para añadir una Toolbar con flecha de retroceso a la actividad de perfil de mascota, mejorando la navegación del usuario.

## Cambios Propuestos

### Componente de Layout

Añadir la Toolbar al XML de la actividad.

#### [ly_perfil_mascota.xml](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/res/layout/ly_perfil_mascota.xml)

- Insertar un componente `androidx.appcompat.widget.Toolbar` al inicio del `ConstraintLayout`.
- Ajustar las restricciones del `ImageView` (`img_detalle_mascota`) para que se ubique debajo de la Toolbar.

```xml
    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbarPerfilMascota"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="#5D4037"
        app:titleTextColor="@android:color/white"
        app:layout_constraintTop_toTopOf="parent" />

    <ImageView
        android:id="@+id/img_detalle_mascota"
        ...
        app:layout_constraintTop_toBottomOf="@id/toolbarPerfilMascota" />
```

---

### Componente de Código (Controlador)

Configurar la Toolbar en la actividad Java.

#### [ActividadPerfilMascota.java](file:///C:/Users/jacky/StudioProjects/PatitasAlRescate/app/src/main/java/com/patitasalrescate/controllers/management/ActividadPerfilMascota.java)

- Importar `androidx.appcompat.widget.Toolbar`.
- Implementar un método `configToolbar()` para inicializarla.
- Habilitar el botón de retroceso (`setDisplayHomeAsUpEnabled(true)`).
- Configurar el listener para cerrar la actividad al presionar la flecha.

```java
    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarPerfilMascota);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle de Mascota");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
```

## Plan de Verificación

### Pruebas Manuales
1. **Verificar Apariencia**:
   - Abrir el perfil de cualquier mascota.
   - Confirmar que aparece una barra marrón (`#5D4037`) en la parte superior con el título "Detalle de Mascota".
2. **Verificar Navegación**:
   - Presionar la flecha de retroceso en la Toolbar.
   - Confirmar que la actividad se cierra y regresa a la pantalla anterior (Lista o Feed).
3. **Verificar Consistencia**:
   - Asegurarse de que el contenido (foto y datos) no se superponga con la Toolbar.
