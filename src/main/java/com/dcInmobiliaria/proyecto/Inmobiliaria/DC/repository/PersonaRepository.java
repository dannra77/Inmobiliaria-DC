package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    // Búsquedas exactas
    Optional<Persona> findByDni(String dni);
    Optional<Persona> findByEmail(String email);
    boolean existsByDni(String dni);
    boolean existsByEmail(String email);

    // Búsquedas por campos individuales (case insensitive)
    List<Persona> findByNombreContainingIgnoreCase(String nombre);
    List<Persona> findByApellidoContainingIgnoreCase(String apellido);
    List<Persona> findByLocalidadContainingIgnoreCase(String localidad);
    List<Persona> findByProfesionContainingIgnoreCase(String profesion);

    // Búsquedas combinadas
    List<Persona> findByNombreContainingIgnoreCaseAndApellidoContainingIgnoreCase(String nombre, String apellido);
    List<Persona> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);

    // Búsqueda por rango de fechas
    List<Persona> findByFechaAltaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<Persona> findByFechaNacimientoBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // Búsqueda general por múltiples campos (CORREGIDO)
    @Query("SELECT p FROM Persona p WHERE " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(p.dni) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(p.localidad) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "LOWER(p.profesion) LIKE LOWER(CONCAT('%', :criterio, '%'))")
    List<Persona> buscarPorCriterio(@Param("criterio") String criterio);

    // Búsqueda por nombre O apellido O DNI (método derivado CORREGIDO)
    List<Persona> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrDniContaining(
            @Param("nombre") String nombre,
            @Param("apellido") String apellido,
            @Param("dni") String dni);

    // Consultas útiles para estadísticas
    @Query("SELECT COUNT(p) FROM Persona p")
    long countTotalPersonas();

    @Query("SELECT p.localidad, COUNT(p) FROM Persona p GROUP BY p.localidad ORDER BY COUNT(p) DESC")
    List<Object[]> contarPersonasPorLocalidad();

    @Query("SELECT p.profesion, COUNT(p) FROM Persona p WHERE p.profesion IS NOT NULL GROUP BY p.profesion ORDER BY COUNT(p) DESC")
    List<Object[]> contarPersonasPorProfesion();

    // Personas sin email registrado
    List<Persona> findByEmailIsNull();

    // Personas con fecha de nacimiento en un mes específico
    @Query("SELECT p FROM Persona p WHERE MONTH(p.fechaNacimiento) = :mes")
    List<Persona> findByMesNacimiento(@Param("mes") int mes);

    // Ordenamientos útiles
    List<Persona> findAllByOrderByApellidoAscNombreAsc();
    List<Persona> findByOrderByFechaAltaDesc();
}