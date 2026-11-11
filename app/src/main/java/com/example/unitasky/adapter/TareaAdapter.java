package com.example.unitasky.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unitasky.R;
import com.example.unitasky.model.Tarea;

import java.util.ArrayList;
import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    public interface AccionesTareaListener {
        void onEditar(Tarea tarea);
        void onEliminar(Tarea tarea);
        void onCompletar(Tarea tarea);
    }

    private final List<Tarea> tareas = new ArrayList<>();
    private final AccionesTareaListener listener;

    public TareaAdapter(AccionesTareaListener listener) {
        this.listener = listener;
    }

    public void actualizarTareas(List<Tarea> nuevasTareas) {
        tareas.clear();
        if (nuevasTareas != null) {
            tareas.addAll(nuevasTareas);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        holder.bind(tareas.get(position));
    }

    @Override
    public int getItemCount() {
        return tareas.size();
    }

    class TareaViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtTitulo;
        private final TextView txtDescripcion;
        private final TextView txtAsignatura;
        private final TextView txtTipo;
        private final TextView txtFecha;
        private final TextView txtPrioridad;
        private final TextView txtEstado;
        private final Button btnCompletar;
        private final Button btnEditar;
        private final Button btnEliminar;

        TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtItemTitulo);
            txtDescripcion = itemView.findViewById(R.id.txtItemDescripcion);
            txtAsignatura = itemView.findViewById(R.id.txtItemAsignatura);
            txtTipo = itemView.findViewById(R.id.txtItemTipo);
            txtFecha = itemView.findViewById(R.id.txtItemFecha);
            txtPrioridad = itemView.findViewById(R.id.txtItemPrioridad);
            txtEstado = itemView.findViewById(R.id.txtItemEstado);
            btnCompletar = itemView.findViewById(R.id.btnItemCompletar);
            btnEditar = itemView.findViewById(R.id.btnItemEditar);
            btnEliminar = itemView.findViewById(R.id.btnItemEliminar);
        }

        void bind(Tarea tarea) {
            txtTitulo.setText(tarea.getTitulo());

            String nombreAsignatura = tarea.getAsignaturaNombre();
            if (nombreAsignatura == null || nombreAsignatura.trim().isEmpty()) {
                nombreAsignatura = itemView.getContext().getString(R.string.sin_asignatura);
            }

            txtAsignatura.setText(itemView.getContext().getString(
                    R.string.formato_asignatura,
                    nombreAsignatura
            ));
            txtTipo.setText(itemView.getContext().getString(R.string.formato_tipo, tarea.getTipo()));
            txtFecha.setText(itemView.getContext().getString(R.string.formato_fecha, tarea.getFecha()));
            txtPrioridad.setText(itemView.getContext().getString(
                    R.string.formato_prioridad,
                    tarea.getPrioridad()
            ));
            txtEstado.setText(itemView.getContext().getString(R.string.formato_estado, tarea.getEstado()));

            if (tarea.getDescripcion() == null || tarea.getDescripcion().trim().isEmpty()) {
                txtDescripcion.setVisibility(View.GONE);
            } else {
                txtDescripcion.setVisibility(View.VISIBLE);
                txtDescripcion.setText(tarea.getDescripcion());
            }

            btnEditar.setOnClickListener(v -> listener.onEditar(tarea));
            btnEliminar.setOnClickListener(v -> listener.onEliminar(tarea));

            boolean completada = itemView.getContext()
                    .getString(R.string.estado_completada)
                    .equals(tarea.getEstado());

            btnCompletar.setVisibility(completada ? View.GONE : View.VISIBLE);
            if (completada) {
                btnCompletar.setOnClickListener(null);
            } else {
                btnCompletar.setOnClickListener(v -> listener.onCompletar(tarea));
            }
        }
    }
}
