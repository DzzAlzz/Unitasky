package com.example.unitasky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class InicioActivity extends Activity {

    private TextView txtUsuarioSesion;
    private TextView txtCantidadPendientes;
    private TextView txtCantidadCompletadas;
    private TextView txtCantidadAsignaturas;
    private TextView txtProximaTitulo;
    private TextView txtProximaAsignatura;
    private TextView txtProximaFecha;
    private TextView txtProximaPrioridad;
    private TareaRepository tareaRepository;
    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);

        if (!validarSesionActual()) {
            volverAlLogin();
            return;
        }

        setContentView(R.layout.activity_inicio);

        txtUsuarioSesion = findViewById(R.id.txtUsuarioSesion);
        txtCantidadPendientes = findViewById(R.id.txtCantidadPendientes);
        txtCantidadCompletadas = findViewById(R.id.txtCantidadCompletadas);
        txtCantidadAsignaturas = findViewById(R.id.txtCantidadAsignaturas);
        txtProximaTitulo = findViewById(R.id.txtProximaTitulo);
        txtProximaAsignatura = findViewById(R.id.txtProximaAsignatura);
        txtProximaFecha = findViewById(R.id.txtProximaFecha);
        txtProximaPrioridad = findViewById(R.id.txtProximaPrioridad);

        Button btnMisTareas = findViewById(R.id.btnMisTareas);
        Button btnAgregarTarea = findViewById(R.id.btnAgregarTarea);
        Button btnProximasEntregas = findViewById(R.id.btnProximasEntregas);
        Button btnMisAsignaturas = findViewById(R.id.btnMisAsignaturas);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        tareaRepository = new TareaRepository(this);

        btnMisTareas.setOnClickListener(v ->
                startActivity(new Intent(InicioActivity.this, MisTareasActivity.class)));

        btnAgregarTarea.setOnClickListener(v ->
                startActivity(new Intent(InicioActivity.this, AgregarTareaActivity.class)));

        btnProximasEntregas.setOnClickListener(v ->
                startActivity(new Intent(InicioActivity.this, ProximasEntregasActivity.class)));

        btnMisAsignaturas.setOnClickListener(v ->
                startActivity(new Intent(InicioActivity.this, MisAsignaturasActivity.class)));

        btnCerrarSesion.setOnClickListener(v -> confirmarCierreSesion());

        mostrarUsuarioActual();
        cargarResumen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager == null || databaseHelper == null) {
            return;
        }

        if (!validarSesionActual()) {
            volverAlLogin();
            return;
        }

        if (tareaRepository != null) {
            mostrarUsuarioActual();
            cargarResumen();
        }
    }

    private boolean validarSesionActual() {
        if (!sessionManager.haySesionIniciada()) {
            return false;
        }
        Usuario usuario = databaseHelper.obtenerUsuarioPorId(sessionManager.getUsuarioId());
        if (usuario == null) {
            sessionManager.cerrarSesion();
            return false;
        }
        sessionManager.actualizarDatosUsuario(usuario);
        return true;
    }

    private void mostrarUsuarioActual() {
        String nombre = sessionManager.getNombreUsuario().trim();
        String correo = sessionManager.getCorreoUsuario().trim();

        if (!nombre.isEmpty()) {
            txtUsuarioSesion.setText(getString(R.string.formato_usuario_sesion_nombre, nombre, correo));
        } else {
            txtUsuarioSesion.setText(getString(R.string.formato_usuario_sesion, correo));
        }
    }

    private void cargarResumen() {
        txtCantidadPendientes.setText(String.valueOf(tareaRepository.contarTareasPendientes()));
        txtCantidadCompletadas.setText(String.valueOf(tareaRepository.contarTareasCompletadas()));
        txtCantidadAsignaturas.setText(String.valueOf(tareaRepository.contarAsignaturas()));

        Tarea proximaEntrega = tareaRepository.obtenerProximaEntrega();
        if (proximaEntrega == null) {
            txtProximaTitulo.setText(R.string.sin_entregas_inicio);
            txtProximaAsignatura.setVisibility(View.GONE);
            txtProximaFecha.setVisibility(View.GONE);
            txtProximaPrioridad.setVisibility(View.GONE);
            return;
        }

        txtProximaTitulo.setText(proximaEntrega.getTitulo());
        txtProximaAsignatura.setVisibility(View.VISIBLE);
        txtProximaFecha.setVisibility(View.VISIBLE);
        txtProximaPrioridad.setVisibility(View.VISIBLE);

        String nombreAsignatura = proximaEntrega.getAsignaturaNombre() == null
                || proximaEntrega.getAsignaturaNombre().trim().isEmpty()
                ? getString(R.string.sin_asignatura)
                : proximaEntrega.getAsignaturaNombre();

        txtProximaAsignatura.setText(getString(R.string.formato_asignatura, nombreAsignatura));
        txtProximaFecha.setText(getString(R.string.formato_fecha, proximaEntrega.getFecha()));
        txtProximaPrioridad.setText(getString(R.string.formato_prioridad, proximaEntrega.getPrioridad()));
    }

    private void confirmarCierreSesion() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cerrar_sesion)
                .setMessage(R.string.confirmar_cerrar_sesion)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.cerrar_sesion, (dialog, which) -> {
                    sessionManager.cerrarSesion();
                    volverAlLogin();
                })
                .show();
    }

    private void volverAlLogin() {
        Intent intent = new Intent(InicioActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
