package com.example.unitasky;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModoEnfoqueActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorProximidad;
    private boolean sensorRegistrado = false;
    private boolean sensorCubierto = false;
    private boolean estadoSensorConocido = false;

    private TextView txtEstadoSensor;
    private TextView txtTiempoEnfoque;
    private TextView txtTareaSeleccionada;
    private Spinner spinnerTareas;

    private TareaRepository tareaRepository;
    private final List<Tarea> tareasPendientes = new ArrayList<>();
    private long tareaSeleccionadaId = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean temporizadorActivo = false;
    private long tiempoAcumuladoMs = 0;
    private long inicioTramoMs = 0;

    private final Runnable actualizarTemporizador = new Runnable() {
        @Override
        public void run() {
            actualizarTiempoEnPantalla();
            if (temporizadorActivo) {
                handler.postDelayed(this, 250);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modo_enfoque);

        txtEstadoSensor = findViewById(R.id.txtEstadoSensor);
        txtTiempoEnfoque = findViewById(R.id.txtTiempoEnfoque);
        txtTareaSeleccionada = findViewById(R.id.txtTareaSeleccionada);
        spinnerTareas = findViewById(R.id.spinnerTareasEnfoque);
        Button btnReiniciar = findViewById(R.id.btnReiniciarEnfoque);
        Button btnVolver = findViewById(R.id.btnVolverModoEnfoque);

        tareaRepository = new TareaRepository(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorProximidad = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        if (sensorProximidad == null) {
            txtEstadoSensor.setText(R.string.sensor_no_disponible);
        } else {
            txtEstadoSensor.setText(R.string.sensor_listo);
        }

        configurarSpinnerTareas();
        cargarTareasPendientes();
        actualizarTiempoEnPantalla();

        btnReiniciar.setOnClickListener(v -> reiniciarSesion());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void configurarSpinnerTareas() {
        spinnerTareas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (tareasPendientes.isEmpty()) {
                    tareaSeleccionadaId = 0;
                    txtTareaSeleccionada.setText(R.string.modo_enfoque_sin_tareas_pendientes);
                    pausarTemporizador();
                    return;
                }

                long nuevaTareaId = 0;
                String nombreTarea = getString(R.string.modo_enfoque_sin_tarea_seleccionada);

                if (position > 0 && position - 1 < tareasPendientes.size()) {
                    Tarea tarea = tareasPendientes.get(position - 1);
                    nuevaTareaId = tarea.getId();
                    nombreTarea = tarea.getTitulo();
                }

                if (tareaSeleccionadaId != nuevaTareaId) {
                    pausarTemporizador();
                    tiempoAcumuladoMs = 0;
                    actualizarTiempoEnPantalla();
                }

                tareaSeleccionadaId = nuevaTareaId;
                txtTareaSeleccionada.setText(getString(
                        R.string.modo_enfoque_tarea_actual,
                        nombreTarea
                ));

                if (sensorCubierto && tareaSeleccionadaId > 0 && sensorProximidad != null) {
                    txtEstadoSensor.setText(R.string.modo_enfoque_activo);
                    iniciarTemporizador();
                } else if (sensorProximidad != null && tareaSeleccionadaId > 0) {
                    txtEstadoSensor.setText(R.string.modo_enfoque_pausado);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tareaSeleccionadaId = 0;
                pausarTemporizador();
            }
        });
    }

    private void cargarTareasPendientes() {
        tareasPendientes.clear();
        tareasPendientes.addAll(
                tareaRepository.obtenerTareasPendientes(DatabaseHelper.ORDEN_RECIENTES)
        );

        List<String> opciones = new ArrayList<>();
        opciones.add(getString(R.string.modo_enfoque_selecciona_tarea));

        for (Tarea tarea : tareasPendientes) {
            String asignatura = tarea.getAsignaturaNombre();
            if (asignatura != null && !asignatura.trim().isEmpty()) {
                opciones.add(getString(
                        R.string.modo_enfoque_tarea_con_asignatura,
                        tarea.getTitulo(),
                        asignatura
                ));
            } else {
                opciones.add(tarea.getTitulo());
            }
        }

        if (tareasPendientes.isEmpty()) {
            opciones.clear();
            opciones.add(getString(R.string.modo_enfoque_sin_tareas_pendientes));
            spinnerTareas.setEnabled(false);
            txtTareaSeleccionada.setText(R.string.modo_enfoque_sin_tareas_pendientes);
        } else {
            spinnerTareas.setEnabled(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opciones
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTareas.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registrarSensor();
    }

    @Override
    protected void onPause() {
        pausarTemporizador();
        liberarSensor();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(actualizarTemporizador);
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        super.onDestroy();
    }

    private void registrarSensor() {
        if (sensorManager == null || sensorProximidad == null || sensorRegistrado) {
            return;
        }

        estadoSensorConocido = false;
        sensorRegistrado = sensorManager.registerListener(
                this,
                sensorProximidad,
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    private void liberarSensor() {
        if (sensorManager != null && sensorRegistrado) {
            sensorManager.unregisterListener(this);
            sensorRegistrado = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_PROXIMITY || event.values.length == 0) {
            return;
        }

        float distancia = event.values[0];
        boolean ahoraCubierto = distancia < sensorProximidad.getMaximumRange();

        if (estadoSensorConocido && ahoraCubierto == sensorCubierto) {
            return;
        }

        sensorCubierto = ahoraCubierto;
        estadoSensorConocido = true;

        if (sensorCubierto) {
            if (tareaSeleccionadaId <= 0) {
                txtEstadoSensor.setText(R.string.modo_enfoque_selecciona_antes);
                pausarTemporizador();
                return;
            }

            txtEstadoSensor.setText(R.string.modo_enfoque_activo);
            iniciarTemporizador();
        } else {
            txtEstadoSensor.setText(
                    tareaSeleccionadaId > 0
                            ? R.string.modo_enfoque_pausado
                            : R.string.sensor_libre
            );
            pausarTemporizador();
        }
    }

    private void iniciarTemporizador() {
        if (temporizadorActivo || tareaSeleccionadaId <= 0 || sensorProximidad == null) {
            return;
        }

        temporizadorActivo = true;
        inicioTramoMs = SystemClock.elapsedRealtime();
        handler.removeCallbacks(actualizarTemporizador);
        handler.post(actualizarTemporizador);
    }

    private void pausarTemporizador() {
        if (temporizadorActivo) {
            tiempoAcumuladoMs += SystemClock.elapsedRealtime() - inicioTramoMs;
            temporizadorActivo = false;
        }
        handler.removeCallbacks(actualizarTemporizador);
        actualizarTiempoEnPantalla();
    }

    private void reiniciarSesion() {
        pausarTemporizador();
        tiempoAcumuladoMs = 0;
        actualizarTiempoEnPantalla();

        if (sensorProximidad == null) {
            txtEstadoSensor.setText(R.string.sensor_no_disponible);
        } else if (tareaSeleccionadaId <= 0) {
            txtEstadoSensor.setText(R.string.sensor_listo);
        } else if (sensorCubierto) {
            txtEstadoSensor.setText(R.string.modo_enfoque_activo);
            iniciarTemporizador();
        } else {
            txtEstadoSensor.setText(R.string.modo_enfoque_pausado);
        }
    }

    private void actualizarTiempoEnPantalla() {
        long totalMs = tiempoAcumuladoMs;
        if (temporizadorActivo) {
            totalMs += SystemClock.elapsedRealtime() - inicioTramoMs;
        }

        long totalSegundos = totalMs / 1000;
        long minutos = totalSegundos / 60;
        long segundos = totalSegundos % 60;

        txtTiempoEnfoque.setText(String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutos,
                segundos
        ));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se requiere una acción especial para el modo enfoque básico.
    }
}
