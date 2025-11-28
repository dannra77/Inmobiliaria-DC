package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.MovimientoCorredor;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.Pago;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.EstadoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoCorredorRepository extends JpaRepository<MovimientoCorredor, Long> {

    List<MovimientoCorredor> findByEstado(EstadoMovimiento estado);

    List<MovimientoCorredor> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // ✅ NUEVO: Buscar movimiento por pago asociado
    Optional<MovimientoCorredor> findByPagoAsociado(Pago pago);

    List<MovimientoCorredor> findAllByOrderByFechaDesc();

    @Query("SELECT SUM(m.monto) FROM MovimientoCorredor m WHERE m.estado = 'CONFIRMADO' AND m.fecha BETWEEN :inicio AND :fin")
    BigDecimal sumIngresosConfirmadosByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // ✅ NUEVO: Contar por estado
    @Query("SELECT COUNT(m) FROM MovimientoCorredor m WHERE m.estado = :estado")
    Long countByEstado(@Param("estado") EstadoMovimiento estado);

    default Long countPendientes() {
        return countByEstado(EstadoMovimiento.PENDIENTE);
    }
}