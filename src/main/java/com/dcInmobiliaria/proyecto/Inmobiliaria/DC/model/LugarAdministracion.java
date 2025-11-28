package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

public enum LugarAdministracion {
    INMOBILIARIA_DC("Inmobiliaria DC"),
    PROPIETARIO("Propietario"),
    ADMINISTRADOR("Administrador Externo"),
    OTRO("Otro");

    private final String descripcion;

    LugarAdministracion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}