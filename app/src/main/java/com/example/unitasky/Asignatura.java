package com.example.unitasky;

public class Asignatura {

    private long id;
    private final String nombre;

    public Asignatura(String nombre) {
        this(0, nombre);
    }

    public Asignatura(long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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

    @Override
    public String toString() {
        return nombre;
    }
}
