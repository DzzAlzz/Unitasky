package com.example.unitasky;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ProximasEntregasActivity extends Activity {

    private LinearLayout contenedorEntregas;
    private TextView txtSinEntregas;
    private TareaRepository tareaRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximas_entregas);

        contenedorEntregas = findViewById(R.id.contenedorEntregas);
        txtSinEntregas = findViewById(R.id.txtSinEntregas);
        Button btnVolver = findViewById(R.id.btnVolver);

        tareaRepository = new TareaRepository(this);

        btnVolver.setOnClickListener(v -> finish());
        mostrarProximasEntregas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contenedorEntregas != null && tareaRepository != null) {
            mostrarProximasEntregas();
        }
    }

    private void mostrarProximasEntregas() {
        contenedorEntregas.removeAllViews();

        // La consulta ya llega filtrada y ordenada directamente desde SQLite.
        List<Tarea> pendientes = tareaRepository.obtenerProximasEntregas();

        if (pendientes.isEmpty()) {
            txtSinEntregas.setVisibility(View.VISIBLE);
            return;
        }

        txtSinEntregas.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Tarea tarea : pendientes) {
            View item = inflater.inflate(R.layout.item_proxima_entrega, contenedorEntregas, false);

            TextView txtTitulo = item.findViewById(R.id.txtEntregaTitulo);
            TextView txtAsignatura = item.findViewById(R.id.txtEntregaAsignatura);
            TextView txtTipo = item.findViewById(R.id.txtEntregaTipo);
            TextView txtFecha = item.findViewById(R.id.txtEntregaFecha);
            TextView txtPrioridad = item.findViewById(R.id.txtEntregaPrioridad);

            txtTitulo.setText(tarea.getTitulo());
            String nombreAsignatura = tarea.getAsignaturaNombre() == null || tarea.getAsignaturaNombre().trim().isEmpty()
                    ? getString(R.string.sin_asignatura)
                    : tarea.getAsignaturaNombre();
            txtAsignatura.setText(getString(R.string.formato_asignatura, nombreAsignatura));
            txtTipo.setText(getString(R.string.formato_tipo, tarea.getTipo()));
            txtFecha.setText(getString(R.string.formato_entrega, tarea.getFecha()));
            txtPrioridad.setText(getString(R.string.formato_prioridad, tarea.getPrioridad()));

            contenedorEntregas.addView(item);
        }
    }

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        super.onDestroy();
    }
}
