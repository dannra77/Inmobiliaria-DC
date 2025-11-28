package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuota_id", nullable = false)
    private CuotaAlquiler cuota;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago;

    // NUEVO CAMPO: Estado del pago para el corredor
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPago estadoPago;

    @Column(nullable = false)
    private LocalDate fechaPago;

    @Column(precision = 10, scale = 2)
    private BigDecimal mora;

    @Column(precision = 10, scale = 2)
    private BigDecimal descuentos;

    @Column(precision = 10, scale = 2)
    private BigDecimal comisionCorredor;

    @Column(precision = 10, scale = 2)
    private BigDecimal montoLocador;

    @Column(nullable = false)
    private Boolean distribuido = false;

    private LocalDate fechaDistribucion;

    @Column(length = 500)
    private String observaciones;

    private String numeroFactura;

    @Column(nullable = false)
    private LocalDate fechaCreacion;

    // Constructores
    public Pago() {
        this.fechaCreacion = LocalDate.now();
        this.fechaPago = LocalDate.now();
        this.distribuido = false;
        this.estadoPago = EstadoPago.PENDIENTE; // Valor por defecto
    }

    public Pago(CuotaAlquiler cuota, BigDecimal monto, MetodoPago metodoPago) {
        this();
        this.cuota = cuota;
        this.monto = monto;
        this.metodoPago = metodoPago;
        calcularDistribucion();

        // Si es efectivo, confirmar automáticamente
        if (metodoPago == MetodoPago.EFECTIVO) {
            this.estadoPago = EstadoPago.EFECTIVO_CONFIRMADO;
            this.distribuido = true;
            this.fechaDistribucion = LocalDate.now();
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CuotaAlquiler getCuota() { return cuota; }
    public void setCuota(CuotaAlquiler cuota) {
        this.cuota = cuota;
        calcularDistribucion();
    }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
        calcularDistribucion();
    }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    // NUEVO: Getter y Setter para estadoPago
    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;

        // Si se confirma el pago, marcar como distribuido
        if (estadoPago == EstadoPago.CONFIRMADO || estadoPago == EstadoPago.EFECTIVO_CONFIRMADO) {
            this.distribuido = true;
            if (this.fechaDistribucion == null) {
                this.fechaDistribucion = LocalDate.now();
            }
        }
    }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public BigDecimal getMora() { return mora; }
    public void setMora(BigDecimal mora) { this.mora = mora; }

    public BigDecimal getDescuentos() { return descuentos; }
    public void setDescuentos(BigDecimal descuentos) { this.descuentos = descuentos; }

    public BigDecimal getComisionCorredor() { return comisionCorredor; }
    public void setComisionCorredor(BigDecimal comisionCorredor) { this.comisionCorredor = comisionCorredor; }

    public BigDecimal getMontoLocador() { return montoLocador; }
    public void setMontoLocador(BigDecimal montoLocador) { this.montoLocador = montoLocador; }

    public Boolean getDistribuido() { return distribuido; }
    public void setDistribuido(Boolean distribuido) {
        this.distribuido = distribuido;
        if (distribuido && this.fechaDistribucion == null) {
            this.fechaDistribucion = LocalDate.now();
        }
    }

    public LocalDate getFechaDistribucion() { return fechaDistribucion; }
    public void setFechaDistribucion(LocalDate fechaDistribucion) { this.fechaDistribucion = fechaDistribucion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    // Métodos de negocio
    private void calcularDistribucion() {
        if (this.monto != null) {
            // 10% para el corredor, 90% para el locador
            this.comisionCorredor = this.monto.multiply(new BigDecimal("0.10"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            this.montoLocador = this.monto.subtract(this.comisionCorredor)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    public void distribuirFondos() {
        this.distribuido = true;
        this.fechaDistribucion = LocalDate.now();
    }

    // Método para confirmar transferencia (llamado por el corredor)
    public void confirmarTransferencia(String comprobante) {
        this.estadoPago = EstadoPago.CONFIRMADO;
        this.distribuido = true;
        this.fechaDistribucion = LocalDate.now();
        if (comprobante != null && !comprobante.trim().isEmpty()) {
            this.observaciones = (this.observaciones != null ? this.observaciones + " | " : "") +
                    "Comprobante: " + comprobante;
        }
    }

    // Método para rechazar transferencia
    public void rechazarTransferencia(String motivo) {
        this.estadoPago = EstadoPago.RECHAZADO;
        this.distribuido = false;
        this.observaciones = "RECHAZADO - " + motivo;
    }

    public String getResumenDistribucion() {
        return String.format("Corredor: $%s | Locador: $%s | Estado: %s",
                comisionCorredor != null ? comisionCorredor.toPlainString() : "0",
                montoLocador != null ? montoLocador.toPlainString() : "0",
                estadoPago != null ? estadoPago.toString() : "PENDIENTE");
    }

    @Override
    public String toString() {
        return "Pago{" +
                "id=" + id +
                ", monto=" + monto +
                ", metodoPago=" + metodoPago +
                ", estadoPago=" + estadoPago +
                ", fechaPago=" + fechaPago +
                ", distribuido=" + distribuido +
                '}';
    }
}