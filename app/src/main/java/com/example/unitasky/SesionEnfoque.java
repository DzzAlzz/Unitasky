package com.example.unitasky;

public class SesionEnfoque {

    private final long id;
    private final long tareaId;
    private final String fecha;
    private final int duracionSegundos;

    public SesionEnfoque(long id, long tareaId, String fecha, int duracionSegundos) {
        this.id = id;
        this.tareaId = tareaId;
        this.fecha = fecha;
        this.duracionSegundos = duracionSegundos;
    }

    public long getId() {
        return id;
    }

    public long getTareaId() {
        return tareaId;
    }

    public String getFecha() {
        return fecha;
    }

    public int getDuracionSegundos() {
        return duracionSegundos;
    }
}
