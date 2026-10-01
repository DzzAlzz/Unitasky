package com.example.unitasky;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS = "unitasky_sesion";
    private static final String KEY_SESION = "sesion_iniciada";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_CORREO = "correo_usuario";
    private static final String KEY_NOMBRE = "nombre_usuario";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void iniciarSesion(Usuario usuario) {
        preferences.edit()
                .putBoolean(KEY_SESION, true)
                .putLong(KEY_USUARIO_ID, usuario.getId())
                .putString(KEY_CORREO, usuario.getCorreo())
                .putString(KEY_NOMBRE, usuario.getNombre())
                .apply();
    }

    public boolean haySesionIniciada() {
        return preferences.getBoolean(KEY_SESION, false) && getUsuarioId() > 0;
    }

    public long getUsuarioId() {
        return preferences.getLong(KEY_USUARIO_ID, 0);
    }

    public String getCorreoUsuario() {
        return preferences.getString(KEY_CORREO, "");
    }

    public String getNombreUsuario() {
        return preferences.getString(KEY_NOMBRE, "");
    }

    public void actualizarDatosUsuario(Usuario usuario) {
        if (!haySesionIniciada() || usuario == null || usuario.getId() != getUsuarioId()) {
            return;
        }
        preferences.edit()
                .putString(KEY_CORREO, usuario.getCorreo())
                .putString(KEY_NOMBRE, usuario.getNombre())
                .apply();
    }

    public void cerrarSesion() {
        preferences.edit().clear().apply();
    }
}
