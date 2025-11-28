package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.Pago;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.MetodoPago;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    // ✅ Métodos existentes
    List<Pago> findByMetodoPagoAndEstadoPago(MetodoPago metodoPago, EstadoPago estadoPago);
    List<Pago> findByEstadoPago(EstadoPago estadoPago);
    List<Pago> findByMetodoPago(MetodoPago metodoPago);
    List<Pago> findByFechaPagoBetween(LocalDate inicio, LocalDate fin);

    // ✅ NUEVO: Sumar comisiones del corredor (para CorredorController)
    @Query("SELECT SUM(p.comisionCorredor) FROM Pago p WHERE p.distribuido = true")
    BigDecimal sumComisionesCorredor();

    // ✅ NUEVO: Sumar comisiones por método de pago
    @Query("SELECT SUM(p.comisionCorredor) FROM Pago p WHERE p.metodoPago = :metodo AND p.distribuido = true")
    BigDecimal sumComisionesByMetodoPago(@Param("metodo") MetodoPago metodo);

    // ✅ NUEVO: Contar pagos por método
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.metodoPago = :metodo")
    Long countByMetodoPago(@Param("metodo") MetodoPago metodo);

    @Query("SELECT SUM(p.comisionCorredor) FROM Pago p WHERE p.estadoPago IN :estados AND p.fechaPago BETWEEN :inicio AND :fin")
    BigDecimal sumComisionesByEstadoPagoAndFechaBetween(
            @Param("estados") List<EstadoPago> estados,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(p) FROM Pago p WHERE p.metodoPago = :metodo AND p.estadoPago IN :estados AND p.fechaPago BETWEEN :inicio AND :fin")
    Long countByMetodoPagoAndEstadoPagoInAndFechaPagoBetween(
            @Param("metodo") MetodoPago metodo,
            @Param("estados") List<EstadoPago> estados,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin);

    // ✅ NUEVO: Buscar pagos por locatario
    @Query("SELECT p FROM Pago p WHERE p.cuota.contrato.locatario.email = :email")
    List<Pago> findByLocatarioEmail(@Param("email") String email);

    // ✅ Método para transferencias pendientes
    default List<Pago> findTransferenciasPendientes() {
        return findByMetodoPagoAndEstadoPago(MetodoPago.TRANSFERENCIA, EstadoPago.PENDIENTE);
    }
}