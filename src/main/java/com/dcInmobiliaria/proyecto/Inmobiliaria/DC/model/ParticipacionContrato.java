package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "participaciones_contrato",
        uniqueConstraints = @UniqueConstraint(columnNames = {"persona_id", "contrato_id", "rol"}))
public class ParticipacionContrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private ContratoAlquiler contrato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolContrato rol;

    private LocalDate fechaInicioParticipacion;
    private LocalDate fechaFinParticipacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private LocalDate fechaCreacion;

    private LocalDate fechaModificacion;

    // CONSTRUCTORES
    public ParticipacionContrato() {
        this.fechaCreacion = LocalDate.now();
        this.fechaModificacion = LocalDate.now();
    }

    public ParticipacionContrato(Persona persona, ContratoAlquiler contrato, RolContrato rol) {
        this();
        this.persona = persona;
        this.contrato = contrato;
        this.rol = rol;
        this.fechaInicioParticipacion = LocalDate.now();
    }

    public ParticipacionContrato(Persona persona, ContratoAlquiler contrato, RolContrato rol,
                                 LocalDate fechaInicio, String observaciones) {
        this(persona, contrato, rol);
        this.fechaInicioParticipacion = fechaInicio;
        this.observaciones = observaciones;
    }

    // GETTERS Y SETTERS
    public Long getId() { return id; }
    public void setId(Long id) {
        this.id = id;
        this.actualizarFechaModificacion();
    }

    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) {
        this.persona = persona;
        this.actualizarFechaModificacion();
    }

    public ContratoAlquiler getContrato() { return contrato; }
    public void setContrato(ContratoAlquiler contrato) {
        this.contrato = contrato;
        this.actualizarFechaModificacion();
    }

    public RolContrato getRol() { return rol; }
    public void setRol(RolContrato rol) {
        this.rol = rol;
        this.actualizarFechaModificacion();
    }

    public LocalDate getFechaInicioParticipacion() { return fechaInicioParticipacion; }
    public void setFechaInicioParticipacion(LocalDate fechaInicioParticipacion) {
        this.fechaInicioParticipacion = fechaInicioParticipacion;
        this.actualizarFechaModificacion();
    }

    public LocalDate getFechaFinParticipacion() { return fechaFinParticipacion; }
    public void setFechaFinParticipacion(LocalDate fechaFinParticipacion) {
        this.fechaFinParticipacion = fechaFinParticipacion;
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

    // MÉTODOS DE NEGOCIO
    private void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDate.now();
    }

    public boolean isActiva() {
        return fechaFinParticipacion == null || fechaFinParticipacion.isAfter(LocalDate.now());
    }

    public void finalizarParticipacion() {
        this.fechaFinParticipacion = LocalDate.now();
        this.actualizarFechaModificacion();
    }

    public void finalizarParticipacion(LocalDate fechaFin) {
        this.fechaFinParticipacion = fechaFin;
        this.actualizarFechaModificacion();
    }

    public void reactivarParticipacion() {
        this.fechaFinParticipacion = null;
        this.actualizarFechaModificacion();
    }

    public String getDescripcionRol() {
        return rol != null ? rol.getDescripcion() : "";
    }

    public String getNombrePersona() {
        return persona != null ? persona.getNombreCompleto() : "";
    }

    public String getIdentificacionContrato() {
        return contrato != null ? contrato.getNumeroContrato() : "";
    }

    // MÉTODOS DE CONVENIENCIA
    public boolean esLocador() {
        return rol == RolContrato.LOCADOR;
    }

    public boolean esLocatario() {
        return rol == RolContrato.LOCATARIO;
    }

    public boolean esGarante() {
        return rol == RolContrato.GARANTE;
    }

    public boolean esHabitante() {
        return rol == RolContrato.HABITANTE;
    }

    public boolean esSolicitante() {
        return rol == RolContrato.SOLICITANTE;
    }

    @Override
    public String toString() {
        return "ParticipacionContrato{" +
                "id=" + id +
                ", persona=" + (persona != null ? persona.getNombreCompleto() : "null") +
                ", contrato=" + (contrato != null ? contrato.getNumeroContrato() : "null") +
                ", rol=" + rol +
                ", fechaInicio=" + fechaInicioParticipacion +
                ", activa=" + isActiva() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipacionContrato)) return false;
        ParticipacionContrato that = (ParticipacionContrato) o;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        return persona != null && contrato != null && rol != null &&
                persona.equals(that.persona) &&
                contrato.equals(that.contrato) &&
                rol == that.rol;
    }

    @Override
    public int hashCode() {
        int result = persona != null ? persona.hashCode() : 0;
        result = 31 * result + (contrato != null ? contrato.hashCode() : 0);
        result = 31 * result + (rol != null ? rol.hashCode() : 0);
        return result;
    }
}