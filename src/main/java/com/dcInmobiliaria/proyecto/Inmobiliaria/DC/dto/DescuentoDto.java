package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.dto;

import java.math.BigDecimal;

public class DescuentoDto {
    private String concepto;
    private BigDecimal monto;

    // Constructor vacío
    public DescuentoDto() {}

    // Constructor con parámetros
    public DescuentoDto(String concepto, BigDecimal monto) {
        this.concepto = concepto;
        this.monto = monto;
    }

    // Getters y Setters
    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}