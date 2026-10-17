"UniTasky"

UniTasky es una aplicación Android orientada a estudiantes que permite organizar tareas, asignaturas y actividades académicas de manera sencilla.

El proyecto fue desarrollado progresivamente durante la asignatura de Programación Android, incorporando nuevas funcionalidades mediante distintos avances hasta llegar a una versión final estable.

- Objetivo

El objetivo de UniTasky es facilitar la organización académica del estudiante mediante el registro de tareas, asignaturas, fechas de entrega, prioridades y estados.

Además, la aplicación incorpora un Modo Enfoque que utiliza el sensor de proximidad del dispositivo para registrar sesiones de concentración relacionadas con una tarea específica.

- Funcionalidades

UniTasky permite registrar e iniciar sesión con diferentes usuarios.

Cada usuario mantiene sus propios datos de forma independiente.

La aplicación permite crear, editar y eliminar tareas.

Las tareas pueden contener título, descripción, tipo de actividad, asignatura, fecha de entrega, prioridad y estado.

También es posible crear, editar y eliminar asignaturas.

El sistema incluye búsqueda de tareas, filtros por estado y asignatura, ordenamiento y visualización de próximas entregas.

El dashboard principal muestra información académica como tareas pendientes, tareas completadas, asignaturas registradas y próxima entrega.

## Modo Enfoque

UniTasky incorpora un Modo Enfoque utilizando el sensor de proximidad del teléfono.

El usuario selecciona una tarea pendiente y cubre el sensor de proximidad durante aproximadamente 1,5 segundos.

Cuando el sensor detecta proximidad, comienza una sesión de enfoque y se inicia un temporizador.

Al retirar la mano del sensor, el temporizador se pausa.

La sesión puede guardarse posteriormente en SQLite y queda asociada mediante el identificador real de la tarea seleccionada.

Esto permite registrar cuánto tiempo de concentración se ha dedicado a una actividad académica específica.

## Historial de enfoque

Cada tarea puede contener varias sesiones de enfoque.

El historial muestra la fecha y duración de cada sesión registrada.

Las sesiones se muestran desde la más reciente hasta la más antigua.

También se calcula automáticamente el tiempo total de enfoque acumulado para cada tarea.

## Dashboard de enfoque

El dashboard principal muestra un resumen del Modo Enfoque correspondiente únicamente al usuario que tiene la sesión iniciada.

Se muestra el tiempo total de enfoque acumulado entre sus tareas y la última sesión de enfoque registrada.

Los datos de diferentes usuarios se mantienen separados.

## Base de datos

La aplicación utiliza una base de datos SQLite local llamada:

`unitasky.db`

La base de datos utiliza `SQLiteOpenHelper` y actualmente trabaja con la versión 4.

Contiene las siguientes tablas:

`usuarios`

`tareas`

`asignaturas`

`sesiones_enfoque`

Las sesiones de enfoque se relacionan con las tareas mediante `tarea_id`.

Las tareas y asignaturas se encuentran asociadas al usuario correspondiente.

- Tecnologías utilizadas

Android Studio

Java

XML

Gradle Groovy

SQLite

SQLiteOpenHelper

SensorManager

SensorEventListener

Sensor.TYPE_PROXIMITY

Handler

Runnable

Minimum SDK API 29

- Arquitectura del proyecto

El proyecto utiliza una estructura sencilla basada en Activities, modelos Java, layouts XML y una base de datos SQLite local.

Se utilizaron clases como:

`DatabaseHelper`

`TareaRepository`

`SessionManager`

`Tarea`

`Asignatura`

`Usuario`

`SesionEnfoque`

`ModoEnfoqueActivity`

`HistorialEnfoqueActivity`

`InicioActivity`

El proyecto fue mantenido intencionalmente simple para corresponder al nivel y requisitos de la asignatura.

## Persistencia de sesión

La aplicación mantiene la sesión del usuario mediante SharedPreferences.

La contraseña no se almacena en SharedPreferences.

Al volver a abrir UniTasky, el usuario puede regresar directamente a la pantalla principal mientras su sesión continúe activa.

Cerrar sesión no elimina las tareas, asignaturas ni sesiones de enfoque almacenadas.

## Desarrollo progresivo

El proyecto fue desarrollado mediante avances independientes almacenados en distintas ramas del repositorio.

Las ramas abarcan desde:

`Avance-01`

hasta:

`Avance-24`

Cada rama representa una etapa del desarrollo del proyecto.

La versión final del proyecto corresponde a:

`Avance-24`

La rama principal:

`main`

contiene actualmente la versión final del proyecto.

La rama:

`dev`

también contiene la versión final utilizada durante el desarrollo.

- Versión final

La versión final mantiene las principales funcionalidades desarrolladas durante el semestre.

Login y registro de usuarios

Sesión persistente

Separación de datos por usuario

CRUD de tareas

CRUD de asignaturas

Filtros y búsqueda

Próximas entregas

Dashboard académico

Modo Enfoque con sensor de proximidad

Temporizador de concentración

Guardado de sesiones en SQLite

Historial de sesiones por tarea

Tiempo total de enfoque por tarea

Tiempo total de enfoque del usuario

Última sesión de enfoque en el dashboard

- Ejecución

Para ejecutar el proyecto se recomienda utilizar Android Studio.

El proyecto utiliza Java, XML y Gradle Groovy.

El `minSdk` configurado es API 29.

Puede ejecutarse tanto en un emulador Android compatible como en un dispositivo físico mediante depuración USB.

Para comprobar completamente el funcionamiento del Modo Enfoque se recomienda utilizar un dispositivo que disponga de sensor de proximidad.

Android Emulator también permite simular el sensor desde:

`Extended Controls`

`Virtual Sensors`

`Additional Sensors`

`Proximity`

- Autor

Denzel Álvarez

Ingeniería en Informática

Proyecto académico desarrollado en Android Studio
