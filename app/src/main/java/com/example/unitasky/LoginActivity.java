package com.example.unitasky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private EditText edtCorreo;
    private EditText edtPassword;
    private TextView txtMostrarPassword;
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtCorreo = findViewById(R.id.edtCorreoLogin);
        edtPassword = findViewById(R.id.edtPasswordLogin);
        Button btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        TextView txtOlvidePassword = findViewById(R.id.txtOlvidePassword);
        TextView txtIrRegistro = findViewById(R.id.txtIrRegistro);
        txtMostrarPassword = findViewById(R.id.txtMostrarPassword);

        btnIniciarSesion.setOnClickListener(v -> validarLogin());

        txtIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        txtOlvidePassword.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this,
                        R.string.toast_recuperacion,
                        Toast.LENGTH_SHORT).show());

        txtMostrarPassword.setOnClickListener(v -> cambiarVisibilidadPassword());
    }

    private void validarLogin() {
        String correo = edtCorreo.getText().toString().trim();
        String password = edtPassword.getText().toString();

        edtCorreo.setError(null);
        edtPassword.setError(null);

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

        Toast.makeText(this, R.string.toast_login_correcto, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
        startActivity(intent);
    }

    private void cambiarVisibilidadPassword() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            txtMostrarPassword.setText(R.string.ocultar_contrasena);
        } else {
            edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            txtMostrarPassword.setText(R.string.mostrar_contrasena);
        }

        edtPassword.setSelection(edtPassword.getText().length());
    }
}
