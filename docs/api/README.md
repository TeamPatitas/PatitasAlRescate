# Integración de Patitas API

Implementada en la rama `diego-mejoras-cache`, sin cambios de diseño ni flujos de pantalla.

- Documentación: https://patitasalrescate.galaxym4.dev/docs
- Base URL: https://api-patitasalrescate.galaxym4.dev/
- Contrato verificado el 21 de septiembre de 2026: https://api-patitasalrescate.galaxym4.dev/swagger/v1/swagger.json
- Copia del contrato: `openapi.json`. Inventario completo: `endpoints.md`.

Se implementan las 24 operaciones de las cuatro secciones: administración (9), autenticación (4), mascotas (6) y refugios (5). También se incluye `GET /`, la operación pública de estado, para un total de 25.

## Distribución en el proyecto

| Ubicación bajo `com.patitasalrescate` | Responsabilidad |
|---|---|
| `data/source/I*ApiDataSource` | Contratos de acceso remoto que pueden inyectarse y sustituirse en pruebas. |
| `data/remote/*ApiService` | Rutas, métodos HTTP, parámetros y formatos Retrofit. |
| `data/remote/*ApiDataSource` | Conversión de peticiones tipadas a JSON o multipart. |
| `data/remote/dto` | Los 22 esquemas exactos de la API y archivos binarios `UploadFile`. |
| `data/remote/ApiClient` | Cliente compartido, URL, timeouts, JWT y creación de repositorios. |
| `data/remote/ApiSession` | Token en memoria, autenticación explícita y cierre de sesión. |
| `data/remote/ApiError` | Código HTTP, cuerpo de error y `Retry-After`. |
| `data/repository/*ApiRepository` | Acceso tipado a las operaciones por sección. |
| `data/local`, `data/mock` | Persistencia local existente. |

Los repositorios locales `AdoptanteRepository`, `MascotaRepository` y `RefugioRepository` siguen atendiendo las pantallas actuales. **Esta entrega implementa la capa remota; las pantallas todavía no consumen la API.** Se conserva ese límite para respetar el alcance de datos/API. Sus firmas síncronas (`long`, `int`, listas) y los modelos locales no equivalen al contrato HTTP y no deben reemplazarse por llamadas de red bloqueantes.

Tampoco se inventan equivalencias: el contrato no expone login separado para refugios, consulta de existencia de correo, eventos, favoritos, adopciones ni filtro de mascotas por refugio. Las listas remotas son paginadas y devuelven resúmenes; los detalles requieren sus endpoints por ID.

## Uso desde la capa que consuma los repositorios

Crear y compartir una instancia `ApiClient` durante la sesión. Cada método devuelve un `retrofit2.Call<T>` cancelable. Usar `enqueue`, nunca `execute` en el hilo principal de Android. Las pruebas JVM utilizan `execute` fuera de Android.

```java
ApiClient api = new ApiClient();
LoginRequest request = new LoginRequest();
request.email = email;
request.password = password; // El contrato recibe la contraseña, no el hash local.

api.auth.login(request).enqueue(new Callback<AuthResponse>() {
    @Override public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            api.session.authenticate(response.body());
            // Las próximas llamadas protegidas enviarán Authorization: Bearer <token>.
        } else {
            // Consultar response.code() y ApiError.from(response), que puede lanzar IOException.
        }
    }

    @Override public void onFailure(Call<AuthResponse> call, Throwable error) {
        // Error de transporte, cancelación o respuesta JSON inválida.
    }
});
```

El mismo paso `session.authenticate(response.body())` se aplica después de un registro exitoso. El token no se persiste ni se imprime. Al cerrar sesión, llamar `api.session.logout()` además del cierre de la sesión local si se utiliza. Después de reiniciar el proceso se requiere autenticación nuevamente. Un 401 se entrega al consumidor; no se reintenta ni se elimina automáticamente un token que podría pertenecer a una sesión más reciente. No existe endpoint de refresh en el contrato.

Ejemplos de operaciones disponibles:

```java
api.admin.getCurrentUser();
api.pets.getAllPets(1, 20); // null omite el parámetro y usa el valor del servidor.
api.pets.getPetById(petId);
api.shelters.getAllShelters(1, 20);
api.shelters.getShelterById(shelterId);
```

Estas expresiones crean llamadas; solo `enqueue` o `execute` las envían. Cancelar las llamadas que ya no sean necesarias con `call.cancel()`.

## Detalles del contrato

- Login, roles y actualización de datos de mascota usan JSON. Registro, perfiles, creación de mascotas/refugios y fotos usan `multipart/form-data` con nombres exactos y contenido binario.
- `UploadFile.fromFile(file, "image/jpeg")` crea un adjunto. Para un URI de Android, el consumidor debe copiar su contenido a un archivo de caché y conservarlo hasta finalizar la llamada. No se envía el texto del URI como si fuese una imagen.
- Las fotos de creación de mascota se repiten con el campo `photos`; el reemplazo usa `photo`. `photoIndex` se transmite sin reinterpretarlo: el contrato menciona tres fotos pero no especifica si el índice empieza en cero o en uno.
- En peticiones de mascotas se usa `species`; en el detalle devuelto se usa `specie`. Los enums son `OTHER/DOG/CAT` y `MALE/FEMALE`.
- El género de usuario es un entero diferente del enum de mascotas. La documentación no define el significado de sus valores. Las fechas son cadenas ISO `yyyy-MM-dd`.
- Las actualizaciones omiten campos `null`, conservan `false`/`0` y no sobrescriben campos no enviados. Un formulario sin campos se rechaza localmente. El contrato no define una operación para borrar un campo enviando `null`.
- Se admiten respuestas 200, 201, 204 y cuerpos vacíos en operaciones sin resultado. Un fallo HTTP no se trata como una lista vacía ni como éxito.
- Los roles son verificados por el servidor. Los endpoints de administración están disponibles en código, pero necesitan un JWT con los permisos apropiados.
- El servidor aplica cooldowns de 30 segundos, 1 minuto o 5 minutos según la operación. `ApiError.retryAfter` conserva el encabezado si está presente. No se reintentan escrituras ni se siguen redirecciones automáticamente.

## Verificación y mantenimiento

`ApiClientTest` verifica las 25 operaciones con MockWebServer: HTTP, JWT, JSON, multipart, paginación, codificación del token de correo, respuestas vacías, errores y sesión. No utiliza cuentas reales ni ejecuta escrituras en producción.

Resultado del 21 de septiembre de 2026: `assembleDebug` y `testDebugUnitTest` completados correctamente. Pasaron 10 pruebas de integración HTTP y la prueba unitaria existente, sin fallos. APK generado en `app/build/outputs/apk/debug/app-debug.apk`. Gradle conserva avisos previos de opciones obsoletas; no impidieron la compilación.

En esta máquina usar el JDK de Android Studio; el JDK 26 configurado globalmente falló en la transformación `androidJdkImage`. `local.properties` apunta al SDK instalado y está ignorado por Git.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

La integración requiere validación posterior con cuentas de prueba para comprobar reglas de negocio, permisos y efectos reales del backend; las pruebas de contrato no sustituyen esa validación.

Comprobación de conectividad de solo lectura: `GET /` y el contrato OpenAPI respondieron 200; `GET /admin/user`, `GET /pet` y `GET /shelter` respondieron 401 sin credenciales. No se ejecutaron registros, correos de verificación, actualizaciones ni borrados en el servidor real.

La compilación también detectó inconsistencias previas tras integrar `master`. Se corrigieron únicamente los imports de `DAORefugio` y `DAOAdoptante` en las actividades de perfil, y se retiró la firma de login de refugio del contrato/repositorio local: no tenía implementación ni consumidores y el modelo de refugio ya no tiene contraseña. No se alteró la lógica visual.

Para regenerar DTOs, contratos, servicios, fuentes remotas y repositorios después de revisar un nuevo contrato: `node scripts/generate-api-client.cjs`. Los archivos de infraestructura y las pruebas se mantienen manualmente. Revisar el diff del contrato y ajustar las pruebas antes de aceptar cambios del servidor.
