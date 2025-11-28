package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "contratos_alquiler")
public class ContratoAlquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_contrato", unique = true, length = 20)
    private String numeroContrato;

    @Column(name = "modelo_contrato", nullable = false)
    private Integer modeloContrato = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    // Campos para participantes principales
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id")
    private Persona solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locador_id", nullable = false)
    private Persona locador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locatario_id", nullable = false)
    private Persona locatario;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "duracion_meses", nullable = false)
    private Integer duracionMeses;

    @Column(name = "monto_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoMensual;

    @Column(name = "monto_expensas", precision = 10, scale = 2)
    private BigDecimal montoExpensas;

    @Enumerated(EnumType.STRING)
    @Column(name = "expensas_option", nullable = false)
    private ExpensasOption expensasOption = ExpensasOption.SI;

    @Column(name = "dia_vencimiento")
    private Integer diaVencimiento = 1;

    @Column(name = "porcentaje_mora", precision = 5, scale = 2)
    private BigDecimal porcentajeMora = new BigDecimal("5.00");

    @Enumerated(EnumType.STRING)
    @Column(name = "lugar_pago")
    private LugarPago lugarPago = LugarPago.INMOBILIARIA_DC;

    @Column(name = "lugar_pago_otro")
    private String lugarPagoOtro;

    @Enumerated(EnumType.STRING)
    @Column(name = "lugar_administracion")
    private LugarAdministracion lugarAdministracion = LugarAdministracion.INMOBILIARIA_DC;

    @Column(name = "lugar_administracion_otro")
    private String lugarAdministracionOtro;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_indice", nullable = false)
    private TipoIndice tipoIndice = TipoIndice.ICL;

    @Column(name = "frecuencia_meses", nullable = false)
    private Integer frecuenciaMeses = 6;

    @Enumerated(EnumType.STRING)
    @Column(name = "responsable_expensas", nullable = false)
    private ResponsableServicio responsableExpensas = ResponsableServicio.LOCADOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "tgi", nullable = false)
    private ResponsableServicio tgi = ResponsableServicio.LOCADOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "seguro", nullable = false)
    private ResponsableServicio seguro = ResponsableServicio.LOCADOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "gas", nullable = false)
    private ResponsableServicio gas = ResponsableServicio.LOCADOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "agua", nullable = false)
    private ResponsableServicio agua = ResponsableServicio.LOCADOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoContrato estado = EstadoContrato.BORRADOR;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDate fechaModificacion;

    // NUEVOS CAMPOS PARA CONTRATOS EXISTENTES
    @Column(name = "fecha_inicio_original")
    private LocalDate fechaInicioOriginal;

    @Column(name = "fecha_inicio_administracion")
    private LocalDate fechaInicioAdministracion;

    @Column(name = "monto_inicial", precision = 10, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "monto_actual", precision = 10, scale = 2)
    private BigDecimal montoActual;

    @Column(name = "es_contrato_existente")
    private Boolean esContratoExistente = false;

    // NUEVOS CAMPOS PARA INFORMACIÓN DE VEHÍCULO (COCHERAS)
    @Column(name = "vehiculo_marca", length = 50)
    private String vehiculoMarca;

    @Column(name = "vehiculo_modelo", length = 50)
    private String vehiculoModelo;

    @Column(name = "vehiculo_color", length = 30)
    private String vehiculoColor;

    @Column(name = "vehiculo_patente", length = 15)
    private String vehiculoPatente;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ParticipacionContrato> participaciones = new ArrayList<>();

    public enum ExpensasOption {
        SI, NO
    }

    // Constructores
    public ContratoAlquiler() {
        this.fechaCreacion = LocalDate.now();
        this.fechaModificacion = LocalDate.now();
        this.numeroContrato = generarNumeroContratoTemporal();
        this.duracionMeses = 12;
        this.diaVencimiento = 1;
        this.porcentajeMora = new BigDecimal("5.00");
        this.modeloContrato = 1;
        this.expensasOption = ExpensasOption.SI;
        this.frecuenciaMeses = 6;
        this.esContratoExistente = false;
    }

    public ContratoAlquiler(Propiedad propiedad, Persona locador, Persona locatario,
                            LocalDate fechaInicio, LocalDate fechaFin, BigDecimal montoMensual) {
        this();
        this.propiedad = Objects.requireNonNull(propiedad, "La propiedad no puede ser nula");
        this.locador = Objects.requireNonNull(locador, "El locador no puede ser nulo");
        this.locatario = Objects.requireNonNull(locatario, "El locatario no puede ser nulo");
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "La fecha de inicio no puede ser nula");
        this.fechaFin = Objects.requireNonNull(fechaFin, "La fecha de fin no puede ser nula");
        this.montoMensual = Objects.requireNonNull(montoMensual, "El monto mensual no puede ser nulo");
        this.montoActual = montoMensual;
        this.calcularDuracionAutomatica();
        validarFechas();
    }

    // En la clase ContratoAlquiler
    public LocalDate getFechaInicioParaCuotas() {
        if (Boolean.TRUE.equals(esContratoExistente) && fechaInicioAdministracion != null) {
            return fechaInicioAdministracion;
        }
        return fechaInicio;
    }

    public String getTipoContratoDescripcion() {
        return Boolean.TRUE.equals(esContratoExistente) ? "Contrato Existente" : "Contrato Nuevo";
    }

    public boolean requiereCamposExistente() {
        return Boolean.TRUE.equals(esContratoExistente);
    }

    // GETTERS Y SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; this.actualizarFechaModificacion(); }

    public String getNumeroContrato() { return numeroContrato; }
    public void setNumeroContrato(String numeroContrato) { this.numeroContrato = numeroContrato; this.actualizarFechaModificacion(); }

    public Integer getModeloContrato() { return modeloContrato; }
    public void setModeloContrato(Integer modeloContrato) { this.modeloContrato = modeloContrato; this.actualizarFechaModificacion(); }

    public Propiedad getPropiedad() { return propiedad; }
    public void setPropiedad(Propiedad propiedad) { this.propiedad = Objects.requireNonNull(propiedad, "La propiedad no puede ser nula"); this.actualizarFechaModificacion(); }

    public Persona getSolicitante() { return solicitante; }
    public void setSolicitante(Persona solicitante) { this.solicitante = solicitante; this.actualizarFechaModificacion(); }

    public Persona getLocador() { return locador; }
    public void setLocador(Persona locador) { this.locador = Objects.requireNonNull(locador, "El locador no puede ser nulo"); this.actualizarFechaModificacion(); }

    public Persona getLocatario() { return locatario; }
    public void setLocatario(Persona locatario) { this.locatario = Objects.requireNonNull(locatario, "El locatario no puede ser nulo"); this.actualizarFechaModificacion(); }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = Objects.requireNonNull(fechaInicio, "La fecha de inicio no puede ser nula"); this.calcularDuracionAutomatica(); this.actualizarFechaModificacion(); }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = Objects.requireNonNull(fechaFin, "La fecha de fin no puede ser nula"); this.calcularDuracionAutomatica(); this.actualizarFechaModificacion(); }

    public Integer getDuracionMeses() { return duracionMeses; }
    public void setDuracionMeses(Integer duracionMeses) { this.duracionMeses = duracionMeses; this.actualizarFechaModificacion(); }

    public BigDecimal getMontoMensual() { return montoMensual; }
    public void setMontoMensual(BigDecimal montoMensual) { this.montoMensual = Objects.requireNonNull(montoMensual, "El monto mensual no puede ser nulo"); this.actualizarFechaModificacion(); }

    public BigDecimal getMontoExpensas() { return montoExpensas; }
    public void setMontoExpensas(BigDecimal montoExpensas) { this.montoExpensas = montoExpensas; this.actualizarFechaModificacion(); }

    public ExpensasOption getExpensasOption() { return expensasOption; }
    public void setExpensasOption(ExpensasOption expensasOption) { this.expensasOption = expensasOption; this.actualizarFechaModificacion(); }

    public Integer getDiaVencimiento() { return diaVencimiento; }
    public void setDiaVencimiento(Integer diaVencimiento) { this.diaVencimiento = diaVencimiento; this.actualizarFechaModificacion(); }

    public BigDecimal getPorcentajeMora() { return porcentajeMora; }
    public void setPorcentajeMora(BigDecimal porcentajeMora) { this.porcentajeMora = porcentajeMora; this.actualizarFechaModificacion(); }

    public LugarPago getLugarPago() { return lugarPago; }
    public void setLugarPago(LugarPago lugarPago) { this.lugarPago = lugarPago; this.actualizarFechaModificacion(); }

    public String getLugarPagoOtro() { return lugarPagoOtro; }
    public void setLugarPagoOtro(String lugarPagoOtro) { this.lugarPagoOtro = lugarPagoOtro; this.actualizarFechaModificacion(); }

    public LugarAdministracion getLugarAdministracion() { return lugarAdministracion; }
    public void setLugarAdministracion(LugarAdministracion lugarAdministracion) { this.lugarAdministracion = lugarAdministracion; this.actualizarFechaModificacion(); }

    public String getLugarAdministracionOtro() { return lugarAdministracionOtro; }
    public void setLugarAdministracionOtro(String lugarAdministracionOtro) { this.lugarAdministracionOtro = lugarAdministracionOtro; this.actualizarFechaModificacion(); }

    public TipoIndice getTipoIndice() { return tipoIndice; }
    public void setTipoIndice(TipoIndice tipoIndice) { this.tipoIndice = Objects.requireNonNull(tipoIndice, "El tipo de índice no puede ser nulo"); this.actualizarFechaModificacion(); }

    public Integer getFrecuenciaMeses() { return frecuenciaMeses; }
    public void setFrecuenciaMeses(Integer frecuenciaMeses) { this.frecuenciaMeses = frecuenciaMeses; this.actualizarFechaModificacion(); }

    public ResponsableServicio getResponsableExpensas() { return responsableExpensas; }
    public void setResponsableExpensas(ResponsableServicio responsableExpensas) { this.responsableExpensas = Objects.requireNonNull(responsableExpensas, "El responsable de expensas no puede ser nulo"); this.actualizarFechaModificacion(); }

    public ResponsableServicio getTgi() { return tgi; }
    public void setTgi(ResponsableServicio tgi) { this.tgi = Objects.requireNonNull(tgi, "El responsable de TGI no puede ser nulo"); this.actualizarFechaModificacion(); }

    public ResponsableServicio getSeguro() { return seguro; }
    public void setSeguro(ResponsableServicio seguro) { this.seguro = Objects.requireNonNull(seguro, "El responsable del seguro no puede ser nulo"); this.actualizarFechaModificacion(); }

    public ResponsableServicio getGas() { return gas; }
    public void setGas(ResponsableServicio gas) { this.gas = Objects.requireNonNull(gas, "El responsable del gas no puede ser nulo"); this.actualizarFechaModificacion(); }

    public ResponsableServicio getAgua() { return agua; }
    public void setAgua(ResponsableServicio agua) { this.agua = Objects.requireNonNull(agua, "El responsable del agua no puede ser nulo"); this.actualizarFechaModificacion(); }

    public EstadoContrato getEstado() { return estado; }
    public void setEstado(EstadoContrato estado) { this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo"); this.actualizarFechaModificacion(); }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; this.actualizarFechaModificacion(); }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "La fecha de creación no puede ser nula"); }

    public LocalDate getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDate fechaModificacion) { this.fechaModificacion = Objects.requireNonNull(fechaModificacion, "La fecha de modificación no puede ser nula"); }

    // NUEVOS GETTERS Y SETTERS
    public LocalDate getFechaInicioOriginal() { return fechaInicioOriginal; }
    public void setFechaInicioOriginal(LocalDate fechaInicioOriginal) { this.fechaInicioOriginal = fechaInicioOriginal; this.actualizarFechaModificacion(); }

    public LocalDate getFechaInicioAdministracion() { return fechaInicioAdministracion; }
    public void setFechaInicioAdministracion(LocalDate fechaInicioAdministracion) { this.fechaInicioAdministracion = fechaInicioAdministracion; this.actualizarFechaModificacion(); }

    public BigDecimal getMontoInicial() { return montoInicial; }
    public void setMontoInicial(BigDecimal montoInicial) { this.montoInicial = montoInicial; this.actualizarFechaModificacion(); }

    public BigDecimal getMontoActual() { return montoActual; }
    public void setMontoActual(BigDecimal montoActual) { this.montoActual = montoActual; this.actualizarFechaModificacion(); }

    public Boolean getEsContratoExistente() { return esContratoExistente; }
    public void setEsContratoExistente(Boolean esContratoExistente) { this.esContratoExistente = esContratoExistente; this.actualizarFechaModificacion(); }

    public String getVehiculoMarca() { return vehiculoMarca; }
    public void setVehiculoMarca(String vehiculoMarca) { this.vehiculoMarca = vehiculoMarca; this.actualizarFechaModificacion(); }

    public String getVehiculoModelo() { return vehiculoModelo; }
    public void setVehiculoModelo(String vehiculoModelo) { this.vehiculoModelo = vehiculoModelo; this.actualizarFechaModificacion(); }

    public String getVehiculoColor() { return vehiculoColor; }
    public void setVehiculoColor(String vehiculoColor) { this.vehiculoColor = vehiculoColor; this.actualizarFechaModificacion(); }

    public String getVehiculoPatente() { return vehiculoPatente; }
    public void setVehiculoPatente(String vehiculoPatente) { this.vehiculoPatente = vehiculoPatente; this.actualizarFechaModificacion(); }

    public List<ParticipacionContrato> getParticipaciones() { return new ArrayList<>(participaciones); }
    public void setParticipaciones(List<ParticipacionContrato> participaciones) {
        this.participaciones.clear();
        if (participaciones != null) {
            this.participaciones.addAll(participaciones);
        }
        this.actualizarFechaModificacion();
    }

    // MÉTODOS DE NEGOCIO
    private String generarNumeroContratoTemporal() {
        return "TEMP-" + System.currentTimeMillis();
    }

    public void calcularDuracionAutomatica() {
        if (this.fechaInicio != null && this.fechaFin != null) {
            long meses = java.time.temporal.ChronoUnit.MONTHS.between(this.fechaInicio, this.fechaFin);
            this.duracionMeses = Math.max(1, (int) meses);
        }
    }

    private void validarFechas() {
        if (this.fechaInicio != null && this.fechaFin != null && !this.fechaFin.isAfter(this.fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    private void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDate.now();
    }

    public void limpiarParticipantes() {
        if (this.participaciones != null) {
            this.participaciones.clear();
            this.actualizarFechaModificacion();
        }
    }

    public void agregarParticipante(Persona persona, RolContrato rol) {
        if (persona != null && rol != null) {
            ParticipacionContrato participacion = new ParticipacionContrato(persona, this, rol);
            this.participaciones.add(participacion);
            this.actualizarFechaModificacion();
        }
    }

    public boolean isVigente() {
        LocalDate hoy = LocalDate.now();
        return estado == EstadoContrato.ACTIVO &&
                (hoy.isEqual(fechaInicio) || hoy.isAfter(fechaInicio)) &&
                (hoy.isEqual(fechaFin) || hoy.isBefore(fechaFin));
    }

    public boolean isPorVencer() {
        if (!isVigente()) return false;
        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaFin);
        return diasRestantes <= 30 && diasRestantes > 0;
    }

    // NUEVO MÉTODO PARA VERIFICAR SI ES COCHERA
    public boolean esCochera() {
        return propiedad != null && propiedad.getTipo() != null &&
                propiedad.getTipo().toLowerCase().contains("cochera");
    }

    // NUEVO MÉTODO PARA OBTENER INFORMACIÓN DEL VEHÍCULO
    public String getInfoVehiculoCompleta() {
        if (vehiculoMarca == null && vehiculoModelo == null &&
                vehiculoColor == null && vehiculoPatente == null) {
            return "No especificado";
        }

        StringBuilder info = new StringBuilder();
        if (vehiculoMarca != null) info.append(vehiculoMarca);
        if (vehiculoModelo != null) info.append(" ").append(vehiculoModelo);
        if (vehiculoColor != null) info.append(" - ").append(vehiculoColor);
        if (vehiculoPatente != null) info.append(" (").append(vehiculoPatente).append(")");

        return info.toString();
    }

    // MÉTODOS PARA OBTENER PARTICIPANTES ESPECÍFICOS
    public List<Persona> getGarantes() {
        List<Persona> garantes = new ArrayList<>();
        if (participaciones != null) {
            for (ParticipacionContrato participacion : participaciones) {
                if (participacion.getRol() == RolContrato.GARANTE) {
                    garantes.add(participacion.getPersona());
                }
            }
        }
        return garantes;
    }

    public List<Persona> getHabitantes() {
        List<Persona> habitantes = new ArrayList<>();
        if (participaciones != null) {
            for (ParticipacionContrato participacion : participaciones) {
                if (participacion.getRol() == RolContrato.HABITANTE) {
                    habitantes.add(participacion.getPersona());
                }
            }
        }
        return habitantes;
    }

    public Persona getLocadorFromParticipaciones() {
        if (participaciones != null) {
            for (ParticipacionContrato participacion : participaciones) {
                if (participacion.getRol() == RolContrato.LOCADOR) {
                    return participacion.getPersona();
                }
            }
        }
        return null;
    }

    public Persona getLocatarioFromParticipaciones() {
        if (participaciones != null) {
            for (ParticipacionContrato participacion : participaciones) {
                if (participacion.getRol() == RolContrato.LOCATARIO) {
                    return participacion.getPersona();
                }
            }
        }
        return null;
    }

    public Persona getSolicitanteFromParticipaciones() {
        if (participaciones != null) {
            for (ParticipacionContrato participacion : participaciones) {
                if (participacion.getRol() == RolContrato.SOLICITANTE) {
                    return participacion.getPersona();
                }
            }
        }
        return null;
    }

    // MÉTODO PARA CALCULAR PRÓXIMO AJUSTE
    public LocalDate calcularProximoAjuste() {
        if (fechaInicioAdministracion != null) {
            return fechaInicioAdministracion.plusMonths(frecuenciaMeses);
        } else if (fechaInicio != null) {
            return fechaInicio.plusMonths(frecuenciaMeses);
        }
        return null;
    }

    // MÉTODO PARA VERIFICAR SI HAY AJUSTE PRÓXIMO
    public boolean isAjusteProximo() {
        LocalDate proximoAjuste = calcularProximoAjuste();
        if (proximoAjuste == null) return false;

        LocalDate hoy = LocalDate.now();
        LocalDate enUnMes = hoy.plusMonths(1);

        return (proximoAjuste.isAfter(hoy) || proximoAjuste.isEqual(hoy)) &&
                (proximoAjuste.isBefore(enUnMes) || proximoAjuste.isEqual(enUnMes));
    }

    // MÉTODO PARA APLICAR AJUSTE
    public void aplicarAjuste(BigDecimal nuevoMonto, BigDecimal porcentajeAjuste) {
        this.montoActual = nuevoMonto;
        this.actualizarFechaModificacion();

        // Aquí podrías agregar lógica para registrar el historial de ajustes
        System.out.println("Ajuste aplicado: " + porcentajeAjuste + "% - Nuevo monto: $" + nuevoMonto);
    }

    @Override
    public String toString() {
        return "ContratoAlquiler{" +
                "id=" + id +
                ", numeroContrato='" + numeroContrato + '\'' +
                ", propiedad=" + (propiedad != null ? propiedad.getTitulo() : "null") +
                ", estado=" + estado +
                ", esContratoExistente=" + esContratoExistente +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContratoAlquiler)) return false;
        ContratoAlquiler that = (ContratoAlquiler) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(numeroContrato, that.numeroContrato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numeroContrato);
    }
}