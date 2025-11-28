package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

public enum LugarPago {
    INMOBILIARIA_DC("Inmobiliaria DC"),
    TRANSFERENCIA_BANCARIA("Transferencia Bancaria"),
    DEPOSITO("Depósito en Cuenta"),
    EFECTIVO("Efectivo"),
    OTRO("Otro");

    private final String descripcion;

    LugarPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}