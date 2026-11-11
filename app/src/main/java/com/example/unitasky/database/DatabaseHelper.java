package com.example.unitasky.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.unitasky.model.Asignatura;
import com.example.unitasky.model.SesionEnfoque;
import com.example.unitasky.model.Tarea;
import com.example.unitasky.model.Usuario;
import com.example.unitasky.util.PasswordUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Administra la base de datos SQLite de UniTasky.
 * Centraliza la creación, migración y operaciones CRUD de usuarios, asignaturas, tareas y sesiones de enfoque.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "unitasky.db";
    private static final int VERSION_BD = 4;

    // Los nombres de tablas y columnas provienen de la Contract Class.
    private static final String TABLA_USUARIOS = UniTaskyContract.UsuarioEntry.TABLE_NAME;
    private static final String COL_USUARIO_PK = UniTaskyContract.UsuarioEntry.COLUMN_ID;
    private static final String COL_USUARIO_NOMBRE = UniTaskyContract.UsuarioEntry.COLUMN_NOMBRE;
    private static final String COL_USUARIO_CORREO = UniTaskyContract.UsuarioEntry.COLUMN_CORREO;
    private static final String COL_USUARIO_PASSWORD = UniTaskyContract.UsuarioEntry.COLUMN_PASSWORD;

    private static final String TABLA_ASIGNATURAS = UniTaskyContract.AsignaturaEntry.TABLE_NAME;
    private static final String COL_ASIGNATURA_PK = UniTaskyContract.AsignaturaEntry.COLUMN_ID;
    private static final String COL_ASIGNATURA_NOMBRE = UniTaskyContract.AsignaturaEntry.COLUMN_NOMBRE;
    private static final String COL_ASIGNATURA_USUARIO_ID = UniTaskyContract.AsignaturaEntry.COLUMN_USUARIO_ID;
    private static final String ALIAS_ASIGNATURA_NOMBRE = "asignatura_nombre";

    private static final String TABLA_TAREAS = UniTaskyContract.TareaEntry.TABLE_NAME;
    private static final String COL_TAREA_PK = UniTaskyContract.TareaEntry.COLUMN_ID;
    private static final String COL_TITULO = UniTaskyContract.TareaEntry.COLUMN_TITULO;
    private static final String COL_DESCRIPCION = UniTaskyContract.TareaEntry.COLUMN_DESCRIPCION;
    private static final String COL_TIPO = UniTaskyContract.TareaEntry.COLUMN_TIPO;
    private static final String COL_FECHA = UniTaskyContract.TareaEntry.COLUMN_FECHA;
    private static final String COL_PRIORIDAD = UniTaskyContract.TareaEntry.COLUMN_PRIORIDAD;
    private static final String COL_ESTADO = UniTaskyContract.TareaEntry.COLUMN_ESTADO;
    private static final String COL_ASIGNATURA_ID = UniTaskyContract.TareaEntry.COLUMN_ASIGNATURA_ID;
    private static final String COL_TAREA_USUARIO_ID = UniTaskyContract.TareaEntry.COLUMN_USUARIO_ID;

    private static final String TABLA_SESIONES_ENFOQUE = UniTaskyContract.SesionEnfoqueEntry.TABLE_NAME;
    private static final String COL_SESION_PK = UniTaskyContract.SesionEnfoqueEntry.COLUMN_ID;
    private static final String COL_SESION_TAREA_ID = UniTaskyContract.SesionEnfoqueEntry.COLUMN_TAREA_ID;
    private static final String COL_SESION_FECHA = UniTaskyContract.SesionEnfoqueEntry.COLUMN_FECHA;
    private static final String COL_SESION_DURACION_SEGUNDOS = UniTaskyContract.SesionEnfoqueEntry.COLUMN_DURACION_SEGUNDOS;

    public static final String ORDEN_RECIENTES = "recientes";
    public static final String ORDEN_FECHA = "fecha";
    public static final String ORDEN_PRIORIDAD = "prioridad";

    private static final String USUARIO_INICIAL_NOMBRE = "Denzel";
    private static final String USUARIO_INICIAL_CORREO = "dalvarez79@gmail.com";
    // Hash SHA-256 de la credencial inicial. La contraseña no se conserva en texto plano.
    private static final String USUARIO_INICIAL_PASSWORD_HASH =
            "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

    /**
     * Crea el helper encargado de abrir y administrar la base de datos local.
     *
     * @param context contexto de la aplicación.
     */
    public DatabaseHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    /**
     * Crea las tablas necesarias cuando la base de datos se genera por primera vez.
     *
     * @param db base de datos entregada por SQLiteOpenHelper.
     * @throws IllegalStateException si no se puede crear el usuario inicial requerido por la app.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        crearTablaUsuarios(db);
        long usuarioInicialId = asegurarUsuarioInicial(db);
        crearTablaAsignaturas(db);
        crearTablaTareas(db);
        crearTablaSesionesEnfoque(db);

        // En una instalación nueva todavía no hay datos antiguos que migrar.
        // El ID se obtiene aquí para garantizar que el usuario inicial exista desde el comienzo.
        if (usuarioInicialId <= 0) {
            throw new IllegalStateException("No se pudo crear el usuario inicial de UniTasky");
        }
    }

    private void crearTablaUsuarios(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLA_USUARIOS + " (" +
                COL_USUARIO_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USUARIO_NOMBRE + " TEXT NOT NULL, " +
                COL_USUARIO_CORREO + " TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
                COL_USUARIO_PASSWORD + " TEXT NOT NULL" +
                ")";
        db.execSQL(sql);
    }

    private void crearTablaAsignaturas(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLA_ASIGNATURAS + " (" +
                COL_ASIGNATURA_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ASIGNATURA_NOMBRE + " TEXT NOT NULL, " +
                COL_ASIGNATURA_USUARIO_ID + " INTEGER" +
                ")";
        db.execSQL(sql);
    }

    private void crearTablaTareas(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLA_TAREAS + " (" +
                COL_TAREA_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITULO + " TEXT NOT NULL, " +
                COL_DESCRIPCION + " TEXT, " +
                COL_TIPO + " TEXT NOT NULL, " +
                COL_FECHA + " TEXT NOT NULL, " +
                COL_PRIORIDAD + " TEXT NOT NULL, " +
                COL_ESTADO + " TEXT NOT NULL, " +
                COL_ASIGNATURA_ID + " INTEGER, " +
                COL_TAREA_USUARIO_ID + " INTEGER" +
                ")";
        db.execSQL(sql);
    }

    private void crearTablaSesionesEnfoque(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLA_SESIONES_ENFOQUE + " (" +
                COL_SESION_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SESION_TAREA_ID + " INTEGER NOT NULL, " +
                COL_SESION_FECHA + " TEXT NOT NULL, " +
                COL_SESION_DURACION_SEGUNDOS + " INTEGER NOT NULL" +
                ")";
        db.execSQL(sql);
    }

    /**
     * Aplica las migraciones correspondientes entre versiones anteriores de UniTasky.
     *
     * @param db base de datos que se está actualizando.
     * @param oldVersion versión existente antes de la actualización.
     * @param newVersion nueva versión solicitada por SQLiteOpenHelper.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            crearTablaAsignaturasVersion2(db);
            if (!columnaExiste(db, TABLA_TAREAS, COL_ASIGNATURA_ID)) {
                db.execSQL("ALTER TABLE " + TABLA_TAREAS +
                        " ADD COLUMN " + COL_ASIGNATURA_ID + " INTEGER");
            }
        }

        if (oldVersion < 3) {
            db.beginTransaction();
            try {
                crearTablaUsuarios(db);
                long usuarioInicialId = asegurarUsuarioInicial(db);

                if (!columnaExiste(db, TABLA_TAREAS, COL_TAREA_USUARIO_ID)) {
                    db.execSQL("ALTER TABLE " + TABLA_TAREAS +
                            " ADD COLUMN " + COL_TAREA_USUARIO_ID + " INTEGER");
                }

                if (!columnaExiste(db, TABLA_ASIGNATURAS, COL_ASIGNATURA_USUARIO_ID)) {
                    db.execSQL("ALTER TABLE " + TABLA_ASIGNATURAS +
                            " ADD COLUMN " + COL_ASIGNATURA_USUARIO_ID + " INTEGER");
                }

                // Todos los datos creados en los avances anteriores se conservan
                // y quedan asociados al usuario inicial.
                ContentValues usuarioTareas = new ContentValues();
                usuarioTareas.put(COL_TAREA_USUARIO_ID, usuarioInicialId);
                db.update(
                        TABLA_TAREAS,
                        usuarioTareas,
                        COL_TAREA_USUARIO_ID + " IS NULL",
                        null
                );

                ContentValues usuarioAsignaturas = new ContentValues();
                usuarioAsignaturas.put(COL_ASIGNATURA_USUARIO_ID, usuarioInicialId);
                db.update(
                        TABLA_ASIGNATURAS,
                        usuarioAsignaturas,
                        COL_ASIGNATURA_USUARIO_ID + " IS NULL",
                        null
                );

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        if (oldVersion < 4) {
            // Avance 20: solo se agrega la tabla de sesiones.
            // Las tablas y datos existentes se conservan.
            crearTablaSesionesEnfoque(db);
        }
    }

    private void crearTablaAsignaturasVersion2(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLA_ASIGNATURAS + " (" +
                COL_ASIGNATURA_PK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ASIGNATURA_NOMBRE + " TEXT NOT NULL" +
                ")";
        db.execSQL(sql);
    }

    private boolean columnaExiste(SQLiteDatabase db, String tabla, String columna) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tabla + ")", null);
        try {
            int indiceNombre = cursor.getColumnIndexOrThrow("name");
            while (cursor.moveToNext()) {
                if (columna.equalsIgnoreCase(cursor.getString(indiceNombre))) {
                    return true;
                }
            }
            return false;
        } finally {
            cursor.close();
        }
    }

    private long asegurarUsuarioInicial(SQLiteDatabase db) {
        Cursor cursor = db.query(
                TABLA_USUARIOS,
                new String[]{COL_USUARIO_PK},
                "LOWER(" + COL_USUARIO_CORREO + ") = LOWER(?)",
                new String[]{USUARIO_INICIAL_CORREO},
                null,
                null,
                null,
                "1"
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(COL_USUARIO_PK));
            }
        } finally {
            cursor.close();
        }

        ContentValues valores = new ContentValues();
        valores.put(COL_USUARIO_NOMBRE, USUARIO_INICIAL_NOMBRE);
        valores.put(COL_USUARIO_CORREO, USUARIO_INICIAL_CORREO);
        valores.put(COL_USUARIO_PASSWORD, USUARIO_INICIAL_PASSWORD_HASH);
        return db.insertOrThrow(TABLA_USUARIOS, null, valores);
    }

    // -------------------- USUARIOS --------------------

    /**
     * Inserta un usuario y almacena su contraseña procesada mediante PasswordUtils.
     *
     * @param nombre nombre del usuario.
     * @param correo correo utilizado para iniciar sesión.
     * @param password contraseña ingresada por el usuario.
     * @return ID generado por SQLite o -1 si la inserción falla.
     */
    public long insertarUsuario(String nombre, String correo, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_USUARIO_NOMBRE, nombre.trim());
        valores.put(COL_USUARIO_CORREO, correo.trim().toLowerCase());
        valores.put(COL_USUARIO_PASSWORD, PasswordUtils.sha256(password));
        return db.insert(TABLA_USUARIOS, null, valores);
    }

    /**
     * Comprueba si un correo ya se encuentra registrado.
     *
     * @param correo correo que se desea buscar.
     * @return true si el correo existe; false en caso contrario.
     */
    public boolean existeCorreo(String correo) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_USUARIOS,
                new String[]{COL_USUARIO_PK},
                "LOWER(TRIM(" + COL_USUARIO_CORREO + ")) = LOWER(TRIM(?))",
                new String[]{correo},
                null,
                null,
                null,
                "1"
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    /**
     * Valida las credenciales de un usuario utilizando correo y contraseña procesada.
     *
     * @param correo correo ingresado en el Login.
     * @param password contraseña ingresada en el Login.
     * @return usuario encontrado o null si las credenciales no son válidas.
     */
    public Usuario validarUsuario(String correo, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_USUARIOS,
                new String[]{COL_USUARIO_PK, COL_USUARIO_NOMBRE, COL_USUARIO_CORREO},
                "LOWER(TRIM(" + COL_USUARIO_CORREO + ")) = LOWER(TRIM(?)) AND " +
                        COL_USUARIO_PASSWORD + " = ?",
                new String[]{correo, PasswordUtils.sha256(password)},
                null,
                null,
                null,
                "1"
        );

        try {
            if (cursor.moveToFirst()) {
                return crearUsuarioDesdeCursor(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    /**
     * Obtiene un usuario mediante su identificador.
     *
     * @param id identificador del usuario.
     * @return usuario encontrado o null si no existe.
     */
    public Usuario obtenerUsuarioPorId(long id) {
        if (id <= 0) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_USUARIOS,
                new String[]{COL_USUARIO_PK, COL_USUARIO_NOMBRE, COL_USUARIO_CORREO},
                COL_USUARIO_PK + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                "1"
        );

        try {
            if (cursor.moveToFirst()) {
                return crearUsuarioDesdeCursor(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private Usuario crearUsuarioDesdeCursor(Cursor cursor) {
        return new Usuario(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_USUARIO_PK)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_NOMBRE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_CORREO))
        );
    }

    // -------------------- ASIGNATURAS POR USUARIO --------------------

    /**
     * Inserta una asignatura asociada al usuario actual.
     *
     * @param asignatura asignatura que se desea guardar.
     * @param usuarioId identificador del usuario propietario.
     * @return ID generado por SQLite o -1 si la operación falla.
     */
    public long insertarAsignatura(Asignatura asignatura, long usuarioId) {
        if (usuarioId <= 0) {
            return -1;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_ASIGNATURA_NOMBRE, asignatura.getNombre().trim());
        valores.put(COL_ASIGNATURA_USUARIO_ID, usuarioId);
        return db.insert(TABLA_ASIGNATURAS, null, valores);
    }

    /**
     * Comprueba si el usuario ya posee una asignatura con el mismo nombre.
     *
     * @param nombre nombre que se desea comprobar.
     * @param idExcluir ID que debe ignorarse durante una edición; 0 si no aplica.
     * @param usuarioId identificador del usuario actual.
     * @return true si existe una asignatura coincidente.
     */
    public boolean existeAsignatura(String nombre, long idExcluir, long usuarioId) {
        if (usuarioId <= 0) {
            return false;
        }
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(COL_ASIGNATURA_PK)
                .append(" FROM ").append(TABLA_ASIGNATURAS)
                .append(" WHERE ").append(COL_ASIGNATURA_USUARIO_ID).append(" = ?")
                .append(" AND LOWER(TRIM(").append(COL_ASIGNATURA_NOMBRE)
                .append(")) = LOWER(TRIM(?))");

        List<String> argumentos = new ArrayList<>();
        argumentos.add(String.valueOf(usuarioId));
        argumentos.add(nombre);

        if (idExcluir > 0) {
            sql.append(" AND ").append(COL_ASIGNATURA_PK).append(" <> ?");
            argumentos.add(String.valueOf(idExcluir));
        }
        sql.append(" LIMIT 1");

        Cursor cursor = db.rawQuery(sql.toString(), argumentos.toArray(new String[0]));
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    /**
     * Actualiza el nombre de una asignatura perteneciente al usuario.
     *
     * @param id identificador de la asignatura.
     * @param nombre nuevo nombre de la asignatura.
     * @param usuarioId identificador del usuario propietario.
     * @return true si se actualizó al menos una fila.
     */
    public boolean actualizarAsignatura(long id, String nombre, long usuarioId) {
        if (usuarioId <= 0) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_ASIGNATURA_NOMBRE, nombre.trim());

        int filasActualizadas = db.update(
                TABLA_ASIGNATURAS,
                valores,
                COL_ASIGNATURA_PK + " = ? AND " + COL_ASIGNATURA_USUARIO_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)}
        );
        return filasActualizadas > 0;
    }

    /**
     * Elimina una asignatura y deja sin asignatura las tareas que estaban asociadas a ella.
     *
     * @param id identificador de la asignatura.
     * @param usuarioId identificador del usuario propietario.
     * @return true si la asignatura fue eliminada.
     */
    public boolean eliminarAsignatura(long id, long usuarioId) {
        if (usuarioId <= 0) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues valoresTareas = new ContentValues();
            valoresTareas.putNull(COL_ASIGNATURA_ID);
            db.update(
                    TABLA_TAREAS,
                    valoresTareas,
                    COL_ASIGNATURA_ID + " = ? AND " + COL_TAREA_USUARIO_ID + " = ?",
                    new String[]{String.valueOf(id), String.valueOf(usuarioId)}
            );

            int filasEliminadas = db.delete(
                    TABLA_ASIGNATURAS,
                    COL_ASIGNATURA_PK + " = ? AND " + COL_ASIGNATURA_USUARIO_ID + " = ?",
                    new String[]{String.valueOf(id), String.valueOf(usuarioId)}
            );

            if (filasEliminadas > 0) {
                db.setTransactionSuccessful();
                return true;
            }
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Obtiene las asignaturas registradas por un usuario.
     *
     * @param usuarioId identificador del usuario actual.
     * @return lista de asignaturas ordenada por nombre.
     */
    public List<Asignatura> obtenerAsignaturas(long usuarioId) {
        List<Asignatura> asignaturas = new ArrayList<>();
        if (usuarioId <= 0) {
            return asignaturas;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_ASIGNATURAS,
                null,
                COL_ASIGNATURA_USUARIO_ID + " = ?",
                new String[]{String.valueOf(usuarioId)},
                null,
                null,
                "LOWER(" + COL_ASIGNATURA_NOMBRE + ") ASC, " + COL_ASIGNATURA_PK + " ASC"
        );

        try {
            int indiceId = cursor.getColumnIndexOrThrow(COL_ASIGNATURA_PK);
            int indiceNombre = cursor.getColumnIndexOrThrow(COL_ASIGNATURA_NOMBRE);
            while (cursor.moveToNext()) {
                asignaturas.add(new Asignatura(
                        cursor.getLong(indiceId),
                        cursor.getString(indiceNombre)
                ));
            }
        } finally {
            cursor.close();
        }
        return asignaturas;
    }

    /**
     * Busca una asignatura por ID comprobando que pertenezca al usuario.
     *
     * @param id identificador de la asignatura.
     * @param usuarioId identificador del usuario actual.
     * @return asignatura encontrada o null si no existe.
     */
    public Asignatura obtenerAsignaturaPorId(long id, long usuarioId) {
        if (id <= 0 || usuarioId <= 0) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_ASIGNATURAS,
                null,
                COL_ASIGNATURA_PK + " = ? AND " + COL_ASIGNATURA_USUARIO_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)},
                null,
                null,
                null,
                "1"
        );

        try {
            if (cursor.moveToFirst()) {
                return new Asignatura(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ASIGNATURA_PK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ASIGNATURA_NOMBRE))
                );
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    // -------------------- TAREAS POR USUARIO --------------------

    /**
     * Inserta una nueva tarea asociada al usuario actual.
     *
     * @param tarea objeto con los datos de la tarea.
     * @param usuarioId identificador del usuario propietario.
     * @return ID generado por SQLite o -1 si no se puede insertar.
     */
    public long insertarTarea(Tarea tarea, long usuarioId) {
        if (usuarioId <= 0) {
            return -1;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_TITULO, tarea.getTitulo());
        valores.put(COL_DESCRIPCION, tarea.getDescripcion());
        valores.put(COL_TIPO, tarea.getTipo());
        valores.put(COL_FECHA, tarea.getFecha());
        valores.put(COL_PRIORIDAD, tarea.getPrioridad());
        valores.put(COL_ESTADO, tarea.getEstado());
        valores.put(COL_TAREA_USUARIO_ID, usuarioId);

        long asignaturaId = tarea.getAsignaturaId();
        if (!asignaturaPerteneceUsuario(db, asignaturaId, usuarioId)) {
            asignaturaId = 0;
        }
        agregarAsignaturaAValores(valores, asignaturaId);
        return db.insert(TABLA_TAREAS, null, valores);
    }

    /**
     * Obtiene todas las tareas de un usuario aplicando el criterio de orden indicado.
     *
     * @param usuarioId identificador del usuario actual.
     * @param criterioOrden criterio definido por las constantes ORDEN_*.
     * @return lista de tareas del usuario.
     */
    public List<Tarea> obtenerTareas(long usuarioId, String criterioOrden) {
        return consultarTareasUsuario(usuarioId, null, null, obtenerOrdenSql(criterioOrden), null);
    }

    /**
     * Obtiene las tareas de un usuario filtradas por estado.
     *
     * @param estado estado que se desea consultar.
     * @param criterioOrden criterio utilizado para ordenar los resultados.
     * @param usuarioId identificador del usuario actual.
     * @return lista de tareas que cumplen el estado indicado.
     */
    public List<Tarea> obtenerTareasPorEstado(String estado, String criterioOrden, long usuarioId) {
        return consultarTareasUsuario(
                usuarioId,
                TABLA_TAREAS + "." + COL_ESTADO + " = ?",
                new String[]{estado},
                obtenerOrdenSql(criterioOrden),
                null
        );
    }

    /**
     * Obtiene las tareas de un usuario filtradas por prioridad.
     *
     * @param prioridad prioridad que se desea consultar.
     * @param criterioOrden criterio utilizado para ordenar los resultados.
     * @param usuarioId identificador del usuario actual.
     * @return lista de tareas que cumplen la prioridad indicada.
     */
    public List<Tarea> obtenerTareasPorPrioridad(String prioridad, String criterioOrden, long usuarioId) {
        return consultarTareasUsuario(
                usuarioId,
                TABLA_TAREAS + "." + COL_PRIORIDAD + " = ?",
                new String[]{prioridad},
                obtenerOrdenSql(criterioOrden),
                null
        );
    }

    /**
     * Consulta tareas combinando búsqueda, estado, prioridad, asignatura y orden.
     *
     * @param textoBusqueda texto utilizado para buscar por título.
     * @param estado estado seleccionado o null si no se filtra por estado.
     * @param prioridad prioridad seleccionada o null si no se filtra por prioridad.
     * @param asignaturaId ID de la asignatura seleccionada.
     * @param soloSinAsignatura indica si se desean únicamente tareas sin asignatura.
     * @param criterioOrden criterio de orden de los resultados.
     * @param usuarioId identificador del usuario actual.
     * @return lista de tareas que cumplen los filtros.
     */
    public List<Tarea> obtenerTareasFiltradas(String textoBusqueda,
                                               String estado,
                                               String prioridad,
                                               long asignaturaId,
                                               boolean soloSinAsignatura,
                                               String criterioOrden,
                                               long usuarioId) {
        List<String> condiciones = new ArrayList<>();
        List<String> argumentos = new ArrayList<>();

        String texto = textoBusqueda == null ? "" : textoBusqueda.trim();
        if (!texto.isEmpty()) {
            condiciones.add("LOWER(" + TABLA_TAREAS + "." + COL_TITULO + ") LIKE LOWER(?)");
            argumentos.add("%" + texto + "%");
        }

        if (estado != null && !estado.trim().isEmpty()) {
            condiciones.add(TABLA_TAREAS + "." + COL_ESTADO + " = ?");
            argumentos.add(estado);
        }

        if (prioridad != null && !prioridad.trim().isEmpty()) {
            condiciones.add(TABLA_TAREAS + "." + COL_PRIORIDAD + " = ?");
            argumentos.add(prioridad);
        }

        if (soloSinAsignatura) {
            condiciones.add(TABLA_TAREAS + "." + COL_ASIGNATURA_ID + " IS NULL");
        } else if (asignaturaId > 0) {
            condiciones.add(TABLA_TAREAS + "." + COL_ASIGNATURA_ID + " = ?");
            argumentos.add(String.valueOf(asignaturaId));
        }

        String seleccion = condiciones.isEmpty() ? null : String.join(" AND ", condiciones);
        String[] args = argumentos.isEmpty() ? null : argumentos.toArray(new String[0]);

        return consultarTareasUsuario(
                usuarioId,
                seleccion,
                args,
                obtenerOrdenSql(criterioOrden),
                null
        );
    }

    /**
     * Obtiene las próximas entregas pendientes del usuario.
     *
     * @param estadoPendiente texto utilizado para identificar tareas pendientes.
     * @param usuarioId identificador del usuario actual.
     * @return lista de próximas tareas ordenadas por fecha.
     */
    public List<Tarea> obtenerProximasEntregas(String estadoPendiente, long usuarioId) {
        String seleccion = TABLA_TAREAS + "." + COL_ESTADO + " = ? AND " +
                TABLA_TAREAS + "." + COL_FECHA + " IS NOT NULL AND TRIM(" +
                TABLA_TAREAS + "." + COL_FECHA + ") <> ''";

        return consultarTareasUsuario(
                usuarioId,
                seleccion,
                new String[]{estadoPendiente},
                obtenerOrdenSql(ORDEN_FECHA),
                null
        );
    }

    /**
     * Cuenta las tareas del usuario que tienen un estado determinado.
     *
     * @param estado estado que se desea contar.
     * @param usuarioId identificador del usuario actual.
     * @return cantidad de tareas encontradas.
     */
    public int contarTareasPorEstado(String estado, long usuarioId) {
        if (usuarioId <= 0) {
            return 0;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLA_TAREAS +
                        " WHERE " + COL_ESTADO + " = ? AND " + COL_TAREA_USUARIO_ID + " = ?",
                new String[]{estado, String.valueOf(usuarioId)}
        );
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    /**
     * Cuenta las asignaturas registradas por un usuario.
     *
     * @param usuarioId identificador del usuario actual.
     * @return cantidad de asignaturas del usuario.
     */
    public int contarAsignaturas(long usuarioId) {
        if (usuarioId <= 0) {
            return 0;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLA_ASIGNATURAS +
                        " WHERE " + COL_ASIGNATURA_USUARIO_ID + " = ?",
                new String[]{String.valueOf(usuarioId)}
        );
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    /**
     * Obtiene la entrega pendiente más próxima del usuario.
     *
     * @param estadoPendiente estado utilizado para identificar las tareas pendientes.
     * @param usuarioId identificador del usuario actual.
     * @return próxima tarea o null si no existen entregas pendientes.
     */
    public Tarea obtenerProximaEntrega(String estadoPendiente, long usuarioId) {
        String seleccion = TABLA_TAREAS + "." + COL_ESTADO + " = ? AND " +
                TABLA_TAREAS + "." + COL_FECHA + " IS NOT NULL AND TRIM(" +
                TABLA_TAREAS + "." + COL_FECHA + ") <> ''";

        List<Tarea> resultado = consultarTareasUsuario(
                usuarioId,
                seleccion,
                new String[]{estadoPendiente},
                obtenerOrdenSql(ORDEN_FECHA),
                "1"
        );
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    /**
     * Busca una tarea por ID comprobando que pertenezca al usuario.
     *
     * @param id identificador de la tarea.
     * @param usuarioId identificador del usuario actual.
     * @return tarea encontrada o null si no existe.
     */
    public Tarea obtenerTareaPorId(long id, long usuarioId) {
        List<Tarea> resultado = consultarTareasUsuario(
                usuarioId,
                TABLA_TAREAS + "." + COL_TAREA_PK + " = ?",
                new String[]{String.valueOf(id)},
                null,
                "1"
        );
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    /**
     * Actualiza los datos editables de una tarea del usuario.
     *
     * @param id identificador de la tarea.
     * @param titulo nuevo título.
     * @param descripcion nueva descripción.
     * @param tipo nuevo tipo de tarea.
     * @param fecha nueva fecha de entrega.
     * @param prioridad nueva prioridad.
     * @param asignaturaId asignatura asociada o 0 si no corresponde.
     * @param usuarioId identificador del usuario propietario.
     * @return true si la actualización se realizó correctamente.
     */
    public boolean actualizarTarea(long id, String titulo, String descripcion,
                                   String tipo, String fecha, String prioridad,
                                   long asignaturaId, long usuarioId) {
        if (usuarioId <= 0) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_TITULO, titulo);
        valores.put(COL_DESCRIPCION, descripcion);
        valores.put(COL_TIPO, tipo);
        valores.put(COL_FECHA, fecha);
        valores.put(COL_PRIORIDAD, prioridad);

        long asignaturaValidaId = asignaturaId;
        if (!asignaturaPerteneceUsuario(db, asignaturaValidaId, usuarioId)) {
            asignaturaValidaId = 0;
        }
        agregarAsignaturaAValores(valores, asignaturaValidaId);

        int filasActualizadas = db.update(
                TABLA_TAREAS,
                valores,
                COL_TAREA_PK + " = ? AND " + COL_TAREA_USUARIO_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)}
        );
        return filasActualizadas > 0;
    }

    /**
     * Elimina una tarea del usuario y sus sesiones de enfoque asociadas.
     *
     * @param id identificador de la tarea.
     * @param usuarioId identificador del usuario propietario.
     * @return true si la tarea fue eliminada.
     */
    public boolean eliminarTarea(long id, long usuarioId) {
        if (id <= 0 || usuarioId <= 0) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            String[] argumentos = new String[]{
                    String.valueOf(id),
                    String.valueOf(usuarioId)
            };

            // Solo elimina sesiones si la tarea realmente pertenece al usuario actual.
            db.delete(
                    TABLA_SESIONES_ENFOQUE,
                    COL_SESION_TAREA_ID + " IN (SELECT " + COL_TAREA_PK +
                            " FROM " + TABLA_TAREAS +
                            " WHERE " + COL_TAREA_PK + " = ? AND " + COL_TAREA_USUARIO_ID + " = ?)",
                    argumentos
            );

            int filasEliminadas = db.delete(
                    TABLA_TAREAS,
                    COL_TAREA_PK + " = ? AND " + COL_TAREA_USUARIO_ID + " = ?",
                    argumentos
            );

            if (filasEliminadas > 0) {
                db.setTransactionSuccessful();
                return true;
            }
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Guarda una sesión de enfoque asociada a una tarea.
     *
     * @param tareaId identificador de la tarea.
     * @param fecha fecha y hora registrada para la sesión.
     * @param duracionSegundos duración de la sesión en segundos.
     * @return ID de la sesión creada o -1 si los datos no son válidos.
     */
    public long insertarSesionEnfoque(long tareaId, String fecha, int duracionSegundos) {
        if (tareaId <= 0 || fecha == null || fecha.trim().isEmpty() || duracionSegundos <= 0) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_SESION_TAREA_ID, tareaId);
        valores.put(COL_SESION_FECHA, fecha.trim());
        valores.put(COL_SESION_DURACION_SEGUNDOS, duracionSegundos);
        return db.insert(TABLA_SESIONES_ENFOQUE, null, valores);
    }

    /**
     * Obtiene las sesiones de enfoque registradas para una tarea.
     *
     * @param tareaId identificador de la tarea.
     * @return lista de sesiones ordenadas desde la más reciente.
     */
    public List<SesionEnfoque> obtenerSesionesPorTarea(long tareaId) {
        List<SesionEnfoque> sesiones = new ArrayList<>();
        if (tareaId <= 0) {
            return sesiones;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_SESIONES_ENFOQUE,
                new String[]{
                        COL_SESION_PK,
                        COL_SESION_TAREA_ID,
                        COL_SESION_FECHA,
                        COL_SESION_DURACION_SEGUNDOS
                },
                COL_SESION_TAREA_ID + " = ?",
                new String[]{String.valueOf(tareaId)},
                null,
                null,
                COL_SESION_PK + " DESC"
        );

        try {
            int indiceId = cursor.getColumnIndexOrThrow(COL_SESION_PK);
            int indiceTareaId = cursor.getColumnIndexOrThrow(COL_SESION_TAREA_ID);
            int indiceFecha = cursor.getColumnIndexOrThrow(COL_SESION_FECHA);
            int indiceDuracion = cursor.getColumnIndexOrThrow(COL_SESION_DURACION_SEGUNDOS);

            while (cursor.moveToNext()) {
                sesiones.add(new SesionEnfoque(
                        cursor.getLong(indiceId),
                        cursor.getLong(indiceTareaId),
                        cursor.getString(indiceFecha),
                        cursor.getInt(indiceDuracion)
                ));
            }
        } finally {
            cursor.close();
        }

        return sesiones;
    }

    /**
     * Calcula el tiempo total de enfoque acumulado por una tarea.
     *
     * @param tareaId identificador de la tarea.
     * @return duración total acumulada en segundos.
     */
    public int obtenerTiempoTotalEnfoque(long tareaId) {
        if (tareaId <= 0) {
            return 0;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(" + COL_SESION_DURACION_SEGUNDOS + "), 0) AS total " +
                        "FROM " + TABLA_SESIONES_ENFOQUE +
                        " WHERE " + COL_SESION_TAREA_ID + " = ?",
                new String[]{String.valueOf(tareaId)}
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("total"));
            }
            return 0;
        } finally {
            cursor.close();
        }
    }

    /**
     * Calcula el tiempo total de enfoque acumulado en las tareas de un usuario.
     *
     * @param usuarioId identificador del usuario actual.
     * @return duración total acumulada en segundos.
     */
    public int obtenerTiempoTotalEnfoqueUsuario(long usuarioId) {
        if (usuarioId <= 0) {
            return 0;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(s." + COL_SESION_DURACION_SEGUNDOS + "), 0) AS total " +
                        "FROM " + TABLA_SESIONES_ENFOQUE + " s " +
                        "INNER JOIN " + TABLA_TAREAS + " t ON t." + COL_TAREA_PK +
                        " = s." + COL_SESION_TAREA_ID + " " +
                        "WHERE t." + COL_TAREA_USUARIO_ID + " = ?",
                new String[]{String.valueOf(usuarioId)}
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow("total"));
            }
            return 0;
        } finally {
            cursor.close();
        }
    }

    /**
     * Obtiene la sesión de enfoque más reciente perteneciente a las tareas del usuario.
     *
     * @param usuarioId identificador del usuario actual.
     * @return última sesión registrada o null si no existen sesiones.
     */
    public SesionEnfoque obtenerUltimaSesionEnfoqueUsuario(long usuarioId) {
        if (usuarioId <= 0) {
            return null;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT s." + COL_SESION_PK + ", s." + COL_SESION_TAREA_ID + ", s." +
                        COL_SESION_FECHA + ", s." + COL_SESION_DURACION_SEGUNDOS + " " +
                        "FROM " + TABLA_SESIONES_ENFOQUE + " s " +
                        "INNER JOIN " + TABLA_TAREAS + " t ON t." + COL_TAREA_PK +
                        " = s." + COL_SESION_TAREA_ID + " " +
                        "WHERE t." + COL_TAREA_USUARIO_ID + " = ? " +
                        "ORDER BY s." + COL_SESION_PK + " DESC LIMIT 1",
                new String[]{String.valueOf(usuarioId)}
        );

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return new SesionEnfoque(
                    cursor.getLong(0),
                    cursor.getLong(1),
                    cursor.getString(2),
                    cursor.getInt(3)
            );
        } finally {
            cursor.close();
        }
    }

    /**
     * Actualiza únicamente el estado de una tarea del usuario.
     *
     * @param id identificador de la tarea.
     * @param nuevoEstado nuevo estado que se desea guardar.
     * @param usuarioId identificador del usuario propietario.
     * @return true si se actualizó la tarea.
     */
    public boolean actualizarEstado(long id, String nuevoEstado, long usuarioId) {
        if (usuarioId <= 0) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_ESTADO, nuevoEstado);

        int filasActualizadas = db.update(
                TABLA_TAREAS,
                valores,
                COL_TAREA_PK + " = ? AND " + COL_TAREA_USUARIO_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)}
        );
        return filasActualizadas > 0;
    }

    /**
     * Ejecuta la consulta base de tareas de un usuario y agrega filtros opcionales.
     *
     * @param usuarioId identificador del usuario actual.
     * @param seleccionExtra condición adicional ya construida con marcadores ?.
     * @param argumentosExtra argumentos asociados a la condición adicional.
     * @param orden expresión de orden permitida por la aplicación.
     * @param limite límite opcional de resultados.
     * @return lista de tareas obtenidas desde SQLite.
     */
    private List<Tarea> consultarTareasUsuario(long usuarioId,
                                                String seleccionExtra,
                                                String[] argumentosExtra,
                                                String orden,
                                                String limite) {
        List<Tarea> tareas = new ArrayList<>();
        if (usuarioId <= 0) {
            return tareas;
        }

        List<String> argumentos = new ArrayList<>();
        argumentos.add(String.valueOf(usuarioId));

        StringBuilder seleccion = new StringBuilder();
        seleccion.append(TABLA_TAREAS).append(".").append(COL_TAREA_USUARIO_ID).append(" = ?");

        if (seleccionExtra != null && !seleccionExtra.trim().isEmpty()) {
            seleccion.append(" AND (").append(seleccionExtra).append(")");
            if (argumentosExtra != null) {
                for (String argumento : argumentosExtra) {
                    argumentos.add(argumento);
                }
            }
        }

        SQLiteDatabase db = getReadableDatabase();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append(TABLA_TAREAS).append(".*, ")
                .append(TABLA_ASIGNATURAS).append(".").append(COL_ASIGNATURA_NOMBRE)
                .append(" AS ").append(ALIAS_ASIGNATURA_NOMBRE)
                .append(" FROM ").append(TABLA_TAREAS)
                .append(" LEFT JOIN ").append(TABLA_ASIGNATURAS)
                .append(" ON ").append(TABLA_TAREAS).append(".").append(COL_ASIGNATURA_ID)
                .append(" = ").append(TABLA_ASIGNATURAS).append(".").append(COL_ASIGNATURA_PK)
                .append(" AND ").append(TABLA_ASIGNATURAS).append(".").append(COL_ASIGNATURA_USUARIO_ID)
                .append(" = ").append(TABLA_TAREAS).append(".").append(COL_TAREA_USUARIO_ID)
                .append(" WHERE ").append(seleccion);

        if (orden != null && !orden.trim().isEmpty()) {
            sql.append(" ORDER BY ").append(orden);
        }
        if (limite != null && !limite.trim().isEmpty()) {
            sql.append(" LIMIT ").append(limite);
        }

        Cursor cursor = db.rawQuery(sql.toString(), argumentos.toArray(new String[0]));
        try {
            while (cursor.moveToNext()) {
                tareas.add(crearTareaDesdeCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return tareas;
    }

    private String obtenerOrdenSql(String criterioOrden) {
        String fecha = TABLA_TAREAS + "." + COL_FECHA;
        String prioridad = TABLA_TAREAS + "." + COL_PRIORIDAD;
        String id = TABLA_TAREAS + "." + COL_TAREA_PK;

        if (ORDEN_FECHA.equals(criterioOrden)) {
            return "substr(" + fecha + ", 7, 4) || " +
                    "substr(" + fecha + ", 4, 2) || " +
                    "substr(" + fecha + ", 1, 2) ASC, " + id + " DESC";
        }
        if (ORDEN_PRIORIDAD.equals(criterioOrden)) {
            return "CASE " + prioridad +
                    " WHEN 'Alta' THEN 1" +
                    " WHEN 'Media' THEN 2" +
                    " WHEN 'Baja' THEN 3" +
                    " ELSE 4 END ASC, " + id + " DESC";
        }
        return id + " DESC";
    }

    /**
     * Comprueba que una asignatura corresponda al usuario antes de asociarla a una tarea.
     *
     * @param db base de datos utilizada por la operación actual.
     * @param asignaturaId identificador de la asignatura.
     * @param usuarioId identificador del usuario propietario.
     * @return true si la asignatura pertenece al usuario o si no se seleccionó asignatura.
     */
    private boolean asignaturaPerteneceUsuario(SQLiteDatabase db, long asignaturaId, long usuarioId) {
        if (asignaturaId <= 0) {
            return true;
        }
        if (usuarioId <= 0) {
            return false;
        }

        Cursor cursor = db.query(
                TABLA_ASIGNATURAS,
                new String[]{COL_ASIGNATURA_PK},
                COL_ASIGNATURA_PK + " = ? AND " + COL_ASIGNATURA_USUARIO_ID + " = ?",
                new String[]{String.valueOf(asignaturaId), String.valueOf(usuarioId)},
                null,
                null,
                null,
                "1"
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    private void agregarAsignaturaAValores(ContentValues valores, long asignaturaId) {
        if (asignaturaId > 0) {
            valores.put(COL_ASIGNATURA_ID, asignaturaId);
        } else {
            valores.putNull(COL_ASIGNATURA_ID);
        }
    }

    /**
     * Construye un objeto Tarea a partir de la fila actual de un Cursor.
     *
     * @param cursor cursor posicionado en una tarea válida.
     * @return objeto Tarea creado con los valores de la fila actual.
     */
    private Tarea crearTareaDesdeCursor(Cursor cursor) {
        int indiceId = cursor.getColumnIndexOrThrow(COL_TAREA_PK);
        int indiceTitulo = cursor.getColumnIndexOrThrow(COL_TITULO);
        int indiceDescripcion = cursor.getColumnIndexOrThrow(COL_DESCRIPCION);
        int indiceTipo = cursor.getColumnIndexOrThrow(COL_TIPO);
        int indiceFecha = cursor.getColumnIndexOrThrow(COL_FECHA);
        int indicePrioridad = cursor.getColumnIndexOrThrow(COL_PRIORIDAD);
        int indiceEstado = cursor.getColumnIndexOrThrow(COL_ESTADO);
        int indiceAsignaturaId = cursor.getColumnIndexOrThrow(COL_ASIGNATURA_ID);
        int indiceAsignaturaNombre = cursor.getColumnIndexOrThrow(ALIAS_ASIGNATURA_NOMBRE);

        long asignaturaId = cursor.isNull(indiceAsignaturaId) ? 0 : cursor.getLong(indiceAsignaturaId);
        String asignaturaNombre = cursor.isNull(indiceAsignaturaNombre)
                ? null
                : cursor.getString(indiceAsignaturaNombre);

        return new Tarea(
                cursor.getLong(indiceId),
                cursor.getString(indiceTitulo),
                cursor.getString(indiceDescripcion),
                cursor.getString(indiceFecha),
                cursor.getString(indiceTipo),
                cursor.getString(indicePrioridad),
                cursor.getString(indiceEstado),
                asignaturaId,
                asignaturaNombre
        );
    }
}
