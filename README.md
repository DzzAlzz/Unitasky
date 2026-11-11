# UniTasky

UniTasky es una aplicación Android orientada a estudiantes que busca facilitar la organización de tareas y actividades académicas desde un dispositivo móvil.

El proyecto se ha desarrollado de forma progresiva en la asignatura **Programación Android (IEI-093)**, manteniendo una solución sencilla basada en Java, XML y SQLite.

## Problema que resuelve

Durante el semestre un estudiante puede manejar distintas asignaturas, tareas, fechas de entrega, prioridades y estados de avance al mismo tiempo. UniTasky centraliza esa información para permitir un seguimiento más ordenado y además incorpora sesiones de enfoque asociadas a las tareas pendientes.

## Funcionalidades actuales

- Registro de usuarios e inicio de sesión.
- Gestión de sesión local mediante `SharedPreferences`.
- Dashboard con resumen de información académica.
- Creación, consulta, edición, eliminación y completado de tareas.
- Visualización de tareas mediante `RecyclerView` y `CardView`.
- Búsqueda y filtros por estado, prioridad y asignatura.
- Orden de tareas por recientes, fecha y prioridad.
- Gestión de asignaturas.
- Consulta de próximas entregas.
- Modo Enfoque asociado a tareas pendientes.
- Uso del sensor de proximidad dentro del Modo Enfoque.
- Registro e historial de sesiones de enfoque.
- Persistencia local mediante SQLite.
- Operaciones de base de datos en segundo plano mediante `ExecutorService`.
- Clase Contrato para centralizar nombres de tablas y columnas.
- Medidas básicas de seguridad para consultas, sesión y pantallas de credenciales.

## Tecnologías utilizadas

- Java 17.
- XML para interfaces Android.
- Android Studio.
- Gradle Groovy.
- Android Gradle Plugin 8.7.3.
- Gradle 8.9.
- SQLite y `SQLiteOpenHelper`.
- `ExecutorService` para operaciones SQLite fuera del hilo principal.
- RecyclerView 1.4.0.
- CardView 1.0.0.
- ConstraintLayout 2.2.1.
- SharedPreferences para la sesión.
- Sensor de proximidad de Android.

## Requisitos del proyecto

| Elemento | Versión / valor |
|---|---|
| `applicationId` | `com.example.unitasky` |
| `minSdk` | 29 |
| `compileSdk` | 35 |
| `targetSdk` | 35 |
| Java | 17 |
| Android Gradle Plugin | 8.7.3 |
| Gradle Wrapper | 8.9 |
| Base de datos | `unitasky.db` |
| Versión SQLite de la aplicación | 4 |

## Instalación y ejecución

1. Clonar el repositorio:

```bash
git clone https://github.com/DzzAlzz/Unitasky.git
```

2. Abrir Android Studio y seleccionar la carpeta del proyecto.
3. Esperar la sincronización de Gradle.
4. Verificar que Android SDK 35 se encuentre disponible.
5. Seleccionar un emulador o dispositivo con Android 10 (API 29) o superior.
6. Ejecutar el módulo `app`.

## Persistencia de datos

La persistencia local se organiza de forma sencilla mediante:

```text
UniTaskyContract
       ↓
DatabaseHelper
       ↓
SQLite (unitasky.db)
```

`UniTaskyContract` centraliza los nombres de tablas y columnas, mientras que `DatabaseHelper` extiende `SQLiteOpenHelper` y administra la creación, migración y operaciones CRUD.

Las tablas actuales son:

- `usuarios`
- `asignaturas`
- `tareas`
- `sesiones_enfoque`

La base existente mantiene `id` como clave primaria por compatibilidad con los avances anteriores.

## Seguridad implementada

UniTasky incorpora medidas básicas acordes al alcance del proyecto:

- Consultas con argumentos parametrizados (`selectionArgs`) cuando se utilizan valores dinámicos.
- Uso de `ContentValues` en inserciones y actualizaciones.
- Cierre de objetos `Cursor` después de las consultas.
- Activities internas configuradas con `android:exported="false"`; solamente la Activity launcher se mantiene exportada.
- `android:allowBackup="false"` para evitar respaldos innecesarios de los datos locales.
- Contraseñas almacenadas como hash SHA-256 mediante `PasswordUtils`, no como texto plano.
- `SessionManager` conserva únicamente el estado de sesión y el ID del usuario.
- `FLAG_SECURE` en Login y Registro para proteger las pantallas donde se ingresan credenciales.

Estas medidas corresponden a una protección básica del proyecto y no se presentan como un sistema de seguridad absoluto.

## Documentación técnica

Los diagramas editables se encuentran en:

```text
docs/diagramas/UniTasky_Diagrama_Navegacion.drawio
docs/diagramas/UniTasky_ERD_SQLite.drawio
```

Los archivos `.drawio` utilizan XML nativo sin comprimir y pueden abrirse directamente con diagrams.net / Draw.io.

## Capturas de pantalla

La carpeta para las capturas reales de la aplicación queda preparada en:

```text
docs/capturas/
```

Rutas sugeridas para incorporarlas posteriormente:

```text
docs/capturas/inicio.png
docs/capturas/login.png
docs/capturas/mis_tareas.png
docs/capturas/mis_asignaturas.png
docs/capturas/modo_enfoque.png
```

Ejemplo de cómo agregarlas al README cuando estén disponibles:

```markdown
![Inicio de UniTasky](docs/capturas/inicio.png)
```

## Estructura principal

```text
app/src/main/java/com/example/unitasky/
├── adapter/
├── database/
│   ├── DatabaseHelper.java
│   └── UniTaskyContract.java
├── model/
├── repository/
├── util/
└── Activities de la aplicación

docs/
├── capturas/
└── diagramas/
```

## Diagramas

### Navegación

El diagrama de navegación representa el flujo principal entre las 13 Activities actuales y los datos más importantes enviados mediante Intents.

### Modelo Entidad-Relación

El ERD representa las cuatro tablas SQLite y sus relaciones lógicas. Actualmente los campos `usuario_id`, `asignatura_id` y `tarea_id` relacionan información dentro de la aplicación, pero los `CREATE TABLE` no declaran restricciones `FOREIGN KEY`; por esta razón el diagrama no las presenta como constraints físicas de SQLite.
