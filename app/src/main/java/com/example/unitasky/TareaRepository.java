package com.example.unitasky;

import android.content.Context;

import java.util.List;

public class TareaRepository {

    private final DatabaseHelper databaseHelper;

    public TareaRepository(Context context) {
        databaseHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public boolean agregarTarea(Tarea tarea) {
        long idGenerado = databaseHelper.insertarTarea(tarea);
        if (idGenerado == -1) {
            return false;
        }

        tarea.setId(idGenerado);
        return true;
    }

    public List<Tarea> obtenerTareas() {
        return databaseHelper.obtenerTareas();
    }

    public boolean marcarComoCompletada(Tarea tarea, String estadoCompletada) {
        boolean actualizado = databaseHelper.actualizarEstado(tarea.getId(), estadoCompletada);
        if (actualizado) {
            tarea.setEstado(estadoCompletada);
        }
        return actualizado;
    }

    public void cerrar() {
        databaseHelper.close();
    }
}
