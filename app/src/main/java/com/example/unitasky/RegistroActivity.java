package com.example.unitasky;

import android.app.Activity;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegistroActivity extends Activity {

    private EditText edtNombre;
    private EditText edtCorreo;
    private EditText edtPassword;
    private EditText edtConfirmarPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        edtNombre = findViewById(R.id.edtNombreRegistro);
        edtCorreo = findViewById(R.id.edtCorreoRegistro);
        edtPassword = findViewById(R.id.edtPasswordRegistro);
        edtConfirmarPassword = findViewById(R.id.edtConfirmarPassword);
        Button btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        TextView txtVolverLogin = findViewById(R.id.txtVolverLogin);

        btnCrearCuenta.setOnClickListener(v -> validarRegistro());
        txtVolverLogin.setOnClickListener(v -> finish());
    }

    private void validarRegistro() {
        String nombre = edtNombre.getText().toString().trim();
        String correo = edtCorreo.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmarPassword = edtConfirmarPassword.getText().toString();

        edtNombre.setError(null);
        edtCorreo.setError(null);
        edtPassword.setError(null);
        edtConfirmarPassword.setError(null);

        if (nombre.isEmpty()) {
            edtNombre.setError(getString(R.string.error_nombre_vacio));
            edtNombre.requestFocus();
            return;
        }

        if (correo.isEmpty()) {
            edtCorreo.setError(getString(R.string.error_correo_vacio));
            edtCorreo.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtCorreo.setError(getString(R.string.error_correo_invalido));
            edtCorreo.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError(getString(R.string.error_password_vacio));
            edtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtPassword.setError(getString(R.string.error_password_corta));
            edtPassword.requestFocus();
            return;
        }

        if (confirmarPassword.isEmpty()) {
            edtConfirmarPassword.setError(getString(R.string.error_confirmar_password));
            edtConfirmarPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmarPassword)) {
            edtConfirmarPassword.setError(getString(R.string.error_password_distinta));
            edtConfirmarPassword.requestFocus();
            return;
        }

        Toast.makeText(this, R.string.toast_cuenta_creada, Toast.LENGTH_SHORT).show();
        finish();
    }
}
