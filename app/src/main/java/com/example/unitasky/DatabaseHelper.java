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
    private static final int VERSION_BD = 2;

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

    public static final String ORDEN_RECIENTES = "recientes";
    public static final String ORDEN_FECHA = "fecha";
    public static final String ORDEN_PRIORIDAD = "prioridad";

    public DatabaseHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        crearTablaAsignaturas(db);
        crearTablaTareas(db);
    }

    private void crearTablaAsignaturas(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLA_ASIGNATURAS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ASIGNATURA_NOMBRE + " TEXT NOT NULL" +
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
                COL_ASIGNATURA_ID + " INTEGER" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            crearTablaAsignaturas(db);
            db.execSQL("ALTER TABLE " + TABLA_TAREAS +
                    " ADD COLUMN " + COL_ASIGNATURA_ID + " INTEGER");
        }
    }

    public long insertarAsignatura(Asignatura asignatura) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_ASIGNATURA_NOMBRE, asignatura.getNombre().trim());
        return db.insert(TABLA_ASIGNATURAS, null, valores);
    }

    public boolean existeAsignatura(String nombre) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_ID + " FROM " + TABLA_ASIGNATURAS +
                        " WHERE LOWER(TRIM(" + COL_ASIGNATURA_NOMBRE + ")) = LOWER(TRIM(?)) LIMIT 1",
                new String[]{nombre}
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public List<Asignatura> obtenerAsignaturas() {
        List<Asignatura> asignaturas = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_ASIGNATURAS,
                null,
                null,
                null,
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

    public Asignatura obtenerAsignaturaPorId(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLA_ASIGNATURAS,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
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

    public long insertarTarea(Tarea tarea) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_TITULO, tarea.getTitulo());
        valores.put(COL_DESCRIPCION, tarea.getDescripcion());
        valores.put(COL_TIPO, tarea.getTipo());
        valores.put(COL_FECHA, tarea.getFecha());
        valores.put(COL_PRIORIDAD, tarea.getPrioridad());
        valores.put(COL_ESTADO, tarea.getEstado());
        agregarAsignaturaAValores(valores, tarea.getAsignaturaId());

        return db.insert(TABLA_TAREAS, null, valores);
    }

    public List<Tarea> obtenerTareas() {
        return obtenerTareas(ORDEN_RECIENTES);
    }

    public List<Tarea> obtenerTareas(String criterioOrden) {
        return consultarTareas(null, null, obtenerOrdenSql(criterioOrden));
    }

    public List<Tarea> obtenerTareasPorEstado(String estado, String criterioOrden) {
        return consultarTareas(
                TABLA_TAREAS + "." + COL_ESTADO + " = ?",
                new String[]{estado},
                obtenerOrdenSql(criterioOrden)
        );
    }

    public List<Tarea> obtenerTareasPorPrioridad(String prioridad, String criterioOrden) {
        return consultarTareas(
                TABLA_TAREAS + "." + COL_PRIORIDAD + " = ?",
                new String[]{prioridad},
                obtenerOrdenSql(criterioOrden)
        );
    }

    public List<Tarea> obtenerProximasEntregas(String estadoPendiente) {
        String seleccion = TABLA_TAREAS + "." + COL_ESTADO + " = ? AND " +
                TABLA_TAREAS + "." + COL_FECHA + " IS NOT NULL AND TRIM(" +
                TABLA_TAREAS + "." + COL_FECHA + ") <> ''";

        return consultarTareas(
                seleccion,
                new String[]{estadoPendiente},
                obtenerOrdenSql(ORDEN_FECHA)
        );
    }

    private List<Tarea> consultarTareas(String seleccion, String[] argumentos, String orden) {
        List<Tarea> tareas = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append(TABLA_TAREAS).append(".*, ")
                .append(TABLA_ASIGNATURAS).append(".").append(COL_ASIGNATURA_NOMBRE)
                .append(" AS ").append(ALIAS_ASIGNATURA_NOMBRE)
                .append(" FROM ").append(TABLA_TAREAS)
                .append(" LEFT JOIN ").append(TABLA_ASIGNATURAS)
                .append(" ON ").append(TABLA_TAREAS).append(".").append(COL_ASIGNATURA_ID)
                .append(" = ").append(TABLA_ASIGNATURAS).append(".").append(COL_ID);

        if (seleccion != null && !seleccion.trim().isEmpty()) {
            sql.append(" WHERE ").append(seleccion);
        }

        if (orden != null && !orden.trim().isEmpty()) {
            sql.append(" ORDER BY ").append(orden);
        }

        Cursor cursor = db.rawQuery(sql.toString(), argumentos);
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

    public Tarea obtenerTareaPorId(long id) {
        List<Tarea> resultado = consultarTareas(
                TABLA_TAREAS + "." + COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null
        );
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean actualizarTarea(long id, String titulo, String descripcion,
                                   String tipo, String fecha, String prioridad,
                                   long asignaturaId) {
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
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return filasActualizadas > 0;
    }

    private void agregarAsignaturaAValores(ContentValues valores, long asignaturaId) {
        if (asignaturaId > 0) {
            valores.put(COL_ASIGNATURA_ID, asignaturaId);
        } else {
            valores.putNull(COL_ASIGNATURA_ID);
        }
    }

    public boolean eliminarTarea(long id) {
        SQLiteDatabase db = getWritableDatabase();

        int filasEliminadas = db.delete(
                TABLA_TAREAS,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return filasEliminadas > 0;
    }

    public boolean actualizarEstado(long id, String nuevoEstado) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_ESTADO, nuevoEstado);

        int filasActualizadas = db.update(
                TABLA_TAREAS,
                valores,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return filasActualizadas > 0;
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
