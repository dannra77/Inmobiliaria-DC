package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.dto;

import java.math.BigDecimal;
import java.util.List;

public class PagoRequest {
    private Long cuotaId;
    private BigDecimal monto;
    private String metodoPago;
    private List<DescuentoDto> descuentos;
    private String observaciones;

    // Constructor vacío
    public PagoRequest() {}

    // Constructor con parámetros
    public PagoRequest(Long cuotaId, BigDecimal monto, String metodoPago,
                       List<DescuentoDto> descuentos, String observaciones) {
        this.cuotaId = cuotaId;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.descuentos = descuentos;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public Long getCuotaId() { return cuotaId; }
    public void setCuotaId(Long cuotaId) { this.cuotaId = cuotaId; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public List<DescuentoDto> getDescuentos() { return descuentos; }
    public void setDescuentos(List<DescuentoDto> descuentos) { this.descuentos = descuentos; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}