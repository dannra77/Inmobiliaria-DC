package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "propiedades")
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo; // Casa, Departamento, Oficina, etc.

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String direccion;
    private String localidad;
    private String provincia;
    private String pais;
    private String codigoPostal;

    // Superficies
    private BigDecimal superficieTotal;
    private BigDecimal superficieCubierta;

    // Ambientes
    private Integer ambientes;
    private Integer dormitorios;
    private Integer banios;
    private Integer antiguedad; // en años

    // Características booleanas
    private Boolean cochera = false;
    private Boolean patio = false;
    private Boolean jardin = false;
    private Boolean pileta = false;
    private Boolean terraza = false;
    private Boolean amoblado = false;

    // Precios
    private BigDecimal precioVenta;
    private BigDecimal precioAlquiler;

    // Estado
    private Boolean enVenta = false;
    private Boolean enAlquiler = false;
    private Boolean reservado = false;
    private String estado; // Disponible, Vendida, Alquilada, etc.

    // Georreferenciación
    private BigDecimal latitud;
    private BigDecimal longitud;

    // Relación con el propietario (Persona)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id")
    private Persona propietario;

    private String observaciones;

    // Para almacenar URLs de imágenes (separadas por coma)
    @Column(columnDefinition = "TEXT")
    private String imagenes;

    // Auditoría
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;

    // Relación con contratos (para el futuro)
    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContratoAlquiler> contratos = new ArrayList<>();

    // Constructores
    public Propiedad() {
        this.fechaCreacion = LocalDate.now();
        this.fechaModificacion = LocalDate.now();
    }

    public Propiedad(String tipo, String titulo, String direccion, Persona propietario) {
        this();
        this.tipo = tipo;
        this.titulo = titulo;
        this.direccion = direccion;
        this.propietario = propietario;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) {
        this.id = id;
        this.actualizarFechaModificacion();
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) {
        this.tipo = tipo;
        this.actualizarFechaModificacion();
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
        this.actualizarFechaModificacion();
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        this.actualizarFechaModificacion();
    }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
        this.actualizarFechaModificacion();
    }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) {
        this.localidad = localidad;
        this.actualizarFechaModificacion();
    }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) {
        this.provincia = provincia;
        this.actualizarFechaModificacion();
    }

    public String getPais() { return pais; }
    public void setPais(String pais) {
        this.pais = pais;
        this.actualizarFechaModificacion();
    }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getSuperficieTotal() { return superficieTotal; }
    public void setSuperficieTotal(BigDecimal superficieTotal) {
        this.superficieTotal = superficieTotal;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getSuperficieCubierta() { return superficieCubierta; }
    public void setSuperficieCubierta(BigDecimal superficieCubierta) {
        this.superficieCubierta = superficieCubierta;
        this.actualizarFechaModificacion();
    }

    public Integer getAmbientes() { return ambientes; }
    public void setAmbientes(Integer ambientes) {
        this.ambientes = ambientes;
        this.actualizarFechaModificacion();
    }

    public Integer getDormitorios() { return dormitorios; }
    public void setDormitorios(Integer dormitorios) {
        this.dormitorios = dormitorios;
        this.actualizarFechaModificacion();
    }

    public Integer getBanios() { return banios; }
    public void setBanios(Integer banios) {
        this.banios = banios;
        this.actualizarFechaModificacion();
    }

    public Integer getAntiguedad() { return antiguedad; }
    public void setAntiguedad(Integer antiguedad) {
        this.antiguedad = antiguedad;
        this.actualizarFechaModificacion();
    }

    public Boolean getCochera() { return cochera; }
    public void setCochera(Boolean cochera) {
        this.cochera = cochera;
        this.actualizarFechaModificacion();
    }

    public Boolean getPatio() { return patio; }
    public void setPatio(Boolean patio) {
        this.patio = patio;
        this.actualizarFechaModificacion();
    }

    public Boolean getJardin() { return jardin; }
    public void setJardin(Boolean jardin) {
        this.jardin = jardin;
        this.actualizarFechaModificacion();
    }

    public Boolean getPileta() { return pileta; }
    public void setPileta(Boolean pileta) {
        this.pileta = pileta;
        this.actualizarFechaModificacion();
    }

    public Boolean getTerraza() { return terraza; }
    public void setTerraza(Boolean terraza) {
        this.terraza = terraza;
        this.actualizarFechaModificacion();
    }

    public Boolean getAmoblado() { return amoblado; }
    public void setAmoblado(Boolean amoblado) {
        this.amoblado = amoblado;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getPrecioAlquiler() { return precioAlquiler; }
    public void setPrecioAlquiler(BigDecimal precioAlquiler) {
        this.precioAlquiler = precioAlquiler;
        this.actualizarFechaModificacion();
    }

    public Boolean getEnVenta() { return enVenta; }
    public void setEnVenta(Boolean enVenta) {
        this.enVenta = enVenta;
        this.actualizarFechaModificacion();
    }

    public Boolean getEnAlquiler() { return enAlquiler; }
    public void setEnAlquiler(Boolean enAlquiler) {
        this.enAlquiler = enAlquiler;
        this.actualizarFechaModificacion();
    }

    public Boolean getReservado() { return reservado; }
    public void setReservado(Boolean reservado) {
        this.reservado = reservado;
        this.actualizarFechaModificacion();
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) {
        this.estado = estado;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
        this.actualizarFechaModificacion();
    }

    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
        this.actualizarFechaModificacion();
    }

    public Persona getPropietario() { return propietario; }
    public void setPropietario(Persona propietario) {
        this.propietario = propietario;
        this.actualizarFechaModificacion();
    }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
        this.actualizarFechaModificacion();
    }

    public String getImagenes() { return imagenes; }
    public void setImagenes(String imagenes) {
        this.imagenes = imagenes;
        this.actualizarFechaModificacion();
    }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDate getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDate fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public List<ContratoAlquiler> getContratos() { return contratos; }
    public void setContratos(List<ContratoAlquiler> contratos) { this.contratos = contratos; }

    // Métodos de negocio
    private void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDate.now();
    }

    public String getDireccionCompleta() {
        return String.format("%s, %s, %s", direccion, localidad, provincia);
    }

    public boolean isDisponible() {
        return !reservado && ("Disponible".equals(estado) || estado == null);
    }

    public List<String> getListaImagenes() {
        if (imagenes == null || imagenes.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(imagenes.split("\\s*,\\s*"));
    }

    public void agregarImagen(String urlImagen) {
        if (this.imagenes == null) {
            this.imagenes = urlImagen;
        } else {
            this.imagenes += "," + urlImagen;
        }
        this.actualizarFechaModificacion();
    }

    @Override
    public String toString() {
        return "Propiedad{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", direccion='" + direccion + '\'' +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}