package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.Propiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {

    // Búsquedas por características
    List<Propiedad> findByTipo(String tipo);
    List<Propiedad> findByLocalidad(String localidad);
    List<Propiedad> findByProvincia(String provincia);
    List<Propiedad> findByEstado(String estado);
    List<Propiedad> findByEnAlquilerTrue();
    List<Propiedad> findByEnVentaTrue();
    List<Propiedad> findByReservadoFalse();

    // Búsqueda por propietario
    List<Propiedad> findByPropietarioId(Long propietarioId);

    // Búsquedas por rango de precios
    List<Propiedad> findByPrecioAlquilerBetween(BigDecimal min, BigDecimal max);
    List<Propiedad> findByPrecioVentaBetween(BigDecimal min, BigDecimal max);

    // Búsqueda avanzada
    @Query("SELECT p FROM Propiedad p WHERE " +
            "LOWER(p.titulo) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(p.direccion) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(p.localidad) LIKE LOWER(CONCAT('%', :criterio, '%'))")
    List<Propiedad> buscarPorCriterio(@Param("criterio") String criterio);

    // Propiedades disponibles para alquiler
    @Query("SELECT p FROM Propiedad p WHERE p.enAlquiler = true AND p.reservado = false AND p.estado = 'Disponible'")
    List<Propiedad> findDisponiblesParaAlquiler();

    // Contar propiedades por tipo
    @Query("SELECT p.tipo, COUNT(p) FROM Propiedad p GROUP BY p.tipo")
    List<Object[]> contarPorTipo();

    // Ordenamiento
    List<Propiedad> findAllByOrderByTituloAsc();
    List<Propiedad> findAllByOrderByFechaCreacionDesc();
}