UNITASKY - AVANCE 02
====================

Nombre visible de la app: UniTasky
Lenguaje: Java
Interfaz: XML (Views tradicionales)
Gradle: Groovy DSL (.gradle, no .kts)
Minimum SDK: API 29 - Android 10.0
Compile SDK: API 35
Target SDK: API 35
Rama preparada: Avance-02

CONTENIDO DE ESTE AVANCE
------------------------
- Pantalla de Login con diseño azul y amarillo.
- Pantalla de Registro.
- Pantalla de Inicio de UniTasky.
- Layouts XML visibles en Code, Split y Design.
- setContentView() para relacionar cada Activity con su layout.
- findViewById() para conectar elementos XML con Java.
- setOnClickListener() para botones y TextView clickeables.
- Intent para navegar entre Login, Registro e Inicio.
- Toast para mensajes simples.
- setError() para validaciones de correo y contraseña.
- Mostrar y ocultar contraseña en el Login.
- Validación de contraseña mínima de 6 caracteres.
- Validación de contraseñas iguales en Registro.

ACTIVITIES
----------
- LoginActivity.java
- RegistroActivity.java
- InicioActivity.java

LAYOUTS NUEVOS
--------------
- activity_login.xml
- activity_registro.xml
- activity_inicio.xml

IMPORTANTE
----------
Este avance NO utiliza todavía Firebase, base de datos, Room, Retrofit,
APIs, Fragments ni Jetpack Compose. El registro y login solo validan los
campos de forma local y no guardan usuarios.

La antigua MainActivity y activity_main.xml del Avance 01 se conservan
como parte del proyecto, pero LoginActivity es ahora la pantalla inicial.

PRUEBA EN ANDROID STUDIO
------------------------
1. Abrir la carpeta UniTasky_Avance02 en Android Studio.
2. Esperar la sincronización de Gradle.
3. Seleccionar el emulador Pixel 10 Pro.
4. Presionar Run 'app'.
5. Probar validaciones del Login.
6. Probar el enlace Regístrate y la pantalla de Registro.
7. Ingresar un correo válido y una contraseña de 6 o más caracteres.
8. Verificar que se abra la pantalla de Inicio.
9. Probar los tres botones del Inicio y sus mensajes Toast.

GIT
---
El proyecto se entrega con los cambios SIN commit para que el commit del
Avance 02 se realice después de comprobarlo en Android Studio.
