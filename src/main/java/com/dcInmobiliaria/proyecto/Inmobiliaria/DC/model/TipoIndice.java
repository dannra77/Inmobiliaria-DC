package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

public enum TipoIndice {
    ICL("Índice de Contratos de Locación"),
    IPC("Índice de Precios al Consumidor"),
    MIXTO("Mixto"),
    SIN_AJUSTE("Sin Ajuste");

    private final String descripcion;

    TipoIndice(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

