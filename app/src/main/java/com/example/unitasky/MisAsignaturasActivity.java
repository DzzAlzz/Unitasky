package com.example.unitasky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
            Button btnEditar = item.findViewById(R.id.btnEditarAsignatura);
            Button btnEliminar = item.findViewById(R.id.btnEliminarAsignatura);

            txtNombre.setText(asignatura.getNombre());

            btnEditar.setOnClickListener(v -> abrirEdicion(asignatura));
            btnEliminar.setOnClickListener(v -> confirmarEliminacion(asignatura));

            contenedorAsignaturas.addView(item);
        }
    }

    private void abrirEdicion(Asignatura asignatura) {
        Intent intent = new Intent(this, EditarAsignaturaActivity.class);
        intent.putExtra(EditarAsignaturaActivity.EXTRA_ASIGNATURA_ID, asignatura.getId());
        startActivity(intent);
    }

    private void confirmarEliminacion(Asignatura asignatura) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.eliminar_asignatura)
                .setMessage(R.string.confirmar_eliminar_asignatura)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.eliminar, (dialog, which) -> eliminarAsignatura(asignatura))
                .show();
    }

    private void eliminarAsignatura(Asignatura asignatura) {
        if (!tareaRepository.eliminarAsignatura(asignatura.getId())) {
            Toast.makeText(this, R.string.toast_error_eliminar_asignatura, Toast.LENGTH_SHORT).show();
            return;
        }

        FeedbackHelper.mostrarExito(this, getString(R.string.toast_asignatura_eliminada));
        mostrarAsignaturas();
    }

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        super.onDestroy();
    }
}
