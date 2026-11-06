package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
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

public class AgregarTareaActivity extends Activity {

    private EditText edtTitulo;
    private EditText edtDescripcion;
    private EditText edtFecha;
    private Spinner spinnerTipo;
    private Spinner spinnerPrioridad;
    private Spinner spinnerEstado;
    private Spinner spinnerAsignatura;
    private TareaRepository tareaRepository;
    private List<Asignatura> asignaturas = new ArrayList<>();
    private boolean actualizandoFecha = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_tarea);

        edtTitulo = findViewById(R.id.edtTituloTarea);
        edtDescripcion = findViewById(R.id.edtDescripcionTarea);
        edtFecha = findViewById(R.id.edtFechaTarea);
        spinnerTipo = findViewById(R.id.spinnerTipoActividad);
        spinnerPrioridad = findViewById(R.id.spinnerPrioridad);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        spinnerAsignatura = findViewById(R.id.spinnerAsignaturaTarea);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnGuardar = findViewById(R.id.btnGuardarTarea);

        tareaRepository = new TareaRepository(this);

        btnVolver.setOnClickListener(v -> finish());

        configurarSpinner(spinnerTipo, R.array.tipos_actividad);
        configurarSpinner(spinnerPrioridad, R.array.prioridades);
        configurarSpinner(spinnerEstado, R.array.estados_tarea);
        cargarAsignaturas();
        configurarFormatoAutomaticoFecha();

        btnGuardar.setOnClickListener(v -> guardarTarea());
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

    private void guardarTarea() {
        String titulo = edtTitulo.getText().toString().trim();
        String descripcion = edtDescripcion.getText().toString().trim();
        String fecha = edtFecha.getText().toString().trim();
        String tipo = spinnerTipo.getSelectedItem().toString();
        String prioridad = spinnerPrioridad.getSelectedItem().toString();
        String estado = spinnerEstado.getSelectedItem().toString();
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

        Tarea nuevaTarea = new Tarea(
                titulo,
                descripcion,
                fecha,
                tipo,
                prioridad,
                estado,
                asignaturaId
        );

        if (!tareaRepository.agregarTarea(nuevaTarea)) {
            Toast.makeText(this, R.string.toast_error_guardar_tarea, Toast.LENGTH_SHORT).show();
            return;
        }

        FeedbackHelper.mostrarExito(this, getString(R.string.toast_tarea_agregada));

        Intent intent = new Intent(AgregarTareaActivity.this, MisTareasActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
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
