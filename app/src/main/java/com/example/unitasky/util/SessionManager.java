package com.example.unitasky.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.unitasky.model.Usuario;

public class SessionManager {

    private static final String PREFS = "unitasky_sesion";
    private static final String KEY_SESION = "sesion_iniciada";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_CORREO_ANTERIOR = "correo_usuario";
    private static final String KEY_NOMBRE_ANTERIOR = "nombre_usuario";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Limpia datos de sesión de versiones anteriores que ya no son necesarios.
        preferences.edit()
                .remove(KEY_CORREO_ANTERIOR)
                .remove(KEY_NOMBRE_ANTERIOR)
                .apply();
    }

    public void iniciarSesion(Usuario usuario) {
        if (usuario == null || usuario.getId() <= 0) {
            return;
        }

        preferences.edit()
                .putBoolean(KEY_SESION, true)
                .putLong(KEY_USUARIO_ID, usuario.getId())
                .apply();
    }

    public boolean haySesionIniciada() {
        return preferences.getBoolean(KEY_SESION, false) && getUsuarioId() > 0;
    }

    public long getUsuarioId() {
        return preferences.getLong(KEY_USUARIO_ID, 0);
    }

    public void cerrarSesion() {
        preferences.edit().clear().apply();
    }
}
