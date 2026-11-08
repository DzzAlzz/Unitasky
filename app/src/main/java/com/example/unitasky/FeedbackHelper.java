package com.example.unitasky;

import android.app.Activity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

public final class FeedbackHelper {

    private FeedbackHelper() {
        // Clase de utilidad: no necesita instancias.
    }

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
