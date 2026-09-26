package com.example.unitasky;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProximasEntregasActivity extends Activity {

    private LinearLayout contenedorEntregas;
    private TextView txtSinEntregas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximas_entregas);

        contenedorEntregas = findViewById(R.id.contenedorEntregas);
        txtSinEntregas = findViewById(R.id.txtSinEntregas);

        TareaRepository.inicializarEjemplos();
        mostrarProximasEntregas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contenedorEntregas != null) {
            mostrarProximasEntregas();
        }
    }

    private void mostrarProximasEntregas() {
        contenedorEntregas.removeAllViews();

        List<Tarea> pendientes = new ArrayList<>();
        for (Tarea tarea : TareaRepository.obtenerTareas()) {
            if (!getString(R.string.estado_completada).equals(tarea.getEstado())) {
                pendientes.add(tarea);
            }
        }

        pendientes.sort(Comparator.comparing(this::convertirFecha));

        if (pendientes.isEmpty()) {
            txtSinEntregas.setVisibility(View.VISIBLE);
            return;
        }

        txtSinEntregas.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Tarea tarea : pendientes) {
            View item = inflater.inflate(R.layout.item_proxima_entrega, contenedorEntregas, false);

            TextView txtTitulo = item.findViewById(R.id.txtEntregaTitulo);
            TextView txtTipo = item.findViewById(R.id.txtEntregaTipo);
            TextView txtFecha = item.findViewById(R.id.txtEntregaFecha);
            TextView txtPrioridad = item.findViewById(R.id.txtEntregaPrioridad);

            txtTitulo.setText(tarea.getTitulo());
            txtTipo.setText(getString(R.string.formato_tipo, tarea.getTipo()));
            txtFecha.setText(getString(R.string.formato_entrega, tarea.getFecha()));
            txtPrioridad.setText(getString(R.string.formato_prioridad, tarea.getPrioridad()));

            contenedorEntregas.addView(item);
        }
    }

    private Date convertirFecha(Tarea tarea) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        formato.setLenient(false);

        try {
            Date fecha = formato.parse(tarea.getFecha());
            return fecha != null ? fecha : new Date(Long.MAX_VALUE);
        } catch (ParseException e) {
            return new Date(Long.MAX_VALUE);
        }
    }
}
