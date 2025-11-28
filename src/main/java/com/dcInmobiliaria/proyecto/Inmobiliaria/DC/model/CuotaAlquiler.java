package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cuotas_alquiler")
public class CuotaAlquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private ContratoAlquiler contrato;

    @Column(nullable = false)
    private Integer numeroCuota;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(precision = 10, scale = 2)
    private BigDecimal montoExpensas;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuota estado = EstadoCuota.PENDIENTE;

    @Column(precision = 10, scale = 2)
    private BigDecimal mora;

    @Column(precision = 10, scale = 2)
    private BigDecimal descuento;

    private String conceptoDescuento;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPagado;

    @Column(length = 500)
    private String observaciones;

    @Column(nullable = false)
    private LocalDate fechaCreacion;

    private LocalDate fechaModificacion;

    // Constructores
    public CuotaAlquiler() {
        this.fechaCreacion = LocalDate.now();
        this.fechaModificacion = LocalDate.now();
    }

    public CuotaAlquiler(ContratoAlquiler contrato, Integer numeroCuota, Integer mes, Integer anio,
                         BigDecimal monto, LocalDate fechaVencimiento) {
        this();
        this.contrato = contrato;
        this.numeroCuota = numeroCuota;
        this.mes = mes;
        this.anio = anio;
        this.monto = monto;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = EstadoCuota.PENDIENTE;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) {
        this.id = id;
        this.actualizarFechaModificacion();
    }

    public ContratoAlquiler getContrato() { return contrato; }
    public void setContrato(ContratoAlquiler contrato) {
        this.contrato = contrato;
        this.actualizarFechaModificacion();
    }

    public Integer getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(Integer numeroCuota) {
        this.numeroCuota = numeroCuota;
        this.actualizarFechaModificacion();
    }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) {
        this.mes = mes;
        this.actualizarFechaModificacion();
    }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) {
        this.anio = anio;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getMontoExpensas() { return montoExpensas; }
    public void setMontoExpensas(BigDecimal montoExpensas) {
        this.montoExpensas = montoExpensas;
        this.actualizarFechaModificacion();
    }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
        this.actualizarFechaModificacion();
    }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
        this.actualizarFechaModificacion();
    }

    public EstadoCuota getEstado() { return estado; }
    public void setEstado(EstadoCuota estado) {
        this.estado = estado;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getMora() { return mora; }
    public void setMora(BigDecimal mora) {
        this.mora = mora;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
        this.actualizarFechaModificacion();
    }

    public String getConceptoDescuento() { return conceptoDescuento; }
    public void setConceptoDescuento(String conceptoDescuento) {
        this.conceptoDescuento = conceptoDescuento;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getTotalPagado() { return totalPagado; }
    public void setTotalPagado(BigDecimal totalPagado) {
        this.totalPagado = totalPagado;
        this.actualizarFechaModificacion();
    }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
        this.actualizarFechaModificacion();
    }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDate getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDate fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    // Métodos de negocio
    private void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDate.now();
    }

    public BigDecimal getTotalAPagar() {
        BigDecimal total = monto != null ? monto : BigDecimal.ZERO;

        if (montoExpensas != null) {
            total = total.add(montoExpensas);
        }
        if (mora != null) {
            total = total.add(mora);
        }
        if (descuento != null) {
            total = total.subtract(descuento);
        }

        return total.compareTo(BigDecimal.ZERO) > 0 ? total : BigDecimal.ZERO;
    }

    public boolean isVencida() {
        return estado == EstadoCuota.PENDIENTE &&
                LocalDate.now().isAfter(fechaVencimiento);
    }

    public long getDiasVencidos() {
        if (!isVencida()) return 0;
        return Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(fechaVencimiento, LocalDate.now()));
    }

    public void marcarComoPagada(LocalDate fechaPago, String observaciones) {
        this.estado = EstadoCuota.PAGADA;
        this.fechaPago = fechaPago;
        this.totalPagado = this.getTotalAPagar();
        if (observaciones != null) {
            this.observaciones = observaciones;
        }
        this.actualizarFechaModificacion();
    }

    public void aplicarMora(BigDecimal montoMora) {
        this.mora = montoMora;
        this.actualizarFechaModificacion();
    }

    public void aplicarDescuento(BigDecimal montoDescuento, String concepto) {
        this.descuento = montoDescuento;
        this.conceptoDescuento = concepto;
        this.actualizarFechaModificacion();
    }

    @Override
    public String toString() {
        return "CuotaAlquiler{" +
                "id=" + id +
                ", numeroCuota=" + numeroCuota +
                ", fechaVencimiento=" + fechaVencimiento +
                ", monto=" + monto +
                ", estado=" + estado +
                '}';
    }
}