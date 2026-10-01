package com.example.unitasky;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AgregarAsignaturaActivity extends Activity {

    private EditText edtNombreAsignatura;
    private TareaRepository tareaRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_asignatura);

        edtNombreAsignatura = findViewById(R.id.edtNombreAsignatura);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnGuardar = findViewById(R.id.btnGuardarAsignatura);

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

        if (tareaRepository.existeAsignatura(nombre)) {
            edtNombreAsignatura.setError(getString(R.string.error_asignatura_duplicada));
            edtNombreAsignatura.requestFocus();
            return;
        }

        Asignatura asignatura = new Asignatura(nombre);
        if (!tareaRepository.agregarAsignatura(asignatura)) {
            Toast.makeText(this, R.string.toast_error_guardar_asignatura, Toast.LENGTH_SHORT).show();
            return;
        }

        FeedbackHelper.mostrarExito(this, getString(R.string.toast_asignatura_agregada));
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
