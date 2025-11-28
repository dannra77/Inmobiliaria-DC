package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

public enum ResponsableServicio {
    LOCADOR("Locador"),
    LOCATARIO("Locatario"),
    COMPARTIDO("Compartido");

    private final String descripcion;

    ResponsableServicio(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}