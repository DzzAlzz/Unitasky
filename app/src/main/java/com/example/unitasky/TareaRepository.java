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

    public List<Tarea> obtenerTareas(String criterioOrden) {
        return databaseHelper.obtenerTareas(criterioOrden);
    }

    public List<Tarea> obtenerTareasPendientes(String criterioOrden) {
        return databaseHelper.obtenerTareasPorEstado("Pendiente", criterioOrden);
    }

    public List<Tarea> obtenerTareasCompletadas(String criterioOrden) {
        return databaseHelper.obtenerTareasPorEstado("Completada", criterioOrden);
    }

    public List<Tarea> obtenerTareasPorPrioridad(String prioridad, String criterioOrden) {
        return databaseHelper.obtenerTareasPorPrioridad(prioridad, criterioOrden);
    }

    public List<Tarea> obtenerTareasFiltradas(String textoBusqueda,
                                               String estado,
                                               String prioridad,
                                               long asignaturaId,
                                               boolean soloSinAsignatura,
                                               String criterioOrden) {
        return databaseHelper.obtenerTareasFiltradas(
                textoBusqueda,
                estado,
                prioridad,
                asignaturaId,
                soloSinAsignatura,
                criterioOrden
        );
    }

    public List<Tarea> obtenerProximasEntregas() {
        return databaseHelper.obtenerProximasEntregas("Pendiente");
    }

    public int contarTareasPendientes() {
        return databaseHelper.contarTareasPorEstado("Pendiente");
    }

    public int contarTareasCompletadas() {
        return databaseHelper.contarTareasPorEstado("Completada");
    }

    public int contarAsignaturas() {
        return databaseHelper.contarAsignaturas();
    }

    public Tarea obtenerProximaEntrega() {
        return databaseHelper.obtenerProximaEntrega("Pendiente");
    }

    public Tarea obtenerTareaPorId(long id) {
        return databaseHelper.obtenerTareaPorId(id);
    }

    public boolean actualizarTarea(long id, String titulo, String descripcion,
                                   String tipo, String fecha, String prioridad,
                                   long asignaturaId) {
        return databaseHelper.actualizarTarea(
                id,
                titulo,
                descripcion,
                tipo,
                fecha,
                prioridad,
                asignaturaId
        );
    }

    public boolean eliminarTarea(long id) {
        return databaseHelper.eliminarTarea(id);
    }

    public boolean marcarComoCompletada(Tarea tarea, String estadoCompletada) {
        boolean actualizado = databaseHelper.actualizarEstado(tarea.getId(), estadoCompletada);
        if (actualizado) {
            tarea.setEstado(estadoCompletada);
        }
        return actualizado;
    }

    public boolean agregarAsignatura(Asignatura asignatura) {
        long idGenerado = databaseHelper.insertarAsignatura(asignatura);
        if (idGenerado == -1) {
            return false;
        }
        asignatura.setId(idGenerado);
        return true;
    }

    public boolean existeAsignatura(String nombre) {
        return databaseHelper.existeAsignatura(nombre);
    }

    public boolean existeAsignatura(String nombre, long idExcluir) {
        return databaseHelper.existeAsignatura(nombre, idExcluir);
    }

    public boolean actualizarAsignatura(long id, String nombre) {
        return databaseHelper.actualizarAsignatura(id, nombre);
    }

    public boolean eliminarAsignatura(long id) {
        return databaseHelper.eliminarAsignatura(id);
    }

    public List<Asignatura> obtenerAsignaturas() {
        return databaseHelper.obtenerAsignaturas();
    }

    public Asignatura obtenerAsignaturaPorId(long id) {
        return databaseHelper.obtenerAsignaturaPorId(id);
    }

    public void cerrar() {
        databaseHelper.close();
    }
}
