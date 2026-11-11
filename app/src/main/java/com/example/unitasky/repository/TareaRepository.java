package com.example.unitasky.repository;

import android.content.Context;
import android.util.Log;

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

/**
 * Centraliza el acceso de las Activities a los datos de UniTasky.
 * Utiliza un ExecutorService sencillo para ejecutar las operaciones SQLite en segundo plano.
 */
public class TareaRepository {

    private static final String TAG = "TareaRepository";

    private final DatabaseHelper databaseHelper;
    private final SessionManager sessionManager;
    private final ExecutorService executorService;
    private volatile boolean cerrado = false;

    /**
     * Callback simple utilizado para devolver el resultado de una operación asíncrona.
     *
     * @param <T> tipo de resultado esperado.
     */
    public interface ResultadoCallback<T> {
        /**
         * Recibe el valor obtenido al finalizar la operación.
         *
         * @param resultado resultado devuelto por la consulta o escritura.
         */
        void onResultado(T resultado);
    }

    /**
     * Crea el repositorio usando el contexto de la aplicación.
     *
     * @param context contexto desde el que se accede a base de datos y sesión.
     */
    public TareaRepository(Context context) {
        Context appContext = context.getApplicationContext();
        databaseHelper = new DatabaseHelper(appContext);
        sessionManager = new SessionManager(appContext);
        executorService = Executors.newSingleThreadExecutor();
    }

    private long getUsuarioId() {
        return sessionManager.getUsuarioId();
    }

    /**
     * Comprueba que la sesión actual corresponda a un usuario existente en SQLite.
     *
     * @return true si existe una sesión y el usuario todavía es válido.
     */
    public boolean hayUsuarioValido() {
        long usuarioId = getUsuarioId();
        return sessionManager.haySesionIniciada()
                && usuarioId > 0
                && databaseHelper.obtenerUsuarioPorId(usuarioId) != null;
    }

    /**
     * Guarda una tarea para el usuario con sesión iniciada.
     *
     * @param tarea tarea que se desea guardar.
     * @return true si SQLite generó un ID válido.
     */
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

    /**
     * Obtiene las tareas aplicando los filtros seleccionados por el usuario.
     *
     * @param textoBusqueda texto utilizado para buscar por título.
     * @param estado estado seleccionado o null.
     * @param prioridad prioridad seleccionada o null.
     * @param asignaturaId identificador de la asignatura seleccionada.
     * @param soloSinAsignatura indica si solo se desean tareas sin asignatura.
     * @param criterioOrden criterio utilizado para ordenar la lista.
     * @return tareas que cumplen los filtros.
     */
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

    /**
     * Guarda una sesión de enfoque después de comprobar que la tarea pertenece al usuario actual.
     *
     * @param tareaId identificador de la tarea.
     * @param fecha fecha de la sesión.
     * @param duracionSegundos duración en segundos.
     * @return true si la sesión se guardó correctamente.
     */
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

    /**
     * Obtiene las asignaturas del usuario con sesión iniciada.
     *
     * @return lista de asignaturas o una lista vacía si no existe un usuario válido.
     */
    public List<Asignatura> obtenerAsignaturas() {
        if (getUsuarioId() <= 0) {
            return new ArrayList<>();
        }
        return databaseHelper.obtenerAsignaturas(getUsuarioId());
    }

    public Asignatura obtenerAsignaturaPorId(long id) {
        return databaseHelper.obtenerAsignaturaPorId(id, getUsuarioId());
    }



    /**
     * Abre la base de datos para ejecutar las migraciones pendientes si fueran necesarias.
     *
     * @return true cuando la base se pudo preparar.
     */
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

    /**
     * Ejecuta una operación en el ExecutorService y comunica el resultado mediante un callback.
     *
     * @param operacion operación SQLite que se ejecutará en segundo plano.
     * @param valorError valor seguro que se devuelve si la operación falla.
     * @param callback receptor del resultado de la operación.
     * @param <T> tipo de dato devuelto.
     */
    private <T> void ejecutarAsync(Callable<T> operacion, T valorError, ResultadoCallback<T> callback) {
        if (cerrado) {
            return;
        }

        try {
            executorService.execute(() -> {
                T resultado = valorError;
                try {
                    resultado = operacion.call();
                } catch (Exception e) {
                    // Se devuelve el valor seguro y se registra el fallo sin incluir datos sensibles.
                    Log.e(TAG, "Error al ejecutar una operación SQLite", e);
                }

                if (!cerrado && callback != null) {
                    callback.onResultado(resultado);
                }
            });
        } catch (RejectedExecutionException ignored) {
            // El repositorio ya se está cerrando y no acepta nuevas tareas.
        }
    }

    /**
     * Guarda una tarea en segundo plano.
     *
     * @param tarea tarea que se desea guardar.
     * @param callback receptor del resultado.
     */
    public void agregarTareaAsync(Tarea tarea, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> agregarTarea(tarea), false, callback);
    }

    /**
     * Obtiene las tareas recientes en segundo plano.
     *
     * @param callback receptor de la lista obtenida.
     */
    public void obtenerTareasAsync(ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(this::obtenerTareas, new ArrayList<>(), callback);
    }

    /**
     * Obtiene las tareas en segundo plano aplicando un criterio de orden.
     *
     * @param criterioOrden criterio utilizado para ordenar.
     * @param callback receptor de la lista obtenida.
     */
    public void obtenerTareasAsync(String criterioOrden, ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(() -> obtenerTareas(criterioOrden), new ArrayList<>(), callback);
    }

    /**
     * Obtiene las tareas pendientes en segundo plano.
     *
     * @param criterioOrden criterio utilizado para ordenar.
     * @param callback receptor de la lista obtenida.
     */
    public void obtenerTareasPendientesAsync(String criterioOrden, ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(() -> obtenerTareasPendientes(criterioOrden), new ArrayList<>(), callback);
    }

    /**
     * Obtiene las tareas completadas en segundo plano.
     *
     * @param criterioOrden criterio utilizado para ordenar.
     * @param callback receptor de la lista obtenida.
     */
    public void obtenerTareasCompletadasAsync(String criterioOrden, ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(() -> obtenerTareasCompletadas(criterioOrden), new ArrayList<>(), callback);
    }

    /**
     * Obtiene las tareas filtradas por prioridad en segundo plano.
     *
     * @param prioridad prioridad que se desea consultar.
     * @param criterioOrden criterio de orden.
     * @param callback receptor de la lista obtenida.
     */
    public void obtenerTareasPorPrioridadAsync(String prioridad, String criterioOrden,
                                                ResultadoCallback<List<Tarea>> callback) {
        ejecutarAsync(() -> obtenerTareasPorPrioridad(prioridad, criterioOrden),
                new ArrayList<>(), callback);
    }

    /**
     * Ejecuta la consulta combinada de búsqueda y filtros en segundo plano.
     *
     * @param textoBusqueda texto de búsqueda.
     * @param estado estado seleccionado.
     * @param prioridad prioridad seleccionada.
     * @param asignaturaId ID de asignatura.
     * @param soloSinAsignatura indica si se filtra por tareas sin asignatura.
     * @param criterioOrden criterio de orden.
     * @param callback receptor de la lista obtenida.
     */
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

    /**
     * Obtiene las próximas entregas en segundo plano.
     *
     * @param callback receptor de la lista obtenida.
     */
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

    /**
     * Busca una tarea por ID en segundo plano.
     *
     * @param id identificador de la tarea.
     * @param callback receptor de la tarea encontrada.
     */
    public void obtenerTareaPorIdAsync(long id, ResultadoCallback<Tarea> callback) {
        ejecutarAsync(() -> obtenerTareaPorId(id), null, callback);
    }

    /**
     * Actualiza una tarea en segundo plano.
     *
     * @param id identificador de la tarea.
     * @param titulo nuevo título.
     * @param descripcion nueva descripción.
     * @param tipo tipo de tarea.
     * @param fecha fecha de entrega.
     * @param prioridad prioridad.
     * @param asignaturaId ID de asignatura o 0.
     * @param callback receptor del resultado.
     */
    public void actualizarTareaAsync(long id, String titulo, String descripcion,
                                     String tipo, String fecha, String prioridad,
                                     long asignaturaId, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> actualizarTarea(
                id, titulo, descripcion, tipo, fecha, prioridad, asignaturaId
        ), false, callback);
    }

    /**
     * Elimina una tarea en segundo plano.
     *
     * @param id identificador de la tarea.
     * @param callback receptor del resultado.
     */
    public void eliminarTareaAsync(long id, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> eliminarTarea(id), false, callback);
    }

    public void marcarComoCompletadaAsync(Tarea tarea, String estadoCompletada,
                                           ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> marcarComoCompletada(tarea, estadoCompletada), false, callback);
    }

    /**
     * Guarda una sesión de enfoque en segundo plano.
     *
     * @param tareaId identificador de la tarea.
     * @param fecha fecha de la sesión.
     * @param duracionSegundos duración en segundos.
     * @param callback receptor del resultado.
     */
    public void guardarSesionEnfoqueAsync(long tareaId, String fecha, int duracionSegundos,
                                           ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> guardarSesionEnfoque(tareaId, fecha, duracionSegundos),
                false, callback);
    }

    /**
     * Obtiene el historial de enfoque de una tarea en segundo plano.
     *
     * @param tareaId identificador de la tarea.
     * @param callback receptor del historial.
     */
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

    /**
     * Obtiene las asignaturas en segundo plano.
     *
     * @param callback receptor de la lista obtenida.
     */
    public void obtenerAsignaturasAsync(ResultadoCallback<List<Asignatura>> callback) {
        ejecutarAsync(this::obtenerAsignaturas, new ArrayList<>(), callback);
    }


    /**
     * Prepara la base de datos sin bloquear el hilo de interfaz.
     *
     * @param callback receptor del resultado.
     */
    public void prepararBaseDatosAsync(ResultadoCallback<Boolean> callback) {
        ejecutarAsync(this::prepararBaseDatos, false, callback);
    }

    /**
     * Obtiene un usuario por ID en segundo plano.
     *
     * @param id identificador del usuario.
     * @param callback receptor del usuario encontrado.
     */
    public void obtenerUsuarioPorIdAsync(long id, ResultadoCallback<Usuario> callback) {
        ejecutarAsync(() -> obtenerUsuarioPorId(id), null, callback);
    }

    /**
     * Valida credenciales en segundo plano.
     *
     * @param correo correo ingresado.
     * @param password contraseña ingresada.
     * @param callback receptor del usuario autenticado.
     */
    public void validarUsuarioAsync(String correo, String password, ResultadoCallback<Usuario> callback) {
        ejecutarAsync(() -> validarUsuario(correo, password), null, callback);
    }

    /**
     * Comprueba un correo en segundo plano.
     *
     * @param correo correo que se desea comprobar.
     * @param callback receptor del resultado.
     */
    public void existeCorreoAsync(String correo, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> existeCorreo(correo), false, callback);
    }

    /**
     * Registra un usuario en segundo plano.
     *
     * @param nombre nombre del usuario.
     * @param correo correo del usuario.
     * @param password contraseña ingresada.
     * @param callback receptor del ID generado.
     */
    public void insertarUsuarioAsync(String nombre, String correo, String password,
                                     ResultadoCallback<Long> callback) {
        ejecutarAsync(() -> insertarUsuario(nombre, correo, password), -1L, callback);
    }

    /**
     * Agrega una asignatura en segundo plano.
     *
     * @param asignatura asignatura que se desea guardar.
     * @param callback receptor del resultado.
     */
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

    /**
     * Actualiza una asignatura en segundo plano.
     *
     * @param id identificador de la asignatura.
     * @param nombre nuevo nombre.
     * @param callback receptor del resultado.
     */
    public void actualizarAsignaturaAsync(long id, String nombre, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> actualizarAsignatura(id, nombre), false, callback);
    }

    /**
     * Elimina una asignatura en segundo plano.
     *
     * @param id identificador de la asignatura.
     * @param callback receptor del resultado.
     */
    public void eliminarAsignaturaAsync(long id, ResultadoCallback<Boolean> callback) {
        ejecutarAsync(() -> eliminarAsignatura(id), false, callback);
    }

    /**
     * Obtiene una asignatura por ID en segundo plano.
     *
     * @param id identificador de la asignatura.
     * @param callback receptor de la asignatura encontrada.
     */
    public void obtenerAsignaturaPorIdAsync(long id, ResultadoCallback<Asignatura> callback) {
        ejecutarAsync(() -> obtenerAsignaturaPorId(id), null, callback);
    }

    /**
     * Cierra el repositorio, deja de aceptar nuevas operaciones y libera DatabaseHelper.
     */
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
