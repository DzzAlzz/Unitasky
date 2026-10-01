package com.example.unitasky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MisTareasActivity extends Activity {

    private LinearLayout contenedorTareas;
    private TextView txtSinTareas;
    private Spinner spinnerFiltro;
    private Spinner spinnerOrden;
    private TareaRepository tareaRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tareas);

        contenedorTareas = findViewById(R.id.contenedorTareas);
        txtSinTareas = findViewById(R.id.txtSinTareas);
        spinnerFiltro = findViewById(R.id.spinnerFiltroTareas);
        spinnerOrden = findViewById(R.id.spinnerOrdenTareas);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnAgregarTarea = findViewById(R.id.btnAgregarDesdeLista);

        tareaRepository = new TareaRepository(this);

        btnVolver.setOnClickListener(v -> finish());
        btnAgregarTarea.setOnClickListener(v -> {
            Intent intent = new Intent(MisTareasActivity.this, AgregarTareaActivity.class);
            startActivity(intent);
        });

        configurarSpinner(spinnerFiltro, R.array.filtros_tareas);
        configurarSpinner(spinnerOrden, R.array.orden_tareas);
        configurarCambiosDeConsulta();

        mostrarTareas();
    }

    private void configurarSpinner(Spinner spinner, int recursoArray) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                recursoArray,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void configurarCambiosDeConsulta() {
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mostrarTareas();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Se mantiene la consulta actual.
            }
        };

        spinnerFiltro.setOnItemSelectedListener(listener);
        spinnerOrden.setOnItemSelectedListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contenedorTareas != null && tareaRepository != null) {
            mostrarTareas();
        }
    }

    private void mostrarTareas() {
        contenedorTareas.removeAllViews();
        List<Tarea> tareas = obtenerTareasSegunSeleccion();

        if (tareas.isEmpty()) {
            txtSinTareas.setText(
                    spinnerFiltro.getSelectedItemPosition() == 0
                            ? R.string.sin_tareas
                            : R.string.sin_tareas_filtro
            );
            txtSinTareas.setVisibility(View.VISIBLE);
            return;
        }

        txtSinTareas.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Tarea tarea : tareas) {
            View item = inflater.inflate(R.layout.item_tarea, contenedorTareas, false);

            TextView txtTitulo = item.findViewById(R.id.txtItemTitulo);
            TextView txtDescripcion = item.findViewById(R.id.txtItemDescripcion);
            TextView txtTipo = item.findViewById(R.id.txtItemTipo);
            TextView txtFecha = item.findViewById(R.id.txtItemFecha);
            TextView txtPrioridad = item.findViewById(R.id.txtItemPrioridad);
            TextView txtEstado = item.findViewById(R.id.txtItemEstado);
            Button btnCompletar = item.findViewById(R.id.btnItemCompletar);
            Button btnEditar = item.findViewById(R.id.btnItemEditar);
            Button btnEliminar = item.findViewById(R.id.btnItemEliminar);

            txtTitulo.setText(tarea.getTitulo());
            txtTipo.setText(getString(R.string.formato_tipo, tarea.getTipo()));
            txtFecha.setText(getString(R.string.formato_fecha, tarea.getFecha()));
            txtPrioridad.setText(getString(R.string.formato_prioridad, tarea.getPrioridad()));
            txtEstado.setText(getString(R.string.formato_estado, tarea.getEstado()));

            if (tarea.getDescripcion() == null || tarea.getDescripcion().isEmpty()) {
                txtDescripcion.setVisibility(View.GONE);
            } else {
                txtDescripcion.setVisibility(View.VISIBLE);
                txtDescripcion.setText(tarea.getDescripcion());
            }

            btnEditar.setOnClickListener(v -> abrirEdicion(tarea));
            btnEliminar.setOnClickListener(v -> confirmarEliminacion(tarea));

            if (getString(R.string.estado_completada).equals(tarea.getEstado())) {
                btnCompletar.setVisibility(View.GONE);
            } else {
                btnCompletar.setVisibility(View.VISIBLE);
                btnCompletar.setOnClickListener(v -> {
                    String estadoCompletada = getString(R.string.estado_completada);
                    if (tareaRepository.marcarComoCompletada(tarea, estadoCompletada)) {
                        FeedbackHelper.mostrarExito(
                                MisTareasActivity.this,
                                getString(R.string.toast_tarea_completada)
                        );
                        mostrarTareas();
                    } else {
                        Toast.makeText(
                                MisTareasActivity.this,
                                R.string.toast_error_actualizar_tarea,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }

            contenedorTareas.addView(item);
        }
    }

    private List<Tarea> obtenerTareasSegunSeleccion() {
        String criterioOrden = obtenerCriterioOrden();

        switch (spinnerFiltro.getSelectedItemPosition()) {
            case 1:
                return tareaRepository.obtenerTareasPendientes(criterioOrden);
            case 2:
                return tareaRepository.obtenerTareasCompletadas(criterioOrden);
            case 3:
                return tareaRepository.obtenerTareasPorPrioridad("Alta", criterioOrden);
            case 4:
                return tareaRepository.obtenerTareasPorPrioridad("Media", criterioOrden);
            case 5:
                return tareaRepository.obtenerTareasPorPrioridad("Baja", criterioOrden);
            default:
                return tareaRepository.obtenerTareas(criterioOrden);
        }
    }

    private String obtenerCriterioOrden() {
        switch (spinnerOrden.getSelectedItemPosition()) {
            case 1:
                return DatabaseHelper.ORDEN_FECHA;
            case 2:
                return DatabaseHelper.ORDEN_PRIORIDAD;
            default:
                return DatabaseHelper.ORDEN_RECIENTES;
        }
    }

    private void abrirEdicion(Tarea tarea) {
        Intent intent = new Intent(MisTareasActivity.this, EditarTareaActivity.class);
        intent.putExtra(EditarTareaActivity.EXTRA_TAREA_ID, tarea.getId());
        startActivity(intent);
    }

    private void confirmarEliminacion(Tarea tarea) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.eliminar_tarea)
                .setMessage(R.string.confirmar_eliminar_tarea)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.eliminar, (dialog, which) -> eliminarTarea(tarea))
                .show();
    }

    private void eliminarTarea(Tarea tarea) {
        if (tareaRepository.eliminarTarea(tarea.getId())) {
            FeedbackHelper.mostrarExito(this, getString(R.string.toast_tarea_eliminada));
            mostrarTareas();
        } else {
            Toast.makeText(this, R.string.toast_error_eliminar_tarea, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        super.onDestroy();
    }
}
