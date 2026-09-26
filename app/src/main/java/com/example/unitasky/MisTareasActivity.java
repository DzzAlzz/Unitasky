package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MisTareasActivity extends Activity {

    private LinearLayout contenedorTareas;
    private TextView txtSinTareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tareas);

        contenedorTareas = findViewById(R.id.contenedorTareas);
        txtSinTareas = findViewById(R.id.txtSinTareas);
        Button btnAgregarTarea = findViewById(R.id.btnAgregarDesdeLista);

        TareaRepository.inicializarEjemplos();
        mostrarTareas();

        btnAgregarTarea.setOnClickListener(v -> {
            Intent intent = new Intent(MisTareasActivity.this, AgregarTareaActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contenedorTareas != null) {
            mostrarTareas();
        }
    }

    private void mostrarTareas() {
        contenedorTareas.removeAllViews();

        if (TareaRepository.obtenerTareas().isEmpty()) {
            txtSinTareas.setVisibility(View.VISIBLE);
            return;
        }

        txtSinTareas.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Tarea tarea : TareaRepository.obtenerTareas()) {
            View item = inflater.inflate(R.layout.item_tarea, contenedorTareas, false);

            TextView txtTitulo = item.findViewById(R.id.txtItemTitulo);
            TextView txtDescripcion = item.findViewById(R.id.txtItemDescripcion);
            TextView txtTipo = item.findViewById(R.id.txtItemTipo);
            TextView txtFecha = item.findViewById(R.id.txtItemFecha);
            TextView txtPrioridad = item.findViewById(R.id.txtItemPrioridad);
            TextView txtEstado = item.findViewById(R.id.txtItemEstado);
            Button btnCompletar = item.findViewById(R.id.btnItemCompletar);

            txtTitulo.setText(tarea.getTitulo());
            txtTipo.setText(getString(R.string.formato_tipo, tarea.getTipo()));
            txtFecha.setText(getString(R.string.formato_fecha, tarea.getFecha()));
            txtPrioridad.setText(getString(R.string.formato_prioridad, tarea.getPrioridad()));
            txtEstado.setText(getString(R.string.formato_estado, tarea.getEstado()));

            if (tarea.getDescripcion().isEmpty()) {
                txtDescripcion.setVisibility(View.GONE);
            } else {
                txtDescripcion.setText(tarea.getDescripcion());
            }

            if (getString(R.string.estado_completada).equals(tarea.getEstado())) {
                btnCompletar.setVisibility(View.GONE);
            } else {
                btnCompletar.setOnClickListener(v -> {
                    tarea.setEstado(getString(R.string.estado_completada));
                    txtEstado.setText(getString(
                            R.string.formato_estado,
                            getString(R.string.estado_completada)
                    ));
                    btnCompletar.setVisibility(View.GONE);
                    Toast.makeText(
                            MisTareasActivity.this,
                            R.string.toast_tarea_completada,
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

            contenedorTareas.addView(item);
        }
    }
}
