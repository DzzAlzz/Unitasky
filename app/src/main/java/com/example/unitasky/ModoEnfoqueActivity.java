package com.example.unitasky;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ModoEnfoqueActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorProximidad;
    private TextView txtEstadoSensor;
    private boolean sensorRegistrado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modo_enfoque);

        txtEstadoSensor = findViewById(R.id.txtEstadoSensor);
        Button btnVolver = findViewById(R.id.btnVolverModoEnfoque);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorProximidad = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        if (sensorProximidad == null) {
            txtEstadoSensor.setText(R.string.sensor_no_disponible);
        } else {
            txtEstadoSensor.setText(R.string.sensor_listo);
        }

        btnVolver.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registrarSensor();
    }

    @Override
    protected void onPause() {
        liberarSensor();
        super.onPause();
    }

    private void registrarSensor() {
        if (sensorManager == null || sensorProximidad == null || sensorRegistrado) {
            return;
        }

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
        float rangoMaximo = sensorProximidad.getMaximumRange();

        if (distancia < rangoMaximo) {
            txtEstadoSensor.setText(R.string.sensor_cubierto);
        } else {
            txtEstadoSensor.setText(R.string.sensor_libre);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se necesita realizar ninguna acción para esta demostración básica.
    }
}
