package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

public enum EstadoContrato {
    BORRADOR("Borrador"),
    ACTIVO("Activo"),
    FINALIZADO("Finalizado"),
    RESCINDIDO("Rescindido"),
    VENCIDO("Vencido");

    private final String descripcion;

    EstadoContrato(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}