package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.CuotaAlquiler;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuotaAlquilerRepository extends JpaRepository<CuotaAlquiler, Long> {

    // Buscar cuotas por contrato

    // En CuotaAlquilerRepository.java
    List<CuotaAlquiler> findByContratoLocatarioEmailAndEstado(String email, EstadoCuota estado);
    List<CuotaAlquiler> findByContratoId(Long contratoId);
    List<CuotaAlquiler> findByContratoIdOrderByNumeroCuota(Long contratoId);
    List<CuotaAlquiler> findByContratoIdOrderByFechaVencimiento(Long contratoId);

    // Buscar cuotas por estado
    List<CuotaAlquiler> findByEstado(EstadoCuota estado);
    List<CuotaAlquiler> findByContratoIdAndEstado(Long contratoId, EstadoCuota estado);

    // Buscar cuotas por mes y año
    List<CuotaAlquiler> findByMesAndAnio(Integer mes, Integer anio);

    // ✅ AGREGAR ESTE MÉTODO - Verificar si existe cuota para contrato, mes y año
    boolean existsByContratoIdAndMesAndAnio(Long contratoId, Integer mes, Integer anio);

    // Buscar cuotas vencidas
    @Query("SELECT c FROM CuotaAlquiler c WHERE c.estado = 'PENDIENTE' AND c.fechaVencimiento < :hoy")
    List<CuotaAlquiler> findCuotasVencidas(@Param("hoy") LocalDate hoy);

    // Buscar cuotas próximas a vencer (próximos 7 días)
    @Query("SELECT c FROM CuotaAlquiler c WHERE c.estado = 'PENDIENTE' AND c.fechaVencimiento BETWEEN :hoy AND :limite")
    List<CuotaAlquiler> findCuotasProximasAVencer(@Param("hoy") LocalDate hoy, @Param("limite") LocalDate limite);

    // Buscar cuota por número de cuota y contrato
    Optional<CuotaAlquiler> findByContratoIdAndNumeroCuota(Long contratoId, Integer numeroCuota);

    // Estadísticas
    @Query("SELECT COUNT(c) FROM CuotaAlquiler c WHERE c.contrato.id = :contratoId AND c.estado = 'PAGADA'")
    Long countCuotasPagadasByContrato(@Param("contratoId") Long contratoId);

    @Query("SELECT COUNT(c) FROM CuotaAlquiler c WHERE c.contrato.id = :contratoId AND c.estado = 'PENDIENTE'")
    Long countCuotasPendientesByContrato(@Param("contratoId") Long contratoId);

    @Query("SELECT SUM(c.monto) FROM CuotaAlquiler c WHERE c.contrato.id = :contratoId AND c.estado = 'PAGADA'")
    BigDecimal sumMontoPagadoByContrato(@Param("contratoId") Long contratoId);

    @Query("SELECT SUM(c.monto) FROM CuotaAlquiler c WHERE c.contrato.id = :contratoId AND c.estado = 'PENDIENTE'")
    BigDecimal sumMontoPendienteByContrato(@Param("contratoId") Long contratoId);
}