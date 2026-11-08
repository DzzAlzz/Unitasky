package com.example.unitasky.database;

import android.provider.BaseColumns;

// Centraliza los nombres de tablas y columnas utilizados por SQLite en UniTasky.
// La base actual usa "id"; se conserva para no requerir una migración en este avance.
public final class UniTaskyContract {

    private UniTaskyContract() {
        // Clase de contrato: no necesita instancias.
    }

    public static final class UsuarioEntry implements BaseColumns {
        private UsuarioEntry() {}

        public static final String TABLE_NAME = "usuarios";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_CORREO = "correo";
        public static final String COLUMN_PASSWORD = "password";
    }

    public static final class AsignaturaEntry implements BaseColumns {
        private AsignaturaEntry() {}

        public static final String TABLE_NAME = "asignaturas";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_USUARIO_ID = "usuario_id";
    }

    public static final class TareaEntry implements BaseColumns {
        private TareaEntry() {}

        public static final String TABLE_NAME = "tareas";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITULO = "titulo";
        public static final String COLUMN_DESCRIPCION = "descripcion";
        public static final String COLUMN_TIPO = "tipo";
        public static final String COLUMN_FECHA = "fecha";
        public static final String COLUMN_PRIORIDAD = "prioridad";
        public static final String COLUMN_ESTADO = "estado";
        public static final String COLUMN_ASIGNATURA_ID = "asignatura_id";
        public static final String COLUMN_USUARIO_ID = "usuario_id";
    }

    public static final class SesionEnfoqueEntry implements BaseColumns {
        private SesionEnfoqueEntry() {}

        public static final String TABLE_NAME = "sesiones_enfoque";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TAREA_ID = "tarea_id";
        public static final String COLUMN_FECHA = "fecha";
        public static final String COLUMN_DURACION_SEGUNDOS = "duracion_segundos";
    }
}
