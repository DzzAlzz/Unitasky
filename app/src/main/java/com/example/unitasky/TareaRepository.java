package com.example.unitasky;

import java.util.ArrayList;
import java.util.List;

public class TareaRepository {

    private static final ArrayList<Tarea> tareas = new ArrayList<>();

    private TareaRepository() {
        // Evita crear instancias de esta clase de apoyo.
    }

    public static void inicializarEjemplos() {
        if (!tareas.isEmpty()) {
            return;
        }

        tareas.add(new Tarea(
                "Informe de Arquitectura",
                "Preparar el informe solicitado para la asignatura.",
                "23/09/2026",
                "Trabajo",
                "Alta",
                "Pendiente"
        ));

        tareas.add(new Tarea(
                "Actividad de Inglés",
                "Completar la actividad y revisar las respuestas.",
                "25/09/2026",
                "Tarea",
                "Media",
                "Pendiente"
        ));
    }

    public static void agregarTarea(Tarea tarea) {
        tareas.add(tarea);
    }

    public static List<Tarea> obtenerTareas() {
        return tareas;
    }
}
