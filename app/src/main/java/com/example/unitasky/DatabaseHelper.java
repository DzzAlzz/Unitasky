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
    private static final int VERSION_BD = 1;

    public static final String TABLA_TAREAS = "tareas";
    public static final String COL_ID = "id";
    public static final String COL_TITULO = "titulo";
    public static final String COL_DESCRIPCION = "descripcion";
    public static final String COL_TIPO = "tipo";
    public static final String COL_FECHA = "fecha";
    public static final String COL_PRIORIDAD = "prioridad";
    public static final String COL_ESTADO = "estado";

    public static final String ORDEN_RECIENTES = "recientes";
    public static final String ORDEN_FECHA = "fecha";
    public static final String ORDEN_PRIORIDAD = "prioridad";

    public DatabaseHelper(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String crearTablaTareas = "CREATE TABLE " + TABLA_TAREAS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITULO + " TEXT NOT NULL, " +
                COL_DESCRIPCION + " TEXT, " +
                COL_TIPO + " TEXT NOT NULL, " +
                COL_FECHA + " TEXT NOT NULL, " +
                COL_PRIORIDAD + " TEXT NOT NULL, " +
                COL_ESTADO + " TEXT NOT NULL" +
                ")";

        db.execSQL(crearTablaTareas);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_TAREAS);
        onCreate(db);
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
                COL_ESTADO + " = ?",
                new String[]{estado},
                obtenerOrdenSql(criterioOrden)
        );
    }

    public List<Tarea> obtenerTareasPorPrioridad(String prioridad, String criterioOrden) {
        return consultarTareas(
                COL_PRIORIDAD + " = ?",
                new String[]{prioridad},
                obtenerOrdenSql(criterioOrden)
        );
    }

    public List<Tarea> obtenerProximasEntregas(String estadoPendiente) {
        String seleccion = COL_ESTADO + " = ? AND " +
                COL_FECHA + " IS NOT NULL AND TRIM(" + COL_FECHA + ") <> ''";

        return consultarTareas(
                seleccion,
                new String[]{estadoPendiente},
                obtenerOrdenSql(ORDEN_FECHA)
        );
    }

    private List<Tarea> consultarTareas(String seleccion, String[] argumentos, String orden) {
        List<Tarea> tareas = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLA_TAREAS,
                null,
                seleccion,
                argumentos,
                null,
                null,
                orden
        );

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
        if (ORDEN_FECHA.equals(criterioOrden)) {
            return "substr(" + COL_FECHA + ", 7, 4) || " +
                    "substr(" + COL_FECHA + ", 4, 2) || " +
                    "substr(" + COL_FECHA + ", 1, 2) ASC, " + COL_ID + " DESC";
        }

        if (ORDEN_PRIORIDAD.equals(criterioOrden)) {
            return "CASE " + COL_PRIORIDAD +
                    " WHEN 'Alta' THEN 1" +
                    " WHEN 'Media' THEN 2" +
                    " WHEN 'Baja' THEN 3" +
                    " ELSE 4 END ASC, " + COL_ID + " DESC";
        }

        return COL_ID + " DESC";
    }

    public Tarea obtenerTareaPorId(long id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLA_TAREAS,
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
                return crearTareaDesdeCursor(cursor);
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    public boolean actualizarTarea(long id, String titulo, String descripcion,
                                   String tipo, String fecha, String prioridad) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(COL_TITULO, titulo);
        valores.put(COL_DESCRIPCION, descripcion);
        valores.put(COL_TIPO, tipo);
        valores.put(COL_FECHA, fecha);
        valores.put(COL_PRIORIDAD, prioridad);

        int filasActualizadas = db.update(
                TABLA_TAREAS,
                valores,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return filasActualizadas > 0;
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

        return new Tarea(
                cursor.getLong(indiceId),
                cursor.getString(indiceTitulo),
                cursor.getString(indiceDescripcion),
                cursor.getString(indiceFecha),
                cursor.getString(indiceTipo),
                cursor.getString(indicePrioridad),
                cursor.getString(indiceEstado)
        );
    }
}
