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
    private Button btnIniciarSesion;
    private boolean passwordVisible = false;
    private boolean autenticando = false;
    private TareaRepository tareaRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtCorreo = findViewById(R.id.edtCorreoLogin);
        edtPassword = findViewById(R.id.edtPasswordLogin);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        TextView txtOlvidePassword = findViewById(R.id.txtOlvidePassword);
        TextView txtIrRegistro = findViewById(R.id.txtIrRegistro);
        txtMostrarPassword = findViewById(R.id.txtMostrarPassword);

        tareaRepository = new TareaRepository(this);
        sessionManager = new SessionManager(this);

        btnIniciarSesion.setOnClickListener(v -> validarLogin());

        txtIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class))
        );

        txtOlvidePassword.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this,
                        R.string.toast_recuperacion,
                        Toast.LENGTH_SHORT).show());

        txtMostrarPassword.setOnClickListener(v -> cambiarVisibilidadPassword());

        validarSesionExistente();
    }

    private void validarSesionExistente() {
        if (!sessionManager.haySesionIniciada()) {
            return;
        }

        btnIniciarSesion.setEnabled(false);
        long usuarioId = sessionManager.getUsuarioId();

        tareaRepository.obtenerUsuarioPorIdAsync(usuarioId, usuario ->
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (usuario != null) {
                        sessionManager.actualizarDatosUsuario(usuario);
                        abrirInicio();
                        return;
                    }

                    sessionManager.cerrarSesion();
                    btnIniciarSesion.setEnabled(true);
                })
        );
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

        if (autenticando) {
            return;
        }
        autenticando = true;
        btnIniciarSesion.setEnabled(false);

        tareaRepository.validarUsuarioAsync(correo, password, usuario ->
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    autenticando = false;
                    btnIniciarSesion.setEnabled(true);

                    if (usuario == null) {
                        edtPassword.setError(getString(R.string.error_credenciales));
                        edtPassword.setText("");
                        edtPassword.requestFocus();
                        return;
                    }

                    // SharedPreferences conserva solo información de sesión, nunca la contraseña.
                    sessionManager.iniciarSesion(usuario);
                    Toast.makeText(this, R.string.toast_login_correcto, Toast.LENGTH_SHORT).show();
                    abrirInicio();
                })
        );
    }

    private void abrirInicio() {
        Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

    @Override
    protected void onDestroy() {
        if (tareaRepository != null) {
            tareaRepository.cerrar();
        }
        super.onDestroy();
    }
}
