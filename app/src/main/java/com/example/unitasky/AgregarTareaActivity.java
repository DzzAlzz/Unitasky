package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AgregarTareaActivity extends Activity {

    private EditText edtTitulo;
    private EditText edtFecha;
    private Spinner spinnerPrioridad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_tarea);

        edtTitulo = findViewById(R.id.edtTituloTarea);
        edtFecha = findViewById(R.id.edtFechaTarea);
        spinnerPrioridad = findViewById(R.id.spinnerPrioridad);
        Button btnGuardar = findViewById(R.id.btnGuardarTarea);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.prioridades,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridad.setAdapter(adapter);

        btnGuardar.setOnClickListener(v -> guardarTarea());
    }

    private void guardarTarea() {
        String titulo = edtTitulo.getText().toString().trim();
        String fecha = edtFecha.getText().toString().trim();
        String prioridad = spinnerPrioridad.getSelectedItem().toString();

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

        Toast.makeText(this, R.string.toast_tarea_agregada, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(AgregarTareaActivity.this, MisTareasActivity.class);
        intent.putExtra("titulo_tarea", titulo);
        intent.putExtra("fecha_tarea", fecha);
        intent.putExtra("prioridad_tarea", prioridad);
        startActivity(intent);
        finish();
    }
}
