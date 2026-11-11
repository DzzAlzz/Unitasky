# Checklist de auditoría — UniTasky Avance 35

Este checklist compara el estado del proyecto con la rúbrica de la Actividad 2.5. Los puntos que requieren ejecutar la app o revisar GitHub se mantienen como verificación manual y no se consideran aprobados automáticamente.

| Criterio | Requisito | Estado | Evidencia |
|---|---|---|---|
| Seguridad | Consultas parametrizadas con `?` / `selectionArgs` | ✅ Cumple | `database/DatabaseHelper.java` utiliza argumentos en consultas dinámicas de usuarios, tareas, asignaturas y sesiones. |
| Seguridad | Uso de `ContentValues` en inserciones y actualizaciones | ✅ Cumple | `DatabaseHelper.java` utiliza `ContentValues` en usuarios, asignaturas, tareas, migraciones y sesiones de enfoque. |
| Seguridad | Cierre de objetos `Cursor` | ✅ Cumple | Los cursores revisados se cierran mediante bloques `finally`. |
| Seguridad | Contract Class conectada con SQLiteOpenHelper | ✅ Cumple | `UniTaskyContract.java` define las tablas/columnas y `DatabaseHelper.java` utiliza esas constantes. |
| Seguridad | Activities internas no exportadas | ✅ Cumple | `AndroidManifest.xml`: 12 Activities internas usan `android:exported="false"`; `MainActivity` es la launcher. |
| Seguridad | Backup local deshabilitado | ✅ Cumple | `AndroidManifest.xml`: `android:allowBackup="false"`. |
| Seguridad | `FLAG_SECURE` en pantallas sensibles | ✅ Cumple | `LoginActivity.java` y `RegistroActivity.java`. |
| Seguridad | Contraseñas no almacenadas en texto plano | ✅ Cumple | `PasswordUtils.java` + `DatabaseHelper.java`; se almacena/compara SHA-256. |
| Seguridad | Sesión con datos mínimos | ✅ Cumple | `SessionManager.java` conserva estado de sesión e ID de usuario. |
| Seguridad | FileProvider | ✅ Cumple | No aplica: UniTasky no usa cámara ni comparte archivos mediante URIs. No se agrega un FileProvider artificial. |
| Documentación | `README.md` en la raíz | ✅ Cumple | `/README.md`. |
| Documentación | Stack, requisitos, instalación y persistencia | ✅ Cumple | Secciones correspondientes de `/README.md`. |
| Documentación | Diagrama de navegación editable | ✅ Cumple | `docs/diagramas/UniTasky_Diagrama_Navegacion.drawio`, XML nativo de Draw.io. |
| Documentación | ERD de SQLite editable | ✅ Cumple | `docs/diagramas/UniTasky_ERD_SQLite.drawio`, XML nativo de Draw.io. |
| Documentación | ERD coherente con Contract/DatabaseHelper | ✅ Cumple | Cuatro tablas: `usuarios`, `asignaturas`, `tareas`, `sesiones_enfoque`; se conserva `id`. |
| Documentación | JavaDoc estructurado | ✅ Cumple | JavaDoc en las clases Java del proyecto, incluyendo `@param`, `@return` y `@throws` donde corresponde. |
| Documentación | Capturas reales de la aplicación | 🟡 Cumple parcialmente | `docs/capturas/` está preparada, pero todavía no contiene capturas reales. Deben añadirse desde Android Studio/dispositivo. |
| Funcionalidad Core | Registro, Login y sesión conectados a SQLite | 🟡 Cumple parcialmente | El código está conectado mediante `TareaRepository`/`DatabaseHelper`; falta prueba final en ejecución real. |
| Funcionalidad Core | CRUD de tareas | 🟡 Cumple parcialmente | Agregar, listar, editar, eliminar y completar están implementados; falta prueba final en dispositivo/emulador. |
| Funcionalidad Core | Búsqueda, filtros, orden y próximas entregas | 🟡 Cumple parcialmente | Implementados con datos SQLite; falta validación funcional final en ejecución. |
| Funcionalidad Core | CRUD de asignaturas y asociación con tareas | 🟡 Cumple parcialmente | Implementado en código; falta prueba final en ejecución real. |
| Funcionalidad Core | Dashboard con información de SQLite | 🟡 Cumple parcialmente | Consultas asíncronas implementadas; falta comprobar visualmente los resultados en ejecución. |
| Funcionalidad Core | Modo Enfoque e historial | 🟡 Cumple parcialmente | Temporizador, guardado e historial están implementados; falta prueba final en dispositivo/emulador. |
| Hardware | Sensor de proximidad integrado | 🟡 Cumple parcialmente | `ModoEnfoqueActivity` usa `Sensor.TYPE_PROXIMITY`; falta demostrar su funcionamiento en hardware/emulador compatible. |
| Hardware | Manejo del ciclo de vida del sensor | ✅ Cumple | Registro en `onResume`, liberación en `onPause` y liberación defensiva en `onDestroy`. |
| Hardware | Dispositivo sin sensor no provoca acceso nulo | ✅ Cumple | Se comprueba `sensorProximidad == null` y se muestra estado de sensor no disponible. |
| Git | `.gitignore` evita archivos locales/generados | ✅ Cumple | `/.idea`, `.gradle`, `/local.properties`, `/build` y `app/build` están ignorados. |
| Git | Mínimo 10 commits progresivos y semánticos | 🟡 Cumple parcialmente | Verificar manualmente en GitHub el historial completo antes de presentar. |
| Git | Ramas/avances con traza incremental | 🟡 Cumple parcialmente | Verificar manualmente que las ramas de los avances recientes estén publicadas correctamente. |
| Git | Ausencia de un único commit masivo final | 🟡 Cumple parcialmente | Requiere revisar el historial público del repositorio. |
| Estabilidad | SQLite fuera del hilo principal | ✅ Cumple | `TareaRepository` utiliza un `ExecutorService` para operaciones de persistencia. |
| Estabilidad | UI actualizada desde el hilo principal | ✅ Cumple | Activities utilizan `runOnUiThread()` en callbacks asíncronos. |
| Estabilidad | Repository liberado desde Activities | ✅ Cumple | Las Activities con Repository lo cierran en `onDestroy()`. |
| Estabilidad | Compilación final de Gradle | 🟡 Cumple parcialmente | No se pudo ejecutar aquí porque Gradle Wrapper requiere descargar Gradle 8.9; debe comprobarse en Android Studio. |

## Verificaciones manuales antes del Avance 36

1. Ejecutar **Build / Assemble Project** en Android Studio.
2. Probar registro, Login, logout y regreso con el botón Atrás.
3. Probar CRUD completo de tareas y asignaturas.
4. Probar búsqueda, filtros, prioridades, estados y próximas entregas.
5. Probar Modo Enfoque, sensor de proximidad, guardado e historial.
6. Tomar capturas reales y guardarlas en `docs/capturas/`.
7. Agregar las capturas seleccionadas al `README.md`.
8. Revisar en GitHub que existan al menos 10 commits formales, progresivos y semánticos.
9. Confirmar que las ramas de avances recientes estén publicadas y que no exista un único commit masivo final.

## Estado de SQLite

- Base de datos: `unitasky.db`.
- Versión actual: **4** (`VERSION_BD = 4`).
- Este avance no modifica tablas, columnas, relaciones ni migraciones.
- Se mantiene `id` como clave primaria por compatibilidad con los avances anteriores.

## Estado estimado por criterio

| Criterio de la rúbrica | Estado estimado antes de la demostración final |
|---|---|
| 1. Medidas de Seguridad & Buenas Prácticas — 25 pts | ✅ **Potencial 25/25** según auditoría estática del código y Manifest. |
| 2. Documentación Técnica en Git — 25 pts | 🟡 **Muy avanzado**. README, JavaDoc, navegación y ERD están presentes; faltan capturas reales para cerrar completamente la documentación visual. |
| 3. Cumplimiento Ficha & Funcionalidad Core — 20 pts | 🟡 **Potencial 20/20**, pero requiere demostración final en Android Studio/dispositivo para comprobar el flujo completo sin crashes. |
| 4. Integración de Sensores / Entrada Hardware — 15 pts | 🟡 **Potencial 15/15**. El manejo del sensor y su ciclo de vida están implementados; falta demostración física/emulada compatible. |
| 5. Versionamiento Git & Traza — 15 pts | 🟡 **Pendiente de verificación manual en GitHub**. El ZIP y `.gitignore` están preparados, pero el historial remoto no se certifica desde este proyecto local. |

No se asigna automáticamente un puntaje total porque los criterios 3, 4 y 5 requieren evidencia de ejecución o historial externo durante la presentación.
