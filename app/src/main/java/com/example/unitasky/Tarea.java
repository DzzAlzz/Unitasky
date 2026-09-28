package com.example.unitasky;

public class Tarea {

    private long id;
    private final String titulo;
    private final String descripcion;
    private final String fecha;
    private final String tipo;
    private final String prioridad;
    private String estado;

    public Tarea(String titulo, String descripcion, String fecha,
                 String tipo, String prioridad, String estado) {
        this(0, titulo, descripcion, fecha, tipo, prioridad, estado);
    }

    public Tarea(long id, String titulo, String descripcion, String fecha,
                 String tipo, String prioridad, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.estado = estado;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
