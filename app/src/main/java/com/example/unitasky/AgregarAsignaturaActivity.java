package com.example.unitasky;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AgregarAsignaturaActivity extends Activity {

    private EditText edtNombreAsignatura;
    private Button btnGuardar;
    private TareaRepository tareaRepository;
    private boolean guardandoAsignatura = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_asignatura);

        edtNombreAsignatura = findViewById(R.id.edtNombreAsignatura);
        Button btnVolver = findViewById(R.id.btnVolver);
        btnGuardar = findViewById(R.id.btnGuardarAsignatura);

        tareaRepository = new TareaRepository(this);

        btnVolver.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarAsignatura());
    }

    private void guardarAsignatura() {
        String nombre = edtNombreAsignatura.getText().toString().trim();
        edtNombreAsignatura.setError(null);

        if (nombre.isEmpty()) {
            edtNombreAsignatura.setError(getString(R.string.error_nombre_asignatura));
            edtNombreAsignatura.requestFocus();
            return;
        }

        if (guardandoAsignatura) {
            return;
        }
        guardandoAsignatura = true;
        btnGuardar.setEnabled(false);

        tareaRepository.existeAsignaturaAsync(nombre, existe ->
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (existe) {
                        guardandoAsignatura = false;
                        btnGuardar.setEnabled(true);
                        edtNombreAsignatura.setError(getString(R.string.error_asignatura_duplicada));
                        edtNombreAsignatura.requestFocus();
                        return;
                    }

                    agregarAsignatura(nombre);
                })
        );
    }

    private void agregarAsignatura(String nombre) {
        Asignatura asignatura = new Asignatura(nombre);
        tareaRepository.agregarAsignaturaAsync(asignatura, guardada ->
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    guardandoAsignatura = false;
                    btnGuardar.setEnabled(true);

                    if (!guardada) {
                        Toast.makeText(
                                this,
                                R.string.toast_error_guardar_asignatura,
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    FeedbackHelper.mostrarExito(
                            this,
                            getString(R.string.toast_asignatura_agregada)
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
