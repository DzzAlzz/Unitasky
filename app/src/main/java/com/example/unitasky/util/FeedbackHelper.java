package com.example.unitasky.util;

import android.app.Activity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import com.example.unitasky.R;

/** Utilidad para mostrar mensajes visuales simples al usuario. */
public final class FeedbackHelper {

    private FeedbackHelper() {
        // Clase de utilidad: no necesita instancias.
    }

    /**
     * Muestra un Toast de confirmación con el color de éxito definido por la aplicación.
     *
     * @param activity Activity desde la que se muestra el mensaje.
     * @param mensaje texto que se desea informar al usuario.
     */
    public static void mostrarExito(Activity activity, String mensaje) {
        SpannableString texto = new SpannableString("✓  " + mensaje);
        texto.setSpan(
                new ForegroundColorSpan(activity.getColor(R.color.verde_exito)),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        Toast.makeText(activity, texto, Toast.LENGTH_SHORT).show();
    }
}
