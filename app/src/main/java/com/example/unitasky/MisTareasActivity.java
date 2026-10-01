package com.example.unitasky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MisTareasActivity extends Activity {

    private LinearLayout contenedorTareas;
    private TextView txtSinTareas;
    private TareaRepository tareaRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tareas);

        contenedorTareas = findViewById(R.id.contenedorTareas);
        txtSinTareas = findViewById(R.id.txtSinTareas);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnAgregarTarea = findViewById(R.id.btnAgregarDesdeLista);

        tareaRepository = new TareaRepository(this);

        btnVolver.setOnClickListener(v -> finish());
        mostrarTareas();

        btnAgregarTarea.setOnClickListener(v -> {
            Intent intent = new Intent(MisTareasActivity.this, AgregarTareaActivity.class);
            startActivity(intent);
        });
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
        List<Tarea> tareas = tareaRepository.obtenerTareas();

        if (tareas.isEmpty()) {
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
                        txtEstado.setText(getString(R.string.formato_estado, estadoCompletada));
                        btnCompletar.setVisibility(View.GONE);
                        FeedbackHelper.mostrarExito(
                                MisTareasActivity.this,
                                getString(R.string.toast_tarea_completada)
                        );
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
