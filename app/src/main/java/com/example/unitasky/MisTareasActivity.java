package com.example.unitasky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MisTareasActivity extends Activity {

    private RecyclerView recyclerTareas;
    private TareaAdapter tareaAdapter;
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

        recyclerTareas = findViewById(R.id.recyclerTareas);
        txtSinTareas = findViewById(R.id.txtSinTareas);
        edtBuscarTarea = findViewById(R.id.edtBuscarTarea);
        spinnerFiltro = findViewById(R.id.spinnerFiltroTareas);
        spinnerAsignatura = findViewById(R.id.spinnerFiltroAsignatura);
        spinnerOrden = findViewById(R.id.spinnerOrdenTareas);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnAgregarTarea = findViewById(R.id.btnAgregarDesdeLista);

        tareaRepository = new TareaRepository(this);
        configurarRecyclerView();

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

    private void configurarRecyclerView() {
        recyclerTareas.setLayoutManager(new LinearLayoutManager(this));
        recyclerTareas.setHasFixedSize(false);

        tareaAdapter = new TareaAdapter(new TareaAdapter.AccionesTareaListener() {
            @Override
            public void onEditar(Tarea tarea) {
                abrirEdicion(tarea);
            }

            @Override
            public void onEliminar(Tarea tarea) {
                confirmarEliminacion(tarea);
            }

            @Override
            public void onCompletar(Tarea tarea) {
                completarTarea(tarea);
            }
        });

        recyclerTareas.setAdapter(tareaAdapter);
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
        if (recyclerTareas != null && tareaRepository != null) {
            long asignaturaSeleccionadaId = obtenerAsignaturaFiltroSeleccionadaId();
            boolean soloSinAsignatura = spinnerAsignatura != null
                    && spinnerAsignatura.getSelectedItemPosition() == 1;

            cargarFiltroAsignaturas();
            restaurarFiltroAsignatura(asignaturaSeleccionadaId, soloSinAsignatura);
            mostrarTareas();
        }
    }

    private long obtenerAsignaturaFiltroSeleccionadaId() {
        if (spinnerAsignatura == null) {
            return 0;
        }

        int posicion = spinnerAsignatura.getSelectedItemPosition();
        if (posicion < 2) {
            return 0;
        }

        int indiceLista = posicion - 2;
        if (indiceLista < 0 || indiceLista >= asignaturas.size()) {
            return 0;
        }
        return asignaturas.get(indiceLista).getId();
    }

    private void restaurarFiltroAsignatura(long asignaturaId, boolean soloSinAsignatura) {
        if (spinnerAsignatura == null) {
            return;
        }

        if (soloSinAsignatura) {
            spinnerAsignatura.setSelection(1);
            return;
        }

        if (asignaturaId > 0) {
            for (int i = 0; i < asignaturas.size(); i++) {
                if (asignaturas.get(i).getId() == asignaturaId) {
                    spinnerAsignatura.setSelection(i + 2);
                    return;
                }
            }
        }

        spinnerAsignatura.setSelection(0);
    }

    private void mostrarTareas() {
        if (tareaAdapter == null || tareaRepository == null || spinnerFiltro == null
                || spinnerAsignatura == null || spinnerOrden == null || edtBuscarTarea == null) {
            return;
        }

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

        String textoBusqueda = edtBuscarTarea.getText().toString();
        String criterioOrden = obtenerCriterioOrden();
        boolean filtrosActivos = hayFiltrosActivos();

        tareaRepository.obtenerTareasFiltradasAsync(
                textoBusqueda,
                estado,
                prioridad,
                asignaturaId,
                soloSinAsignatura,
                criterioOrden,
                tareas -> runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    tareaAdapter.actualizarTareas(tareas);

                    if (tareas.isEmpty()) {
                        txtSinTareas.setText(filtrosActivos
                                ? R.string.no_se_encontraron_tareas
                                : R.string.sin_tareas);
                        txtSinTareas.setVisibility(View.VISIBLE);
                        recyclerTareas.setVisibility(View.GONE);
                    } else {
                        txtSinTareas.setVisibility(View.GONE);
                        recyclerTareas.setVisibility(View.VISIBLE);
                    }
                })
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

    private void completarTarea(Tarea tarea) {
        String estadoCompletada = getString(R.string.estado_completada);
        tareaRepository.marcarComoCompletadaAsync(tarea, estadoCompletada, actualizado ->
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (actualizado) {
                        FeedbackHelper.mostrarExito(
                                this,
                                getString(R.string.toast_tarea_completada)
                        );
                        mostrarTareas();
                    } else {
                        Toast.makeText(
                                this,
                                R.string.toast_error_actualizar_tarea,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
        );
    }

    private void eliminarTarea(Tarea tarea) {
        tareaRepository.eliminarTareaAsync(tarea.getId(), eliminada ->
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (eliminada) {
                        FeedbackHelper.mostrarExito(this, getString(R.string.toast_tarea_eliminada));
                        mostrarTareas();
                    } else {
                        Toast.makeText(
                                this,
                                R.string.toast_error_eliminar_tarea,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
        );
    }

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        super.onDestroy();
    }
}
