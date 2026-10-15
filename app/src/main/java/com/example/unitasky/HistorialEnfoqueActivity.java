package com.example.unitasky;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class HistorialEnfoqueActivity extends Activity {

    public static final String EXTRA_TAREA_ID = "tarea_id";
    public static final String EXTRA_TAREA_TITULO = "tarea_titulo";

    private TareaRepository tareaRepository;
    private long tareaId;
    private TextView txtTarea;
    private TextView txtEstadoVacio;
    private LinearLayout contenedorSesiones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_enfoque);

        txtTarea = findViewById(R.id.txtHistorialTarea);
        txtEstadoVacio = findViewById(R.id.txtHistorialVacio);
        contenedorSesiones = findViewById(R.id.contenedorSesionesEnfoque);
        Button btnVolver = findViewById(R.id.btnVolverHistorialEnfoque);

        tareaRepository = new TareaRepository(this);
        tareaId = getIntent().getLongExtra(EXTRA_TAREA_ID, 0);

        if (tareaId <= 0) {
            Toast.makeText(this, R.string.historial_enfoque_tarea_no_valida, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Tarea tarea = tareaRepository.obtenerTareaPorId(tareaId);
        if (tarea == null) {
            Toast.makeText(this, R.string.historial_enfoque_tarea_no_valida, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String titulo = getIntent().getStringExtra(EXTRA_TAREA_TITULO);
        if (titulo == null || titulo.trim().isEmpty()) {
            titulo = tarea.getTitulo();
        }
        txtTarea.setText(getString(R.string.historial_enfoque_tarea_formato, titulo));

        btnVolver.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tareaId > 0 && tareaRepository != null) {
            cargarSesiones();
        }
    }

    private void cargarSesiones() {
        List<SesionEnfoque> sesiones = tareaRepository.obtenerSesionesEnfoque(tareaId);
        contenedorSesiones.removeAllViews();

        if (sesiones.isEmpty()) {
            txtEstadoVacio.setVisibility(View.VISIBLE);
            return;
        }

        txtEstadoVacio.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (SesionEnfoque sesion : sesiones) {
            View item = inflater.inflate(R.layout.item_sesion_enfoque, contenedorSesiones, false);
            TextView txtFecha = item.findViewById(R.id.txtSesionFecha);
            TextView txtDuracion = item.findViewById(R.id.txtSesionDuracion);

            txtFecha.setText(sesion.getFecha());
            txtDuracion.setText(getString(
                    R.string.historial_enfoque_duracion_formato,
                    formatearDuracion(sesion.getDuracionSegundos())
            ));

            contenedorSesiones.addView(item);
        }
    }

    private String formatearDuracion(int totalSegundos) {
        int horas = totalSegundos / 3600;
        int minutos = (totalSegundos % 3600) / 60;
        int segundos = totalSegundos % 60;

        if (horas > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos);
        }
        return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
    }

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        super.onDestroy();
    }
}
