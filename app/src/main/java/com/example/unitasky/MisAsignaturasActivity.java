package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MisAsignaturasActivity extends Activity {

    private LinearLayout contenedorAsignaturas;
    private TextView txtSinAsignaturas;
    private TareaRepository tareaRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_asignaturas);

        contenedorAsignaturas = findViewById(R.id.contenedorAsignaturas);
        txtSinAsignaturas = findViewById(R.id.txtSinAsignaturas);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnAgregarAsignatura = findViewById(R.id.btnAgregarAsignatura);

        tareaRepository = new TareaRepository(this);

        btnVolver.setOnClickListener(v -> finish());
        btnAgregarAsignatura.setOnClickListener(v ->
                startActivity(new Intent(MisAsignaturasActivity.this, AgregarAsignaturaActivity.class))
        );

        mostrarAsignaturas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (contenedorAsignaturas != null && tareaRepository != null) {
            mostrarAsignaturas();
        }
    }

    private void mostrarAsignaturas() {
        contenedorAsignaturas.removeAllViews();
        List<Asignatura> asignaturas = tareaRepository.obtenerAsignaturas();

        if (asignaturas.isEmpty()) {
            txtSinAsignaturas.setVisibility(View.VISIBLE);
            return;
        }

        txtSinAsignaturas.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Asignatura asignatura : asignaturas) {
            View item = inflater.inflate(R.layout.item_asignatura, contenedorAsignaturas, false);
            TextView txtNombre = item.findViewById(R.id.txtAsignaturaNombre);
            txtNombre.setText(asignatura.getNombre());
            contenedorAsignaturas.addView(item);
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
