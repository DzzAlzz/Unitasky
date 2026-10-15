package com.example.unitasky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "unitasky.db";
    private static final int VERSION_BD = 4;

    public static final String TABLA_USUARIOS = "usuarios";
    public static final String COL_USUARIO_NOMBRE = "nombre";
    public static final String COL_USUARIO_CORREO = "correo";
    public static final String COL_USUARIO_PASSWORD = "password";
    public static final String COL_USUARIO_ID = "usuario_id";

    public static final String TABLA_TAREAS = "tareas";
    public static final String COL_ID = "id";
    public static final String COL_TITULO = "titulo";
    public static final String COL_DESCRIPCION = "descripcion";
    public static final String COL_TIPO = "tipo";
    public static final String COL_FECHA = "fecha";
    public static final String COL_PRIORIDAD = "prioridad";
    public static final String COL_ESTADO = "estado";
    public static final String COL_ASIGNATURA_ID = "asignatura_id";

    public static final String TABLA_ASIGNATURAS = "asignaturas";
    public static final String COL_ASIGNATURA_NOMBRE = "nombre";
    private static final String ALIAS_ASIGNATURA_NOMBRE = "asignatura_nombre";

    public static final String TABLA_SESIONES_ENFOQUE = "sesiones_enfoque";
    public static final String COL_SESION_TAREA_ID = "tarea_id";
    public static final String COL_SESION_FECHA = "fecha";
    public static final String COL_SESION_DURACION_SEGUNDOS = "duracion_segundos";

    public static final String ORDEN_RECIENTES = "recientes";
    public static final String ORDEN_FECHA = "fecha";
    public static final String ORDEN_PRIORIDAD = "prioridad";

    private static final String USUARIO_INICIAL_NOMBRE = "Denzel";
    private static final String USUARIO_INICIAL_CORREO = "dalvarez79@gmail.com";
    private static final String USUARIO_INICIAL_PASSWORD = "123456";

    public DatabaseHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

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
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USUARIO_NOMBRE + " TEXT NOT NULL, " +
                COL_USUARIO_CORREO + " TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
                COL_USUARIO_PASSWORD + " TEXT NOT NULL" +
                ")";
        db.execSQL(sql);
    }

    private void crearTablaAsignaturas(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLA_ASIGNATURAS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ASIGNATURA_NOMBRE + " TEXT NOT NULL, " +
                COL_USUARIO_ID + " INTEGER" +
                ")";
        db.execSQL(sql);
    }

    private void crearTablaTareas(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLA_TAREAS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITULO + " TEXT NOT NULL, " +
                COL_DESCRIPCION + " TEXT, " +
                COL_TIPO + " TEXT NOT NULL, " +
                COL_FECHA + " TEXT NOT NULL, " +
                COL_PRIORIDAD + " TEXT NOT NULL, " +
                COL_ESTADO + " TEXT NOT NULL, " +
                COL_ASIGNATURA_ID + " INTEGER, " +
                COL_USUARIO_ID + " INTEGER" +
                ")";
        db.execSQL(sql);
    }

    private void crearTablaSesionesEnfoque(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLA_SESIONES_ENFOQUE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SESION_TAREA_ID + " INTEGER NOT NULL, " +
                COL_SESION_FECHA + " TEXT NOT NULL, " +
                COL_SESION_DURACION_SEGUNDOS + " INTEGER NOT NULL" +
                ")";
        db.execSQL(sql);
    }

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

                if (!columnaExiste(db, TABLA_TAREAS, COL_USUARIO_ID)) {
                    db.execSQL("ALTER TABLE " + TABLA_TAREAS +
                            " ADD COLUMN " + COL_USUARIO_ID + " INTEGER");
                }

                if (!columnaExiste(db, TABLA_ASIGNATURAS, COL_USUARIO_ID)) {
                    db.execSQL("ALTER TABLE " + TABLA_ASIGNATURAS +
                            " ADD COLUMN " + COL_USUARIO_ID + " INTEGER");
                }

                // Todos los datos creados en los avances anteriores se conservan
                // y quedan asociados al usuario inicial.
                ContentValues usuarioTareas = new ContentValues();
                usuarioTareas.put(COL_USUARIO_ID, usuarioInicialId);
                db.update(
                        TABLA_TAREAS,
                        usuarioTareas,
                        COL_USUARIO_ID + " IS NULL",
                        null
                );

                ContentValues usuarioAsignaturas = new ContentValues();
                usuarioAsignaturas.put(COL_USUARIO_ID, usuarioInicialId);
                db.update(
                        TABLA_ASIGNATURAS,
                        usuarioAsignaturas,
                        COL_USUARIO_ID + " IS NULL",
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
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
                new String[]{COL_ID},
                "LOWER(" + COL_USUARIO_CORREO + ") = LOWER(?)",
                new String[]{USUARIO_INICIAL_CORREO},
                null,
                null,
                null,
                "1"
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
            }
        } finally {
            cursor.close();
        }

        ContentValues valores = new ContentValues();
        valores.put(COL_USUARIO_NOMBRE, USUARIO_INICIAL_NOMBRE);
        valores.put(COL_USUARIO_CORREO, USUARIO_INICIAL_CORREO);
        valores.put(COL_USUARIO_PASSWORD, PasswordUtils.sha256(USUARIO_INICIAL_PASSWORD));
        return db.insertOrThrow(TABLA_USUARIOS, null, valores);
    }

    // -------------------- USUARIOS --------------------

    public long insertarUsuario(String nombre, String correo, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_USUARIO_NOMBRE, nombre.trim());
        valores.put(COL_USUARIO_CORREO, correo.trim().toLowerCase());
        valores.put(COL_USUARIO_PASSWORD, PasswordUtils.sha256(password));
        return db.insert(TABLA_USUARIOS, null, valores);
    }

    public boolean existeCorreo(String correo) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_USUARIOS,
                new String[]{COL_ID},
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

    public Usuario validarUsuario(String correo, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_USUARIOS,
                new String[]{COL_ID, COL_USUARIO_NOMBRE, COL_USUARIO_CORREO},
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

    public Usuario obtenerUsuarioPorId(long id) {
        if (id <= 0) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_USUARIOS,
                new String[]{COL_ID, COL_USUARIO_NOMBRE, COL_USUARIO_CORREO},
                COL_ID + " = ?",
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
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_NOMBRE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USUARIO_CORREO))
        );
    }

    // -------------------- ASIGNATURAS POR USUARIO --------------------

    public long insertarAsignatura(Asignatura asignatura, long usuarioId) {
        if (usuarioId <= 0) {
            return -1;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_ASIGNATURA_NOMBRE, asignatura.getNombre().trim());
        valores.put(COL_USUARIO_ID, usuarioId);
        return db.insert(TABLA_ASIGNATURAS, null, valores);
    }

    public boolean existeAsignatura(String nombre, long idExcluir, long usuarioId) {
        if (usuarioId <= 0) {
            return false;
        }
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(COL_ID)
                .append(" FROM ").append(TABLA_ASIGNATURAS)
                .append(" WHERE ").append(COL_USUARIO_ID).append(" = ?")
                .append(" AND LOWER(TRIM(").append(COL_ASIGNATURA_NOMBRE)
                .append(")) = LOWER(TRIM(?))");

        List<String> argumentos = new ArrayList<>();
        argumentos.add(String.valueOf(usuarioId));
        argumentos.add(nombre);

        if (idExcluir > 0) {
            sql.append(" AND ").append(COL_ID).append(" <> ?");
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
                COL_ID + " = ? AND " + COL_USUARIO_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)}
        );
        return filasActualizadas > 0;
    }

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
                    COL_ASIGNATURA_ID + " = ? AND " + COL_USUARIO_ID + " = ?",
                    new String[]{String.valueOf(id), String.valueOf(usuarioId)}
            );

            int filasEliminadas = db.delete(
                    TABLA_ASIGNATURAS,
                    COL_ID + " = ? AND " + COL_USUARIO_ID + " = ?",
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

    public List<Asignatura> obtenerAsignaturas(long usuarioId) {
        List<Asignatura> asignaturas = new ArrayList<>();
        if (usuarioId <= 0) {
            return asignaturas;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_ASIGNATURAS,
                null,
                COL_USUARIO_ID + " = ?",
                new String[]{String.valueOf(usuarioId)},
                null,
                null,
                "LOWER(" + COL_ASIGNATURA_NOMBRE + ") ASC, " + COL_ID + " ASC"
        );

        try {
            int indiceId = cursor.getColumnIndexOrThrow(COL_ID);
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

    public Asignatura obtenerAsignaturaPorId(long id, long usuarioId) {
        if (id <= 0 || usuarioId <= 0) {
            return null;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_ASIGNATURAS,
                null,
                COL_ID + " = ? AND " + COL_USUARIO_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)},
                null,
                null,
                null,
                "1"
        );

        try {
            if (cursor.moveToFirst()) {
                return new Asignatura(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ASIGNATURA_NOMBRE))
                );
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    // -------------------- TAREAS POR USUARIO --------------------

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
        valores.put(COL_USUARIO_ID, usuarioId);
        agregarAsignaturaAValores(valores, tarea.getAsignaturaId());
        return db.insert(TABLA_TAREAS, null, valores);
    }

    public List<Tarea> obtenerTareas(long usuarioId, String criterioOrden) {
        return consultarTareasUsuario(usuarioId, null, null, obtenerOrdenSql(criterioOrden), null);
    }

    public List<Tarea> obtenerTareasPorEstado(String estado, String criterioOrden, long usuarioId) {
        return consultarTareasUsuario(
                usuarioId,
                TABLA_TAREAS + "." + COL_ESTADO + " = ?",
                new String[]{estado},
                obtenerOrdenSql(criterioOrden),
                null
        );
    }

    public List<Tarea> obtenerTareasPorPrioridad(String prioridad, String criterioOrden, long usuarioId) {
        return consultarTareasUsuario(
                usuarioId,
                TABLA_TAREAS + "." + COL_PRIORIDAD + " = ?",
                new String[]{prioridad},
                obtenerOrdenSql(criterioOrden),
                null
        );
    }

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

    public int contarTareasPorEstado(String estado, long usuarioId) {
        if (usuarioId <= 0) {
            return 0;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLA_TAREAS +
                        " WHERE " + COL_ESTADO + " = ? AND " + COL_USUARIO_ID + " = ?",
                new String[]{estado, String.valueOf(usuarioId)}
        );
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    public int contarAsignaturas(long usuarioId) {
        if (usuarioId <= 0) {
            return 0;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLA_ASIGNATURAS +
                        " WHERE " + COL_USUARIO_ID + " = ?",
                new String[]{String.valueOf(usuarioId)}
        );
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

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

    public Tarea obtenerTareaPorId(long id, long usuarioId) {
        List<Tarea> resultado = consultarTareasUsuario(
                usuarioId,
                TABLA_TAREAS + "." + COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                "1"
        );
        return resultado.isEmpty() ? null : resultado.get(0);
    }

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
        agregarAsignaturaAValores(valores, asignaturaId);

        int filasActualizadas = db.update(
                TABLA_TAREAS,
                valores,
                COL_ID + " = ? AND " + COL_USUARIO_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)}
        );
        return filasActualizadas > 0;
    }

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
                    COL_SESION_TAREA_ID + " IN (SELECT " + COL_ID +
                            " FROM " + TABLA_TAREAS +
                            " WHERE " + COL_ID + " = ? AND " + COL_USUARIO_ID + " = ?)",
                    argumentos
            );

            int filasEliminadas = db.delete(
                    TABLA_TAREAS,
                    COL_ID + " = ? AND " + COL_USUARIO_ID + " = ?",
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

    public List<SesionEnfoque> obtenerSesionesPorTarea(long tareaId) {
        List<SesionEnfoque> sesiones = new ArrayList<>();
        if (tareaId <= 0) {
            return sesiones;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_SESIONES_ENFOQUE,
                new String[]{
                        COL_ID,
                        COL_SESION_TAREA_ID,
                        COL_SESION_FECHA,
                        COL_SESION_DURACION_SEGUNDOS
                },
                COL_SESION_TAREA_ID + " = ?",
                new String[]{String.valueOf(tareaId)},
                null,
                null,
                COL_ID + " DESC"
        );

        try {
            int indiceId = cursor.getColumnIndexOrThrow(COL_ID);
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
                COL_ID + " = ? AND " + COL_USUARIO_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(usuarioId)}
        );
        return filasActualizadas > 0;
    }

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
        seleccion.append(TABLA_TAREAS).append(".").append(COL_USUARIO_ID).append(" = ?");

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
                .append(" = ").append(TABLA_ASIGNATURAS).append(".").append(COL_ID)
                .append(" AND ").append(TABLA_ASIGNATURAS).append(".").append(COL_USUARIO_ID)
                .append(" = ").append(TABLA_TAREAS).append(".").append(COL_USUARIO_ID)
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
        String id = TABLA_TAREAS + "." + COL_ID;

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

    private void agregarAsignaturaAValores(ContentValues valores, long asignaturaId) {
        if (asignaturaId > 0) {
            valores.put(COL_ASIGNATURA_ID, asignaturaId);
        } else {
            valores.putNull(COL_ASIGNATURA_ID);
        }
    }

    private Tarea crearTareaDesdeCursor(Cursor cursor) {
        int indiceId = cursor.getColumnIndexOrThrow(COL_ID);
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
