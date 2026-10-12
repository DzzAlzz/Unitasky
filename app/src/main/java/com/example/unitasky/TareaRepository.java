package com.example.unitasky;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class TareaRepository {

    private final DatabaseHelper databaseHelper;
    private final SessionManager sessionManager;

    public TareaRepository(Context context) {
        Context appContext = context.getApplicationContext();
        databaseHelper = new DatabaseHelper(appContext);
        sessionManager = new SessionManager(appContext);
    }

    private long getUsuarioId() {
        return sessionManager.getUsuarioId();
    }

    public boolean hayUsuarioValido() {
        long usuarioId = getUsuarioId();
        return sessionManager.haySesionIniciada()
                && usuarioId > 0
                && databaseHelper.obtenerUsuarioPorId(usuarioId) != null;
    }

    public boolean agregarTarea(Tarea tarea) {
        long idGenerado = databaseHelper.insertarTarea(tarea, getUsuarioId());
        if (idGenerado == -1) {
            return false;
        }
        tarea.setId(idGenerado);
        return true;
    }

    public List<Tarea> obtenerTareas() {
        return databaseHelper.obtenerTareas(getUsuarioId(), DatabaseHelper.ORDEN_RECIENTES);
    }

    public List<Tarea> obtenerTareas(String criterioOrden) {
        return databaseHelper.obtenerTareas(getUsuarioId(), criterioOrden);
    }

    public List<Tarea> obtenerTareasPendientes(String criterioOrden) {
        return databaseHelper.obtenerTareasPorEstado("Pendiente", criterioOrden, getUsuarioId());
    }

    public List<Tarea> obtenerTareasCompletadas(String criterioOrden) {
        return databaseHelper.obtenerTareasPorEstado("Completada", criterioOrden, getUsuarioId());
    }

    public List<Tarea> obtenerTareasPorPrioridad(String prioridad, String criterioOrden) {
        return databaseHelper.obtenerTareasPorPrioridad(prioridad, criterioOrden, getUsuarioId());
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
                criterioOrden,
                getUsuarioId()
        );
    }

    public List<Tarea> obtenerProximasEntregas() {
        return databaseHelper.obtenerProximasEntregas("Pendiente", getUsuarioId());
    }

    public int contarTareasPendientes() {
        return databaseHelper.contarTareasPorEstado("Pendiente", getUsuarioId());
    }

    public int contarTareasCompletadas() {
        return databaseHelper.contarTareasPorEstado("Completada", getUsuarioId());
    }

    public int contarAsignaturas() {
        return databaseHelper.contarAsignaturas(getUsuarioId());
    }

    public Tarea obtenerProximaEntrega() {
        return databaseHelper.obtenerProximaEntrega("Pendiente", getUsuarioId());
    }

    public Tarea obtenerTareaPorId(long id) {
        return databaseHelper.obtenerTareaPorId(id, getUsuarioId());
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
                asignaturaId,
                getUsuarioId()
        );
    }

    public boolean eliminarTarea(long id) {
        return databaseHelper.eliminarTarea(id, getUsuarioId());
    }

    public boolean marcarComoCompletada(Tarea tarea, String estadoCompletada) {
        boolean actualizado = databaseHelper.actualizarEstado(
                tarea.getId(),
                estadoCompletada,
                getUsuarioId()
        );
        if (actualizado) {
            tarea.setEstado(estadoCompletada);
        }
        return actualizado;
    }

    public boolean agregarAsignatura(Asignatura asignatura) {
        long idGenerado = databaseHelper.insertarAsignatura(asignatura, getUsuarioId());
        if (idGenerado == -1) {
            return false;
        }
        asignatura.setId(idGenerado);
        return true;
    }

    public boolean existeAsignatura(String nombre) {
        return databaseHelper.existeAsignatura(nombre, 0, getUsuarioId());
    }

    public boolean existeAsignatura(String nombre, long idExcluir) {
        return databaseHelper.existeAsignatura(nombre, idExcluir, getUsuarioId());
    }

    public boolean actualizarAsignatura(long id, String nombre) {
        return databaseHelper.actualizarAsignatura(id, nombre, getUsuarioId());
    }

    public boolean eliminarAsignatura(long id) {
        return databaseHelper.eliminarAsignatura(id, getUsuarioId());
    }

    public List<Asignatura> obtenerAsignaturas() {
        if (getUsuarioId() <= 0) {
            return new ArrayList<>();
        }
        return databaseHelper.obtenerAsignaturas(getUsuarioId());
    }

    public Asignatura obtenerAsignaturaPorId(long id) {
        return databaseHelper.obtenerAsignaturaPorId(id, getUsuarioId());
    }

    public void cerrar() {
        databaseHelper.close();
    }
}
