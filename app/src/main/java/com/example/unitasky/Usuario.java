package com.example.unitasky;

public class Usuario {

    private long id;
    private final String nombre;
    private final String correo;

    public Usuario(String nombre, String correo) {
        this(0, nombre, correo);
    }

    public Usuario(long id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }
}
