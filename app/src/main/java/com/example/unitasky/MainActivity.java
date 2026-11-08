package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        // Abrir la base dispara una posible migración del Avance 15 al 16.
        databaseHelper.getWritableDatabase();

        if (sessionManager.haySesionIniciada()) {
            Usuario usuario = databaseHelper.obtenerUsuarioPorId(sessionManager.getUsuarioId());
            if (usuario != null) {
                sessionManager.actualizarDatosUsuario(usuario);
                databaseHelper.close();
                abrirInicio();
                return;
            }
            sessionManager.cerrarSesion();
        }
        databaseHelper.close();

        setContentView(R.layout.activity_main);
        Button btnComenzar = findViewById(R.id.btnComenzar);
        btnComenzar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void abrirInicio() {
        Intent intent = new Intent(MainActivity.this, InicioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
