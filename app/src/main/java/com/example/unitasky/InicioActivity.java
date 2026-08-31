package com.example.unitasky;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class InicioActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        Button btnMisTareas = findViewById(R.id.btnMisTareas);
        Button btnAgregarTarea = findViewById(R.id.btnAgregarTarea);
        Button btnProximasEntregas = findViewById(R.id.btnProximasEntregas);

        btnMisTareas.setOnClickListener(v -> mostrarProximoAvance());
        btnAgregarTarea.setOnClickListener(v -> mostrarProximoAvance());
        btnProximasEntregas.setOnClickListener(v -> mostrarProximoAvance());
    }

    private void mostrarProximoAvance() {
        Toast.makeText(this, R.string.toast_proximos_avances, Toast.LENGTH_SHORT).show();
    }
}
