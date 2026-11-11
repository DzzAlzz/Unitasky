package com.example.unitasky.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.unitasky.model.Usuario;

/**
 * Administra la sesión local de UniTasky mediante SharedPreferences.
 * Solo conserva el estado de sesión y el identificador del usuario.
 */
public class SessionManager {

    private static final String PREFS = "unitasky_sesion";
    private static final String KEY_SESION = "sesion_iniciada";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_CORREO_ANTERIOR = "correo_usuario";
    private static final String KEY_NOMBRE_ANTERIOR = "nombre_usuario";

    private final SharedPreferences preferences;

    /**
     * Crea el administrador de sesión usando el contexto de la aplicación.
     *
     * @param context contexto utilizado para acceder a SharedPreferences.
     */
    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Limpia datos de sesión de versiones anteriores que ya no son necesarios.
        preferences.edit()
                .remove(KEY_CORREO_ANTERIOR)
                .remove(KEY_NOMBRE_ANTERIOR)
                .apply();
    }

    /**
     * Guarda una sesión válida para el usuario recibido.
     *
     * @param usuario usuario autenticado que inicia la sesión.
     */
    public void iniciarSesion(Usuario usuario) {
        if (usuario == null || usuario.getId() <= 0) {
            return;
        }

        preferences.edit()
                .putBoolean(KEY_SESION, true)
                .putLong(KEY_USUARIO_ID, usuario.getId())
                .apply();
    }

    /**
     * Indica si existe una sesión local con un identificador de usuario válido.
     *
     * @return true si existe una sesión iniciada; false en caso contrario.
     */
    public boolean haySesionIniciada() {
        return preferences.getBoolean(KEY_SESION, false) && getUsuarioId() > 0;
    }

    /**
     * Obtiene el identificador del usuario guardado en la sesión.
     *
     * @return ID del usuario o 0 cuando no existe una sesión válida.
     */
    public long getUsuarioId() {
        return preferences.getLong(KEY_USUARIO_ID, 0);
    }

    /** Elimina todos los datos de la sesión local. */
    public void cerrarSesion() {
        preferences.edit().clear().apply();
    }
}
