package com.example.unitasky;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditarTareaActivity extends Activity {

    public static final String EXTRA_TAREA_ID = "tarea_id";

    private EditText edtTitulo;
    private EditText edtDescripcion;
    private EditText edtFecha;
    private Spinner spinnerTipo;
    private Spinner spinnerPrioridad;
    private Spinner spinnerAsignatura;
    private TareaRepository tareaRepository;
    private List<Asignatura> asignaturas = new ArrayList<>();
    private long tareaId = -1;
    private boolean actualizandoFecha = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tarea);

        edtTitulo = findViewById(R.id.edtTituloEditar);
        edtDescripcion = findViewById(R.id.edtDescripcionEditar);
        edtFecha = findViewById(R.id.edtFechaEditar);
        spinnerTipo = findViewById(R.id.spinnerTipoEditar);
        spinnerPrioridad = findViewById(R.id.spinnerPrioridadEditar);
        spinnerAsignatura = findViewById(R.id.spinnerAsignaturaEditar);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnGuardarCambios = findViewById(R.id.btnGuardarCambios);

        tareaRepository = new TareaRepository(this);
        tareaId = getIntent().getLongExtra(EXTRA_TAREA_ID, -1);

        configurarSpinner(spinnerTipo, R.array.tipos_actividad);
        configurarSpinner(spinnerPrioridad, R.array.prioridades);
        cargarAsignaturas();
        configurarFormatoAutomaticoFecha();

        btnVolver.setOnClickListener(v -> finish());
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());

        cargarTarea();
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

    private void cargarAsignaturas() {
        asignaturas = tareaRepository.obtenerAsignaturas();
        List<String> opciones = new ArrayList<>();
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

    private void cargarTarea() {
        if (tareaId == -1) {
            Toast.makeText(this, R.string.toast_tarea_no_encontrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Tarea tarea = tareaRepository.obtenerTareaPorId(tareaId);
        if (tarea == null) {
            Toast.makeText(this, R.string.toast_tarea_no_encontrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtTitulo.setText(tarea.getTitulo());
        edtDescripcion.setText(tarea.getDescripcion());
        edtFecha.setText(tarea.getFecha());
        seleccionarValorSpinner(spinnerTipo, tarea.getTipo());
        seleccionarValorSpinner(spinnerPrioridad, tarea.getPrioridad());
        seleccionarAsignatura(tarea.getAsignaturaId());
    }

    private void seleccionarValorSpinner(Spinner spinner, String valor) {
        if (valor == null) {
            return;
        }

        for (int i = 0; i < spinner.getCount(); i++) {
            if (valor.equals(spinner.getItemAtPosition(i).toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void seleccionarAsignatura(long asignaturaId) {
        if (asignaturaId <= 0) {
            spinnerAsignatura.setSelection(0);
            return;
        }

        for (int i = 0; i < asignaturas.size(); i++) {
            if (asignaturas.get(i).getId() == asignaturaId) {
                spinnerAsignatura.setSelection(i + 1);
                return;
            }
        }
        spinnerAsignatura.setSelection(0);
    }

    private long obtenerAsignaturaSeleccionadaId() {
        int posicion = spinnerAsignatura.getSelectedItemPosition();
        if (posicion <= 0 || posicion - 1 >= asignaturas.size()) {
            return 0;
        }
        return asignaturas.get(posicion - 1).getId();
    }

    private void configurarFormatoAutomaticoFecha() {
        edtFecha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita acción antes del cambio.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // El formato se aplica después del cambio.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (actualizandoFecha) {
                    return;
                }

                String numeros = editable.toString().replaceAll("[^0-9]", "");

                if (numeros.length() > 8) {
                    numeros = numeros.substring(0, 8);
                }

                String fechaFormateada = formatearFecha(numeros);

                if (!fechaFormateada.equals(editable.toString())) {
                    actualizandoFecha = true;
                    edtFecha.setText(fechaFormateada);
                    edtFecha.setSelection(fechaFormateada.length());
                    actualizandoFecha = false;
                }
            }
        });
    }

    private String formatearFecha(String numeros) {
        if (numeros.length() <= 2) {
            return numeros;
        }

        if (numeros.length() <= 4) {
            return numeros.substring(0, 2) + "/" + numeros.substring(2);
        }

        return numeros.substring(0, 2) + "/" +
                numeros.substring(2, 4) + "/" +
                numeros.substring(4);
    }

    private void guardarCambios() {
        String titulo = edtTitulo.getText().toString().trim();
        String descripcion = edtDescripcion.getText().toString().trim();
        String fecha = edtFecha.getText().toString().trim();
        String tipo = spinnerTipo.getSelectedItem().toString();
        String prioridad = spinnerPrioridad.getSelectedItem().toString();
        long asignaturaId = obtenerAsignaturaSeleccionadaId();

        edtTitulo.setError(null);
        edtFecha.setError(null);

        if (titulo.isEmpty()) {
            edtTitulo.setError(getString(R.string.error_titulo_tarea));
            edtTitulo.requestFocus();
            return;
        }

        if (fecha.isEmpty()) {
            edtFecha.setError(getString(R.string.error_fecha_tarea));
            edtFecha.requestFocus();
            return;
        }

        if (!fechaValida(fecha)) {
            edtFecha.setError(getString(R.string.error_formato_fecha));
            edtFecha.requestFocus();
            return;
        }

        boolean actualizado = tareaRepository.actualizarTarea(
                tareaId,
                titulo,
                descripcion,
                tipo,
                fecha,
                prioridad,
                asignaturaId
        );

        if (!actualizado) {
            Toast.makeText(this, R.string.toast_error_editar_tarea, Toast.LENGTH_SHORT).show();
            return;
        }

        FeedbackHelper.mostrarExito(this, getString(R.string.toast_tarea_editada));
        finish();
    }

    private boolean fechaValida(String fecha) {
        if (!fecha.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false;
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        formato.setLenient(false);

        try {
            formato.parse(fecha);
            return true;
        } catch (ParseException e) {
            return false;
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
