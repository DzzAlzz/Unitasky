package com.example.unitasky.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad sencilla para transformar contraseñas mediante SHA-256 antes de guardarlas o compararlas.
 */
public final class PasswordUtils {

    private PasswordUtils() {
    }

    /**
     * Genera el hash SHA-256 de un texto en formato hexadecimal.
     *
     * @param texto texto que se desea procesar.
     * @return representación SHA-256 del texto.
     * @throws IllegalStateException si el algoritmo SHA-256 no está disponible.
     */
    public static String sha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder resultado = new StringBuilder();
            for (byte b : hash) {
                resultado.append(String.format("%02x", b));
            }
            return resultado.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
