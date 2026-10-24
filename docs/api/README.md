# Integración de Patitas API

Implementada en la rama `diego-mejoras-cache`, sin cambios de diseño ni flujos de pantalla.

- Documentación: https://patitasalrescate.galaxym4.dev/docs
- Base URL: https://api-patitasalrescate.galaxym4.dev/
- Contrato verificado el 21 de septiembre de 2026: https://api-patitasalrescate.galaxym4.dev/swagger/v1/swagger.json
- Copia del contrato: `openapi.json`. Inventario completo: `endpoints.md`.

Se implementan 30 operaciones: administración (9), autenticación (4), eventos (5), mascotas (6), refugios (5) y `GET /`.

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

Las pantallas de login, registro, perfil, listas, búsqueda, detalles, alta de mascotas y eventos consumen ahora la API mediante `enqueue`. `ApiApp` comparte un solo cliente y `ApiPages` completa las páginas antes de mostrar un listado. Los repositorios y DAOs locales se mantienen como código heredado; los flujos conectados no los usan para simular un resultado remoto.

El contrato no expone favoritos, solicitudes de adopción, edad de mascotas, teléfono de refugios, búsqueda en servidor ni filtro de mascotas por refugio. El modo refugio filtra consultando cada detalle de mascota; la búsqueda también consulta cada detalle. Esto funciona para probar la integración, pero será lento con catálogos grandes: se necesita `GET /pet?shelterId=...` y filtros de búsqueda/paginación de servidor. Las acciones locales de favoritos y adopción se desactivaron para evitar mostrar confirmaciones falsas.

El género del usuario se declara como entero sin documentar sus valores. El formulario envía provisionalmente `0` para Masculino y `1` para Femenino; **el equipo backend debe confirmar este orden antes de usar registro con datos reales**. El formulario ahora pide fecha de nacimiento `AAAA-MM-DD`, como exige la API. Teléfono no se envía porque no existe en el contrato de registro ni perfil. El alta de refugio requiere iniciar sesión y queda pendiente de habilitación por administrador. Los eventos usan fecha `AAAA-MM-DD HH:MM` convertida a ISO 8601, y permiten seleccionar una imagen binaria.

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

### Android Studio después de un pull

Abrir la raíz del proyecto que contiene `settings.gradle.kts` y ejecutar **File > Sync Project with Gradle Files**. Descargar cambios con Git actualiza los archivos, pero el editor puede conservar el modelo anterior de dependencias hasta sincronizar Gradle.

Si aparece `Cannot resolve symbol 'retrofit2'`, conservar los imports `retrofit2.*`: la dependencia ya está declarada en `app/build.gradle.kts`. Revisar el resultado de la sincronización antes de modificar Java. La opción Gradle JDK del proyecto debe apuntar al JDK de Android Studio (`GRADLE_LOCAL_JAVA_HOME` en esta máquina). Si la sincronización falla, revisar su error concreto en la ventana Build/Sync.

Compilar por terminal comprueba el código y las dependencias, pero no sustituye la sincronización del modelo del editor.

`ApiClientTest` verifica las 30 operaciones con MockWebServer: HTTP, JWT, JSON, multipart, paginación, codificación del token de correo, respuestas vacías, errores y sesión. No utiliza cuentas reales ni ejecuta escrituras en producción.

Resultado del 21 de septiembre de 2026: `assembleDebug` y `testDebugUnitTest` completados correctamente. Pasaron 10 pruebas de integración HTTP y la prueba unitaria existente, sin fallos. APK generado en `app/build/outputs/apk/debug/app-debug.apk`. Gradle conserva avisos previos de opciones obsoletas; no impidieron la compilación.

Resultado del 23 de septiembre de 2026, en `C:\Users\diego\Documents\PATITASALRESCATE`: `assembleDebug` y `testDebugUnitTest` completados correctamente con 11 pruebas HTTP (incluidos los cinco endpoints de eventos) y la prueba unitaria existente. En pruebas manuales, `/auth/login` aceptó credenciales válidas pero `GET /admin/user` devolvió HTTP 400; la web también queda cargando el perfil. La app permite consultar el feed con acceso limitado, sin habilitar la edición del perfil hasta que ese endpoint funcione. No se realizaron escrituras en producción.

En esta máquina usar el JDK de Android Studio; el JDK 26 configurado globalmente falló en la transformación `androidJdkImage`. `local.properties` apunta al SDK instalado y está ignorado por Git.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

La integración requiere validación posterior con cuentas de prueba para comprobar reglas de negocio, permisos y efectos reales del backend; las pruebas de contrato no sustituyen esa validación.

Comprobación de conectividad de solo lectura: `GET /` y el contrato OpenAPI respondieron 200; `GET /admin/user`, `GET /pet` y `GET /shelter` respondieron 401 sin credenciales. No se ejecutaron registros, correos de verificación, actualizaciones ni borrados en el servidor real.

La compilación también detectó inconsistencias previas tras integrar `master`. Se corrigieron únicamente los imports de `DAORefugio` y `DAOAdoptante` en las actividades de perfil, y se retiró la firma de login de refugio del contrato/repositorio local: no tenía implementación ni consumidores y el modelo de refugio ya no tiene contraseña. No se alteró la lógica visual.

Para regenerar DTOs, contratos, servicios, fuentes remotas y repositorios después de revisar un nuevo contrato: `node scripts/generate-api-client.cjs`. Los archivos de infraestructura y las pruebas se mantienen manualmente. Revisar el diff del contrato y ajustar las pruebas antes de aceptar cambios del servidor.
