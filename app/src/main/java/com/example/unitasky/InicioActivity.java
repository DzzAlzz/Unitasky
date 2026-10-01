package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class InicioActivity extends Activity {

    private TextView txtCantidadPendientes;
    private TextView txtCantidadCompletadas;
    private TextView txtCantidadAsignaturas;
    private TextView txtProximaTitulo;
    private TextView txtProximaAsignatura;
    private TextView txtProximaFecha;
    private TextView txtProximaPrioridad;
    private TareaRepository tareaRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

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

        tareaRepository = new TareaRepository(this);

        btnMisTareas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, MisTareasActivity.class);
            startActivity(intent);
        });

        btnAgregarTarea.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, AgregarTareaActivity.class);
            startActivity(intent);
        });

        btnProximasEntregas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, ProximasEntregasActivity.class);
            startActivity(intent);
        });

        btnMisAsignaturas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, MisAsignaturasActivity.class);
            startActivity(intent);
        });

        cargarResumen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tareaRepository != null) {
            cargarResumen();
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

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        super.onDestroy();
    }
}
