package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "personas")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    // DNI argentino: con length suficiente (ej. 20), no necesitamos 255
    @Column(unique = true, nullable = false, length = 20)
    private String dni;

    private String direccion;
    private String localidad;
    private String provincia;

    // NUEVO CAMPO: Nacionalidad
    private String nacionalidad;

    // Email puede ser largo, pero limitado a 191 para evitar error de índice
    @Column(unique = true, length = 191)
    private String email;

    private String telefono;
    private String cuitCuil;
    private LocalDate fechaNacimiento;
    private String lugarTrabajo;
    private String profesion;

    // CAMPO ACTUAL: Renombrado para claridad
    private String numeroCuentaBancaria;

    // NUEVOS CAMPOS BANCARIOS
    private String cajaAhorro;        // Número de caja de ahorro
    private String cbu;               // CBU (22 caracteres)
    private String alias;             // Alias bancario

    private String facebook;
    private String instagram;
    private String observaciones;

    private LocalDate fechaAlta;
    private LocalDate fechaModificacion;

    public Persona() {
        this.fechaAlta = LocalDate.now();
        this.fechaModificacion = LocalDate.now();
        this.nacionalidad = "Argentina"; // Valor por defecto
    }

    public Persona(String nombre, String apellido, String dni, String email) {
        this();
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.email = email;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; this.actualizarFechaModificacion(); }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; this.actualizarFechaModificacion(); }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; this.actualizarFechaModificacion(); }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; this.actualizarFechaModificacion(); }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; this.actualizarFechaModificacion(); }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; this.actualizarFechaModificacion(); }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; this.actualizarFechaModificacion(); }

    // Nacionalidad
    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
        this.actualizarFechaModificacion();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; this.actualizarFechaModificacion(); }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; this.actualizarFechaModificacion(); }

    public String getCuitCuil() { return cuitCuil; }
    public void setCuitCuil(String cuitCuil) { this.cuitCuil = cuitCuil; this.actualizarFechaModificacion(); }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; this.actualizarFechaModificacion(); }

    public String getLugarTrabajo() { return lugarTrabajo; }
    public void setLugarTrabajo(String lugarTrabajo) { this.lugarTrabajo = lugarTrabajo; this.actualizarFechaModificacion(); }

    public String getProfesion() { return profesion; }
    public void setProfesion(String profesion) { this.profesion = profesion; this.actualizarFechaModificacion(); }

    // Campos bancarios
    public String getNumeroCuentaBancaria() { return numeroCuentaBancaria; }
    public void setNumeroCuentaBancaria(String numeroCuentaBancaria) {
        this.numeroCuentaBancaria = numeroCuentaBancaria;
        this.actualizarFechaModificacion();
    }

    public String getCajaAhorro() { return cajaAhorro; }
    public void setCajaAhorro(String cajaAhorro) {
        this.cajaAhorro = cajaAhorro;
        this.actualizarFechaModificacion();
    }

    public String getCbu() { return cbu; }
    public void setCbu(String cbu) {
        this.cbu = cbu;
        this.actualizarFechaModificacion();
    }

    public String getAlias() { return alias; }
    public void setAlias(String alias) {
        this.alias = alias;
        this.actualizarFechaModificacion();
    }

    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; this.actualizarFechaModificacion(); }

    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; this.actualizarFechaModificacion(); }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; this.actualizarFechaModificacion(); }

    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }

    public LocalDate getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDate fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    // Métodos de negocio
    private void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDate.now();
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public int getEdad() {
        if (fechaNacimiento == null) return 0;
        return LocalDate.now().getYear() - fechaNacimiento.getYear();
    }

    // Método para obtener información bancaria completa
    public String getInformacionBancariaCompleta() {
        StringBuilder sb = new StringBuilder();
        if (cajaAhorro != null && !cajaAhorro.trim().isEmpty()) {
            sb.append("Caja Ahorro: ").append(cajaAhorro);
        }
        if (cbu != null && !cbu.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("CBU: ").append(cbu);
        }
        if (alias != null && !alias.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Alias: ").append(alias);
        }
        return sb.length() > 0 ? sb.toString() : "No especificada";
    }

    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", dni='" + dni + '\'' +
                ", nacionalidad='" + nacionalidad + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}