package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

public enum RolContrato {
    LOCADOR("Locador"),
    LOCATARIO("Locatario"),
    GARANTE("Garante"),
    HABITANTE("Habitante"),
    ASESOR("Asesor"),
    SOLICITANTE("Solicitante");  // ← AGREGÁ EL PARÁMETRO

    private final String descripcion;

    RolContrato(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}