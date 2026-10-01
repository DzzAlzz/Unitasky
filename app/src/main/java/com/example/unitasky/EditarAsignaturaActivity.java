package com.example.unitasky;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditarAsignaturaActivity extends Activity {

    public static final String EXTRA_ASIGNATURA_ID = "asignaturaId";

    private EditText edtNombreAsignatura;
    private TareaRepository tareaRepository;
    private long asignaturaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_asignatura);

        edtNombreAsignatura = findViewById(R.id.edtNombreAsignatura);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnGuardar = findViewById(R.id.btnGuardarCambiosAsignatura);

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

        Asignatura asignatura = tareaRepository.obtenerAsignaturaPorId(asignaturaId);
        if (asignatura == null) {
            Toast.makeText(this, R.string.toast_asignatura_no_encontrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtNombreAsignatura.setText(asignatura.getNombre());
        edtNombreAsignatura.setSelection(edtNombreAsignatura.getText().length());
    }

    private void guardarCambios() {
        String nombre = edtNombreAsignatura.getText().toString().trim();
        edtNombreAsignatura.setError(null);

        if (nombre.isEmpty()) {
            edtNombreAsignatura.setError(getString(R.string.error_nombre_asignatura));
            edtNombreAsignatura.requestFocus();
            return;
        }

        if (tareaRepository.existeAsignatura(nombre, asignaturaId)) {
            edtNombreAsignatura.setError(getString(R.string.error_asignatura_duplicada));
            edtNombreAsignatura.requestFocus();
            return;
        }

        if (!tareaRepository.actualizarAsignatura(asignaturaId, nombre)) {
            Toast.makeText(this, R.string.toast_error_editar_asignatura, Toast.LENGTH_SHORT).show();
            return;
        }

        FeedbackHelper.mostrarExito(this, getString(R.string.toast_asignatura_actualizada));
        finish();
    }

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        super.onDestroy();
    }
}
