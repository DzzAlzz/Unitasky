package com.example.unitasky.repository;

import android.content.Context;

import com.example.unitasky.database.DatabaseHelper;
import com.example.unitasky.model.Asignatura;
import com.example.unitasky.model.SesionEnfoque;
import com.example.unitasky.model.Tarea;
import com.example.unitasky.model.Usuario;
import com.example.unitasky.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class TareaRepository {

    private final DatabaseHelper databaseHelper;
    private final SessionManager sessionManager;
    private final ExecutorService executorService;
    private volatile boolean cerrado = false;

    public interface ResultadoCallback<T> {
        void onResultado(T resultado);
    }

    public TareaRepository(Context context) {
        Context appContext = context.getApplicationContext();
        databaseHelper = new DatabaseHelper(appContext);
        sessionManager = new SessionManager(appContext);
        executorService = Executors.newSingleThreadExecutor();
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

    public boolean guardarSesionEnfoque(long tareaId, String fecha, int duracionSegundos) {
        if (tareaId <= 0 || duracionSegundos <= 0) {
            return false;
        }

        // La tarea debe pertenecer al usuario de la sesión actual.
        Tarea tarea = databaseHelper.obtenerTareaPorId(tareaId, getUsuarioId());
        if (tarea == null) {
            return false;
        }

        return databaseHelper.insertarSesionEnfoque(
                tareaId,
                fecha,
                duracionSegundos
        ) != -1;
    }

    public List<SesionEnfoque> obtenerSesionesEnfoque(long tareaId) {
        if (tareaId <= 0) {
            return new ArrayList<>();
        }

        // Valida que la tarea consultada pertenezca al usuario con sesión iniciada.
        Tarea tarea = databaseHelper.obtenerTareaPorId(tareaId, getUsuarioId());
        if (tarea == null) {
            return new ArrayList<>();
        }

        return databaseHelper.obtenerSesionesPorTarea(tareaId);
    }

    public int obtenerTiempoTotalEnfoque(long tareaId) {
        if (tareaId <= 0) {
            return 0;
        }

        // Mantiene la misma separación por usuario utilizada por el historial.
        Tarea tarea = databaseHelper.obtenerTareaPorId(tareaId, getUsuarioId());
        if (tarea == null) {
            return 0;
        }

        return databaseHelper.obtenerTiempoTotalEnfoque(tareaId);
    }

    public int obtenerTiempoTotalEnfoqueUsuario() {
        long usuarioId = getUsuarioId();
        if (usuarioId <= 0) {
            return 0;
        }
        return databaseHelper.obtenerTiempoTotalEnfoqueUsuario(usuarioId);
    }

    public SesionEnfoque obtenerUltimaSesionEnfoqueUsuario() {
        long usuarioId = getUsuarioId();
        if (usuarioId <= 0) {
            return null;
        }
        return databaseHelper.obtenerUltimaSesionEnfoqueUsuario(usuarioId);
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



    public boolean prepararBaseDatos() {
        databaseHelper.getWritableDatabase();
        return true;
    }

    public Usuario obtenerUsuarioPorId(long id) {
        if (id <= 0) {
            return null;
        }
        return databaseHelper.obtenerUsuarioPorId(id);
    }

    public Usuario validarUsuario(String correo, String password) {
        return databaseHelper.validarUsuario(correo, password);
    }

    public boolean existeCorreo(String correo) {
        return databaseHelper.existeCorreo(correo);
    }

    public long insertarUsuario(String nombre, String correo, String password) {
        return databaseHelper.insertarUsuario(nombre, correo, password);
    }

    private <T> void ejecutarAsync(Callable<T> operacion, T valorError, ResultadoCallback<T> callback) {
        if (cerrado) {
            return;
        }

        try {
            executorService.execute(() -> {
                T resultado = valorError;
                try {
                    resultado = operacion.call();
                } catch (Exception ignored) {
                    // Si ocurre un error, se devuelve el valor seguro definido por cada operación.
                }

                if (!cerrado && callback != null) {
                    callback.onResultado(resultado);
                }
            });
        } catch (RejectedExecutionException ignored) {
            // El repositorio ya se está cerrando y no acepta nuevas tareas.
        }
    }

    public void agregarTareaAsync(Tarea tarea, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> agregarTarea(tarea), false, callback);
    }

    public void obtenerTareasAsync(ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(this::obtenerTareas, new ArrayList<>(), callback);
    }

    public void obtenerTareasAsync(String criterioOrden, ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(() -> obtenerTareas(criterioOrden), new ArrayList<>(), callback);
    }

    public void obtenerTareasPendientesAsync(String criterioOrden, ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(() -> obtenerTareasPendientes(criterioOrden), new ArrayList<>(), callback);
    }

    public void obtenerTareasCompletadasAsync(String criterioOrden, ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(() -> obtenerTareasCompletadas(criterioOrden), new ArrayList<>(), callback);
    }

    public void obtenerTareasPorPrioridadAsync(String prioridad, String criterioOrden,
                                                ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(() -> obtenerTareasPorPrioridad(prioridad, criterioOrden),
                new ArrayList<>(), callback);
    }

    public void obtenerTareasFiltradasAsync(String textoBusqueda,
                                             String estado,
                                             String prioridad,
                                             long asignaturaId,
                                             boolean soloSinAsignatura,
                                             String criterioOrden,
                                             ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(() -> obtenerTareasFiltradas(
                textoBusqueda,
                estado,
                prioridad,
                asignaturaId,
                soloSinAsignatura,
                criterioOrden
        ), new ArrayList<>(), callback);
    }

    public void obtenerProximasEntregasAsync(ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(this::obtenerProximasEntregas, new ArrayList<>(), callback);
    }

    public void contarTareasPendientesAsync(ResultadoCallback<Integer> callback) {
        ejecutarAsync(this::contarTareasPendientes, 0, callback);
    }

    public void contarTareasCompletadasAsync(ResultadoCallback<Integer> callback) {
        ejecutarAsync(this::contarTareasCompletadas, 0, callback);
    }

    public void contarAsignaturasAsync(ResultadoCallback<Integer> callback) {
        ejecutarAsync(this::contarAsignaturas, 0, callback);
    }

    public void obtenerProximaEntregaAsync(ResultadoCallback<Tarea> callback) {
        ejecutarAsync(this::obtenerProximaEntrega, null, callback);
    }

    public void obtenerTareaPorIdAsync(long id, ResultadoCallback<Tarea> callback) {
        ejecutarAsync(() -> obtenerTareaPorId(id), null, callback);
    }

    public void actualizarTareaAsync(long id, String titulo, String descripcion,
                                     String tipo, String fecha, String prioridad,
                                     long asignaturaId, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> actualizarTarea(
                id, titulo, descripcion, tipo, fecha, prioridad, asignaturaId
        ), false, callback);
    }

    public void eliminarTareaAsync(long id, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> eliminarTarea(id), false, callback);
    }

    public void marcarComoCompletadaAsync(Tarea tarea, String estadoCompletada,
                                           ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> marcarComoCompletada(tarea, estadoCompletada), false, callback);
    }

    public void guardarSesionEnfoqueAsync(long tareaId, String fecha, int duracionSegundos,
                                           ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> guardarSesionEnfoque(tareaId, fecha, duracionSegundos),
                false, callback);
    }

    public void obtenerSesionesEnfoqueAsync(long tareaId,
                                             ResultadoCallback<List<SesionEnfoque>> callback) {
        ejecutarAsync(() -> obtenerSesionesEnfoque(tareaId), new ArrayList<>(), callback);
    }

    public void obtenerTiempoTotalEnfoqueAsync(long tareaId, ResultadoCallback<Integer> callback) {
        ejecutarAsync(() -> obtenerTiempoTotalEnfoque(tareaId), 0, callback);
    }

    public void obtenerTiempoTotalEnfoqueUsuarioAsync(ResultadoCallback<Integer> callback) {
        ejecutarAsync(this::obtenerTiempoTotalEnfoqueUsuario, 0, callback);
    }

    public void obtenerUltimaSesionEnfoqueUsuarioAsync(ResultadoCallback<SesionEnfoque> callback) {
        ejecutarAsync(this::obtenerUltimaSesionEnfoqueUsuario, null, callback);
    }

    public void obtenerAsignaturasAsync(ResultadoCallback<List<Asignatura>> callback) {
        ejecutarAsync(this::obtenerAsignaturas, new ArrayList<>(), callback);
    }


    public void prepararBaseDatosAsync(ResultadoCallback<Boolean> callback) {
        ejecutarAsync(this::prepararBaseDatos, false, callback);
    }

    public void obtenerUsuarioPorIdAsync(long id, ResultadoCallback<Usuario> callback) {
        ejecutarAsync(() -> obtenerUsuarioPorId(id), null, callback);
    }

    public void validarUsuarioAsync(String correo, String password, ResultadoCallback<Usuario> callback) {
        ejecutarAsync(() -> validarUsuario(correo, password), null, callback);
    }

    public void existeCorreoAsync(String correo, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> existeCorreo(correo), false, callback);
    }

    public void insertarUsuarioAsync(String nombre, String correo, String password,
                                     ResultadoCallback<Long> callback) {
        ejecutarAsync(() -> insertarUsuario(nombre, correo, password), -1L, callback);
    }

    public void agregarAsignaturaAsync(Asignatura asignatura, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> agregarAsignatura(asignatura), false, callback);
    }

    public void existeAsignaturaAsync(String nombre, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> existeAsignatura(nombre), false, callback);
    }

    public void existeAsignaturaAsync(String nombre, long idExcluir,
                                      ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> existeAsignatura(nombre, idExcluir), false, callback);
    }

    public void actualizarAsignaturaAsync(long id, String nombre, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> actualizarAsignatura(id, nombre), false, callback);
    }

    public void eliminarAsignaturaAsync(long id, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> eliminarAsignatura(id), false, callback);
    }

    public void obtenerAsignaturaPorIdAsync(long id, ResultadoCallback<Asignatura> callback) {
        ejecutarAsync(() -> obtenerAsignaturaPorId(id), null, callback);
    }

    public void cerrar() {
        if (cerrado) {
            return;
        }

        cerrado = true;
        try {
            executorService.execute(databaseHelper::close);
        } catch (RejectedExecutionException ignored) {
            databaseHelper.close();
        }
        executorService.shutdown();
    }
}
