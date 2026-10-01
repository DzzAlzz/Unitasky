package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class InicioActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        Button btnMisTareas = findViewById(R.id.btnMisTareas);
        Button btnAgregarTarea = findViewById(R.id.btnAgregarTarea);
        Button btnProximasEntregas = findViewById(R.id.btnProximasEntregas);

        btnMisTareas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, MisTareasActivity.class);
            startActivity(intent);
        });

        btnAgregarTarea.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, AgregarTareaActivity.class);
            startActivity(intent);
        });

        btnProximasEntregas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, ProximasEntregasActivity.class);
            startActivity(intent);
        });
    }
}
