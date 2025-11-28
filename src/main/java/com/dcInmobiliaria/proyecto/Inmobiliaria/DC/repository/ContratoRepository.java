package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.ContratoAlquiler;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.EstadoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<ContratoAlquiler, Long> {

    // Contratos por propiedad
    List<ContratoAlquiler> findByPropiedadId(Long propiedadId);

    // Contratos por estado
    List<ContratoAlquiler> findByEstado(EstadoContrato estado);

    // Contratos vigentes
    @Query("SELECT c FROM ContratoAlquiler c WHERE c.estado = 'ACTIVO' AND c.fechaInicio <= :hoy AND c.fechaFin >= :hoy")
    List<ContratoAlquiler> findContratosVigentes(@Param("hoy") LocalDate hoy);

    // Contratos próximos a vencer (30 días)
    @Query("SELECT c FROM ContratoAlquiler c WHERE c.estado = 'ACTIVO' AND c.fechaFin BETWEEN :hoy AND :limite")
    List<ContratoAlquiler> findContratosProximosAVencer(@Param("hoy") LocalDate hoy, @Param("limite") LocalDate limite);

    // Contratos por locador (a través de participaciones)
    @Query("SELECT DISTINCT c FROM ContratoAlquiler c JOIN c.participaciones p WHERE p.persona.id = :personaId AND p.rol = 'LOCADOR'")
    List<ContratoAlquiler> findContratosComoLocador(@Param("personaId") Long personaId);

    // Contratos por locatario (a través de participaciones)
    @Query("SELECT DISTINCT c FROM ContratoAlquiler c JOIN c.participaciones p WHERE p.persona.id = :personaId AND p.rol = 'LOCATARIO'")
    List<ContratoAlquiler> findContratosComoLocatario(@Param("personaId") Long personaId);

    // Contratos que involucran a una persona (cualquier rol)
    @Query("SELECT DISTINCT c FROM ContratoAlquiler c JOIN c.participaciones p WHERE p.persona.id = :personaId")
    List<ContratoAlquiler> findContratosPorPersona(@Param("personaId") Long personaId);

    // Buscar por número de contrato
    @Query("SELECT c FROM ContratoAlquiler c WHERE c.id = :id")
    ContratoAlquiler findByNumeroContrato(@Param("id") Long id);

    // Estadísticas
    @Query("SELECT COUNT(c) FROM ContratoAlquiler c WHERE c.estado = 'ACTIVO'")
    Long countContratosActivos();

    @Query("SELECT c.estado, COUNT(c) FROM ContratoAlquiler c GROUP BY c.estado")
    List<Object[]> contarPorEstado();

    // Ordenamientos
    List<ContratoAlquiler> findAllByOrderByFechaCreacionDesc();
    List<ContratoAlquiler> findByOrderByFechaInicioAsc();
}
