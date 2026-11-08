package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends Activity {

    private TareaRepository tareaRepository;
    private SessionManager sessionManager;
    private Button btnComenzar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        tareaRepository = new TareaRepository(this);
        btnComenzar = findViewById(R.id.btnComenzar);

        btnComenzar.setEnabled(false);
        btnComenzar.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class))
        );

        // Abrir/migrar SQLite puede requerir trabajo de disco, por eso se hace fuera de la UI.
        boolean haySesion = sessionManager.haySesionIniciada();
        long usuarioId = sessionManager.getUsuarioId();

        tareaRepository.prepararBaseDatosAsync(preparada -> {
            if (!haySesion || usuarioId <= 0) {
                habilitarComenzar();
                return;
            }

            tareaRepository.obtenerUsuarioPorIdAsync(usuarioId, usuario ->
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (usuario != null) {
                            sessionManager.actualizarDatosUsuario(usuario);
                            abrirInicio();
                            return;
                        }

                        sessionManager.cerrarSesion();
                        btnComenzar.setEnabled(true);
                    })
            );
        });
    }

    private void habilitarComenzar() {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                btnComenzar.setEnabled(true);
            }
        });
    }

    private void abrirInicio() {
        Intent intent = new Intent(MainActivity.this, InicioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        super.onDestroy();
    }
}
