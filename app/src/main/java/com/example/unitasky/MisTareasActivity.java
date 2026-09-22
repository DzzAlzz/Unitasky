package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MisTareasActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tareas);

        Button btnAgregarTarea = findViewById(R.id.btnAgregarDesdeLista);
        LinearLayout cardNuevaTarea = findViewById(R.id.cardNuevaTarea);
        TextView txtNuevaTareaTitulo = findViewById(R.id.txtNuevaTareaTitulo);
        TextView txtNuevaTareaFecha = findViewById(R.id.txtNuevaTareaFecha);
        TextView txtNuevaTareaPrioridad = findViewById(R.id.txtNuevaTareaPrioridad);

        btnAgregarTarea.setOnClickListener(v -> {
            Intent intent = new Intent(MisTareasActivity.this, AgregarTareaActivity.class);
            startActivity(intent);
        });

        Intent intentRecibido = getIntent();
        if (intentRecibido.hasExtra("titulo_tarea")) {
            String titulo = intentRecibido.getStringExtra("titulo_tarea");
            String fecha = intentRecibido.getStringExtra("fecha_tarea");
            String prioridad = intentRecibido.getStringExtra("prioridad_tarea");

            txtNuevaTareaTitulo.setText(titulo);
            txtNuevaTareaFecha.setText(getString(R.string.formato_fecha, fecha));
            txtNuevaTareaPrioridad.setText(getString(R.string.formato_prioridad, prioridad));
            cardNuevaTarea.setVisibility(View.VISIBLE);
        }
    }
}
