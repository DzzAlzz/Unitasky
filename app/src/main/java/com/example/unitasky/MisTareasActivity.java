package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MisTareasActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tareas);

        Button btnAgregarTarea = findViewById(R.id.btnAgregarDesdeLista);
        Button btnCompletarEjemplo1 = findViewById(R.id.btnCompletarEjemplo1);
        Button btnCompletarEjemplo2 = findViewById(R.id.btnCompletarEjemplo2);
        TextView txtEstadoEjemplo1 = findViewById(R.id.txtEstadoEjemplo1);
        TextView txtEstadoEjemplo2 = findViewById(R.id.txtEstadoEjemplo2);

        LinearLayout cardNuevaTarea = findViewById(R.id.cardNuevaTarea);
        TextView txtNuevaTareaTitulo = findViewById(R.id.txtNuevaTareaTitulo);
        TextView txtNuevaTareaFecha = findViewById(R.id.txtNuevaTareaFecha);
        TextView txtNuevaTareaPrioridad = findViewById(R.id.txtNuevaTareaPrioridad);
        TextView txtNuevaTareaEstado = findViewById(R.id.txtNuevaTareaEstado);
        Button btnCompletarNueva = findViewById(R.id.btnCompletarNueva);

        btnAgregarTarea.setOnClickListener(v -> {
            Intent intent = new Intent(MisTareasActivity.this, AgregarTareaActivity.class);
            startActivity(intent);
        });

        btnCompletarEjemplo1.setOnClickListener(v ->
                marcarCompletada(txtEstadoEjemplo1, btnCompletarEjemplo1));

        btnCompletarEjemplo2.setOnClickListener(v ->
                marcarCompletada(txtEstadoEjemplo2, btnCompletarEjemplo2));

        Intent intentRecibido = getIntent();
        if (intentRecibido.hasExtra("titulo_tarea")) {
            String titulo = intentRecibido.getStringExtra("titulo_tarea");
            String fecha = intentRecibido.getStringExtra("fecha_tarea");
            String prioridad = intentRecibido.getStringExtra("prioridad_tarea");
            String estado = intentRecibido.getStringExtra("estado_tarea");

            txtNuevaTareaTitulo.setText(titulo);
            txtNuevaTareaFecha.setText(getString(R.string.formato_fecha, fecha));
            txtNuevaTareaPrioridad.setText(getString(R.string.formato_prioridad, prioridad));
            txtNuevaTareaEstado.setText(getString(R.string.formato_estado, estado));
            cardNuevaTarea.setVisibility(View.VISIBLE);

            if (getString(R.string.estado_completada).equals(estado)) {
                btnCompletarNueva.setVisibility(View.GONE);
            }
        }

        btnCompletarNueva.setOnClickListener(v ->
                marcarCompletada(txtNuevaTareaEstado, btnCompletarNueva));
    }

    private void marcarCompletada(TextView txtEstado, Button boton) {
        txtEstado.setText(getString(R.string.formato_estado, getString(R.string.estado_completada)));
        boton.setVisibility(View.GONE);
        Toast.makeText(this, R.string.toast_tarea_completada, Toast.LENGTH_SHORT).show();
    }
}
