package com.example.unitasky;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditarAsignaturaActivity extends Activity {

    public static final String EXTRA_ASIGNATURA_ID = "asignaturaId";

    private EditText edtNombreAsignatura;
    private Button btnGuardar;
    private TareaRepository tareaRepository;
    private long asignaturaId;
    private boolean guardandoCambios = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_asignatura);

        edtNombreAsignatura = findViewById(R.id.edtNombreAsignatura);
        Button btnVolver = findViewById(R.id.btnVolver);
        btnGuardar = findViewById(R.id.btnGuardarCambiosAsignatura);

        tareaRepository = new TareaRepository(this);
        asignaturaId = getIntent().getLongExtra(EXTRA_ASIGNATURA_ID, -1);

        btnVolver.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarCambios());

        cargarAsignatura();
    }

    private void cargarAsignatura() {
        if (asignaturaId <= 0) {
            Toast.makeText(this, R.string.toast_asignatura_no_encontrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnGuardar.setEnabled(false);
        tareaRepository.obtenerAsignaturaPorIdAsync(asignaturaId, asignatura ->
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (asignatura == null) {
                        Toast.makeText(
                                this,
                                R.string.toast_asignatura_no_encontrada,
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                        return;
                    }

                    edtNombreAsignatura.setText(asignatura.getNombre());
                    edtNombreAsignatura.setSelection(edtNombreAsignatura.getText().length());
                    btnGuardar.setEnabled(true);
                })
        );
    }

    private void guardarCambios() {
        String nombre = edtNombreAsignatura.getText().toString().trim();
        edtNombreAsignatura.setError(null);

        if (nombre.isEmpty()) {
            edtNombreAsignatura.setError(getString(R.string.error_nombre_asignatura));
            edtNombreAsignatura.requestFocus();
            return;
        }

        if (guardandoCambios) {
            return;
        }
        guardandoCambios = true;
        btnGuardar.setEnabled(false);

        tareaRepository.existeAsignaturaAsync(nombre, asignaturaId, existe ->
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (existe) {
                        guardandoCambios = false;
                        btnGuardar.setEnabled(true);
                        edtNombreAsignatura.setError(getString(R.string.error_asignatura_duplicada));
                        edtNombreAsignatura.requestFocus();
                        return;
                    }

                    actualizarAsignatura(nombre);
                })
        );
    }

    private void actualizarAsignatura(String nombre) {
        tareaRepository.actualizarAsignaturaAsync(asignaturaId, nombre, actualizada ->
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    guardandoCambios = false;
                    btnGuardar.setEnabled(true);

                    if (!actualizada) {
                        Toast.makeText(
                                this,
                                R.string.toast_error_editar_asignatura,
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    FeedbackHelper.mostrarExito(
                            this,
                            getString(R.string.toast_asignatura_actualizada)
                    );
                    finish();
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
