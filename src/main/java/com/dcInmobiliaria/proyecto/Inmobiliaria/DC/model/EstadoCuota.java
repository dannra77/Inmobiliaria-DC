package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

public enum EstadoCuota {
    PENDIENTE("Pendiente"),
    PAGADA("Pagada"),
    VENCIDA("Vencida"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoCuota(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}