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
        List<Tarea> tareas = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLA_TAREAS,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " DESC"
        );

        try {
            int indiceId = cursor.getColumnIndexOrThrow(COL_ID);
            int indiceTitulo = cursor.getColumnIndexOrThrow(COL_TITULO);
            int indiceDescripcion = cursor.getColumnIndexOrThrow(COL_DESCRIPCION);
            int indiceTipo = cursor.getColumnIndexOrThrow(COL_TIPO);
            int indiceFecha = cursor.getColumnIndexOrThrow(COL_FECHA);
            int indicePrioridad = cursor.getColumnIndexOrThrow(COL_PRIORIDAD);
            int indiceEstado = cursor.getColumnIndexOrThrow(COL_ESTADO);

            while (cursor.moveToNext()) {
                tareas.add(new Tarea(
                        cursor.getLong(indiceId),
                        cursor.getString(indiceTitulo),
                        cursor.getString(indiceDescripcion),
                        cursor.getString(indiceFecha),
                        cursor.getString(indiceTipo),
                        cursor.getString(indicePrioridad),
                        cursor.getString(indiceEstado)
                ));
            }
        } finally {
            cursor.close();
        }

        return tareas;
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
}
