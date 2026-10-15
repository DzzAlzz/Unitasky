package com.example.unitasky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MisTareasActivity extends Activity {

    private LinearLayout contenedorTareas;
    private TextView txtSinTareas;
    private EditText edtBuscarTarea;
    private Spinner spinnerFiltro;
    private Spinner spinnerAsignatura;
    private Spinner spinnerOrden;
    private TareaRepository tareaRepository;
    private List<Asignatura> asignaturas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tareas);

        contenedorTareas = findViewById(R.id.contenedorTareas);
        txtSinTareas = findViewById(R.id.txtSinTareas);
        edtBuscarTarea = findViewById(R.id.edtBuscarTarea);
        spinnerFiltro = findViewById(R.id.spinnerFiltroTareas);
        spinnerAsignatura = findViewById(R.id.spinnerFiltroAsignatura);
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
        cargarFiltroAsignaturas();
        configurarCambiosDeConsulta();
        configurarBusqueda();

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

    private void cargarFiltroAsignaturas() {
        asignaturas = tareaRepository.obtenerAsignaturas();
        List<String> opciones = new ArrayList<>();
        opciones.add(getString(R.string.todas_asignaturas));
        opciones.add(getString(R.string.sin_asignatura));

        for (Asignatura asignatura : asignaturas) {
            opciones.add(asignatura.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opciones
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAsignatura.setAdapter(adapter);
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
        spinnerAsignatura.setOnItemSelectedListener(listener);
        spinnerOrden.setOnItemSelectedListener(listener);
    }

    private void configurarBusqueda() {
        edtBuscarTarea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita acción antes del cambio.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // La consulta se actualiza después del cambio.
            }

            @Override
            public void afterTextChanged(Editable s) {
                mostrarTareas();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contenedorTareas != null && tareaRepository != null) {
            int posicionAnterior = spinnerAsignatura == null ? 0 : spinnerAsignatura.getSelectedItemPosition();
            cargarFiltroAsignaturas();
            if (spinnerAsignatura != null && posicionAnterior >= 0 && posicionAnterior < spinnerAsignatura.getCount()) {
                spinnerAsignatura.setSelection(posicionAnterior);
            }
            mostrarTareas();
        }
    }

    private void mostrarTareas() {
        if (contenedorTareas == null || tareaRepository == null || spinnerFiltro == null ||
                spinnerAsignatura == null || spinnerOrden == null || edtBuscarTarea == null) {
            return;
        }

        contenedorTareas.removeAllViews();
        List<Tarea> tareas = obtenerTareasSegunSeleccion();

        if (tareas.isEmpty()) {
            txtSinTareas.setText(hayFiltrosActivos()
                    ? R.string.no_se_encontraron_tareas
                    : R.string.sin_tareas);
            txtSinTareas.setVisibility(View.VISIBLE);
            return;
        }

        txtSinTareas.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Tarea tarea : tareas) {
            View item = inflater.inflate(R.layout.item_tarea, contenedorTareas, false);

            TextView txtTitulo = item.findViewById(R.id.txtItemTitulo);
            TextView txtDescripcion = item.findViewById(R.id.txtItemDescripcion);
            TextView txtAsignatura = item.findViewById(R.id.txtItemAsignatura);
            TextView txtTipo = item.findViewById(R.id.txtItemTipo);
            TextView txtFecha = item.findViewById(R.id.txtItemFecha);
            TextView txtPrioridad = item.findViewById(R.id.txtItemPrioridad);
            TextView txtEstado = item.findViewById(R.id.txtItemEstado);
            Button btnCompletar = item.findViewById(R.id.btnItemCompletar);
            Button btnEditar = item.findViewById(R.id.btnItemEditar);
            Button btnEliminar = item.findViewById(R.id.btnItemEliminar);

            txtTitulo.setText(tarea.getTitulo());
            String nombreAsignatura = tarea.getAsignaturaNombre() == null || tarea.getAsignaturaNombre().trim().isEmpty()
                    ? getString(R.string.sin_asignatura)
                    : tarea.getAsignaturaNombre();
            txtAsignatura.setText(getString(R.string.formato_asignatura, nombreAsignatura));
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
        String estado = null;
        String prioridad = null;

        switch (spinnerFiltro.getSelectedItemPosition()) {
            case 1:
                estado = getString(R.string.estado_pendiente);
                break;
            case 2:
                estado = getString(R.string.estado_completada);
                break;
            case 3:
                prioridad = "Alta";
                break;
            case 4:
                prioridad = "Media";
                break;
            case 5:
                prioridad = "Baja";
                break;
            default:
                break;
        }

        long asignaturaId = 0;
        boolean soloSinAsignatura = false;
        int posicionAsignatura = spinnerAsignatura.getSelectedItemPosition();

        if (posicionAsignatura == 1) {
            soloSinAsignatura = true;
        } else if (posicionAsignatura >= 2) {
            int indiceLista = posicionAsignatura - 2;
            if (indiceLista >= 0 && indiceLista < asignaturas.size()) {
                asignaturaId = asignaturas.get(indiceLista).getId();
            }
        }

        return tareaRepository.obtenerTareasFiltradas(
                edtBuscarTarea.getText().toString(),
                estado,
                prioridad,
                asignaturaId,
                soloSinAsignatura,
                obtenerCriterioOrden()
        );
    }

    private boolean hayFiltrosActivos() {
        return !edtBuscarTarea.getText().toString().trim().isEmpty()
                || spinnerFiltro.getSelectedItemPosition() != 0
                || spinnerAsignatura.getSelectedItemPosition() != 0;
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
