package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.service;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.*;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.PagoRepository;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.MovimientoCorredorRepository;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.CuotaAlquilerRepository;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.dto.DescuentoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private MovimientoCorredorRepository movimientoRepository;

    @Autowired
    private CuotaAlquilerRepository cuotaRepository;

    @Autowired
    private CuotaGeneracionService cuotaGeneracionService;

    /**
     * PROCESAR PAGO COMPLETO CON DISTRIBUCIÓN CORRECTA 90%/10%
     * El 10% del corredor es SIEMPRE sobre la cuota pura, sin importar descuentos
     */
    public Pago procesarPago(Long cuotaId, BigDecimal monto, MetodoPago metodoPago,
                             List<DescuentoDto> descuentos, String observaciones) {

        System.out.println("💰 INICIANDO PROCESAMIENTO DE PAGO - Cuota ID: " + cuotaId);

        // Obtener la cuota
        CuotaAlquiler cuota = cuotaGeneracionService.findById(cuotaId);
        System.out.println("📋 Cuota encontrada - Monto: $" + cuota.getMonto() + " - Contrato: " +
                cuota.getContrato().getNumeroContrato());

        // Crear pago
        Pago pago = new Pago();
        pago.setCuota(cuota);
        pago.setMonto(monto);
        pago.setMetodoPago(metodoPago);
        pago.setObservaciones(observaciones);
        pago.setNumeroFactura(generarNumeroFactura());

        // Procesar descuentos si existen
        BigDecimal totalDescuentos = BigDecimal.ZERO;
        if (descuentos != null && !descuentos.isEmpty()) {
            totalDescuentos = procesarDescuentos(pago, descuentos);
            System.out.println("🎯 Descuentos aplicados: $" + totalDescuentos);
        }

        // Calcular mora si existe
        if (cuota.getMora() != null && cuota.getMora().compareTo(BigDecimal.ZERO) > 0) {
            pago.setMora(cuota.getMora());
            System.out.println("⚠️ Mora aplicada: $" + cuota.getMora());
        }

        // CALCULAR DISTRIBUCIÓN CORRECTA 90%/10%
        calcularDistribucionCorrecta(pago, cuota);

        // Configurar estado según método de pago
        if (metodoPago == MetodoPago.EFECTIVO) {
            pago.setEstadoPago(EstadoPago.EFECTIVO_CONFIRMADO);
            pago.setDistribuido(true);
            pago.setFechaDistribucion(LocalDate.now());
            System.out.println("💵 Pago en EFECTIVO - Confirmado automáticamente");
        } else {
            pago.setEstadoPago(EstadoPago.PENDIENTE);
            pago.setDistribuido(false);
            System.out.println("🏦 Pago por TRANSFERENCIA - Pendiente de verificación");
        }

        // Marcar cuota como pagada
        cuota.setEstado(EstadoCuota.PAGADA);
        cuota.setFechaPago(LocalDate.now());
        cuota.setTotalPagado(monto);

        // Limpiar mora de la cuota una vez pagada
        cuota.setMora(BigDecimal.ZERO);

        cuotaRepository.save(cuota);
        System.out.println("✅ Cuota marcada como PAGADA");

        // Registrar movimiento para el corredor
        registrarMovimientoCorredor(pago);

        // Guardar pago
        Pago pagoGuardado = pagoRepository.save(pago);
        System.out.println("💾 Pago guardado exitosamente - ID: " + pagoGuardado.getId() +
                " - Factura: " + pagoGuardado.getNumeroFactura());

        return pagoGuardado;
    }

    /**
     * CALCULAR DISTRIBUCIÓN CORRECTA 90%/10%
     * El 10% del corredor es SIEMPRE sobre la cuota pura, sin importar descuentos
     */
    private void calcularDistribucionCorrecta(Pago pago, CuotaAlquiler cuota) {
        System.out.println("🧮 CALCULANDO DISTRIBUCIÓN 90%/10%");

        // 1. CUOTA PURA (sin expensas, sin mora, sin descuentos)
        BigDecimal cuotaPura = cuota.getMonto();
        System.out.println("📊 Cuota pura (base): $" + cuotaPura);

        // 2. CALCULAR 10% PARA EL CORREDOR SOBRE LA CUOTA PURA
        BigDecimal comisionCorredor = cuotaPura.multiply(new BigDecimal("0.10"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        System.out.println("👨‍💼 Comisión corredor (10% sobre cuota pura): $" + comisionCorredor);

        // 3. CALCULAR MONTO PARA EL LOCADOR
        // Locador recibe: (Monto total pagado - Comisión corredor)
        BigDecimal montoLocador = pago.getMonto().subtract(comisionCorredor)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // 4. VERIFICAR QUE EL MONTO DEL LOCADOR NO SEA NEGATIVO
        if (montoLocador.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("⚠️ Ajuste: Monto locador negativo, ajustando a $0");
            montoLocador = BigDecimal.ZERO;

            // Si el monto es menor que la comisión, ajustar la comisión
            comisionCorredor = pago.getMonto();
            System.out.println("🔄 Comisión corredor ajustada a: $" + comisionCorredor);
        }

        // 5. ESTABLECER VALORES EN EL PAGO
        pago.setComisionCorredor(comisionCorredor);
        pago.setMontoLocador(montoLocador);

        System.out.println("💰 DISTRIBUCIÓN FINAL:");
        System.out.println("   • Cuota pura: $" + cuotaPura);
        System.out.println("   • Monto total pagado: $" + pago.getMonto());
        System.out.println("   • Comisión corredor (10%): $" + comisionCorredor);
        System.out.println("   • Monto locador: $" + montoLocador);
        System.out.println("   • Descuentos: $" + (pago.getDescuentos() != null ? pago.getDescuentos() : "0"));
        System.out.println("   • Mora: $" + (pago.getMora() != null ? pago.getMora() : "0"));
    }

    /**
     * PROCESAR DESCUENTOS Y AGREGAR A OBSERVACIONES
     */
    private BigDecimal procesarDescuentos(Pago pago, List<DescuentoDto> descuentos) {
        BigDecimal totalDescuentos = descuentos.stream()
                .map(DescuentoDto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pago.setDescuentos(totalDescuentos);

        // Construir descripción de descuentos para observaciones
        StringBuilder descuentosDesc = new StringBuilder();
        descuentosDesc.append("Descuentos aplicados: ");

        for (int i = 0; i < descuentos.size(); i++) {
            DescuentoDto descuento = descuentos.get(i);
            descuentosDesc.append(descuento.getConcepto())
                    .append(": -$")
                    .append(descuento.getMonto());

            if (i < descuentos.size() - 1) {
                descuentosDesc.append("; ");
            }
        }

        // Agregar a observaciones existentes
        if (pago.getObservaciones() != null && !pago.getObservaciones().isEmpty()) {
            pago.setObservaciones(pago.getObservaciones() + " | " + descuentosDesc.toString());
        } else {
            pago.setObservaciones(descuentosDesc.toString());
        }

        return totalDescuentos;
    }

    /**
     * REGISTRAR MOVIMIENTO PARA EL CORREDOR
     */
    private void registrarMovimientoCorredor(Pago pago) {
        MovimientoCorredor movimiento = new MovimientoCorredor();
        movimiento.setTipo(TipoMovimiento.INGRESO);
        movimiento.setMonto(pago.getComisionCorredor());
        movimiento.setMetodo(pago.getMetodoPago());

        // Estado del movimiento según estado del pago
        if (pago.getEstadoPago() == EstadoPago.EFECTIVO_CONFIRMADO) {
            movimiento.setEstado(EstadoMovimiento.CONFIRMADO);
        } else if (pago.getEstadoPago() == EstadoPago.PENDIENTE) {
            movimiento.setEstado(EstadoMovimiento.PENDIENTE);
        } else {
            movimiento.setEstado(EstadoMovimiento.CONFIRMADO);
        }

        movimiento.setDescripcion("Comisión por alquiler - " +
                pago.getCuota().getContrato().getPropiedad().getDireccion() +
                " - Factura: " + pago.getNumeroFactura());
        movimiento.setPagoAsociado(pago);
        movimiento.setComprobante(pago.getObservaciones());
        movimiento.setFecha(LocalDateTime.now());

        MovimientoCorredor movimientoGuardado = movimientoRepository.save(movimiento);
        System.out.println("📈 Movimiento de corredor registrado - ID: " + movimientoGuardado.getId() +
                " - Monto: $" + movimientoGuardado.getMonto());
    }

    /**
     * CONFIRMAR TRANSFERENCIA (cuando corredor verifica en home banking)
     */
    public Pago confirmarTransferencia(Long pagoId, String comprobante) {
        System.out.println("✅ CONFIRMANDO TRANSFERENCIA - Pago ID: " + pagoId);

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + pagoId));

        validarPagoParaConfirmacion(pago);

        pago.confirmarTransferencia(comprobante);
        Pago pagoGuardado = pagoRepository.save(pago);

        actualizarMovimientoCorredor(pago, EstadoMovimiento.CONFIRMADO);

        System.out.println("🎉 Transferencia CONFIRMADA - Pago ID: " + pagoGuardado.getId());
        return pagoGuardado;
    }

    /**
     * RECHAZAR TRANSFERENCIA
     */
    public Pago rechazarTransferencia(Long pagoId, String motivo) {
        System.out.println("❌ RECHAZANDO TRANSFERENCIA - Pago ID: " + pagoId);

        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + pagoId));

        validarPagoParaRechazo(pago);

        // Revertir el pago en la cuota
        CuotaAlquiler cuota = pago.getCuota();
        cuota.setEstado(EstadoCuota.PENDIENTE);
        cuota.setFechaPago(null);
        cuota.setTotalPagado(null);

        // Restaurar la mora si existía
        if (pago.getMora() != null && pago.getMora().compareTo(BigDecimal.ZERO) > 0) {
            cuota.setMora(pago.getMora());
        }

        cuotaRepository.save(cuota);

        pago.rechazarTransferencia(motivo);
        Pago pagoGuardado = pagoRepository.save(pago);

        actualizarMovimientoCorredor(pago, EstadoMovimiento.CANCELADO);

        System.out.println("🚫 Transferencia RECHAZADA - Pago ID: " + pagoGuardado.getId());
        return pagoGuardado;
    }

    /**
     * VALIDACIONES
     */
    private void validarPagoParaConfirmacion(Pago pago) {
        if (pago.getMetodoPago() != MetodoPago.TRANSFERENCIA) {
            throw new RuntimeException("Solo se pueden confirmar transferencias. Método actual: " + pago.getMetodoPago());
        }
        if (pago.getEstadoPago() != EstadoPago.PENDIENTE) {
            throw new RuntimeException("El pago ya fue procesado - Estado actual: " + pago.getEstadoPago());
        }
    }

    private void validarPagoParaRechazo(Pago pago) {
        if (pago.getEstadoPago() != EstadoPago.PENDIENTE) {
            throw new RuntimeException("El pago ya fue procesado - Estado actual: " + pago.getEstadoPago());
        }
    }

    /**
     * ACTUALIZAR MOVIMIENTO EXISTENTE
     */
    private void actualizarMovimientoCorredor(Pago pago, EstadoMovimiento nuevoEstado) {
        MovimientoCorredor movimiento = movimientoRepository.findByPagoAsociado(pago)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado para el pago: " + pago.getId()));

        movimiento.setEstado(nuevoEstado);
        movimiento.setComprobante(pago.getObservaciones());
        movimientoRepository.save(movimiento);

        System.out.println("🔄 Movimiento actualizado - Estado: " + nuevoEstado);
    }

    /**
     * OBTENER PAGOS PENDIENTES DE VERIFICACIÓN (solo transferencias)
     */
    public List<Pago> obtenerPagosPendientesVerificacion() {
        return pagoRepository.findByMetodoPagoAndEstadoPago(MetodoPago.TRANSFERENCIA, EstadoPago.PENDIENTE);
    }

    /**
     * OBTENER ESTADÍSTICAS PARA EL PANEL DEL CORREDOR
     */
    public Map<String, Object> obtenerEstadisticasCorredor(LocalDate mes) {
        LocalDate inicioMes = mes.withDayOfMonth(1);
        LocalDate finMes = mes.withDayOfMonth(mes.lengthOfMonth());

        Map<String, Object> stats = new HashMap<>();

        // Estados confirmados (efectivo confirmado y transferencias confirmadas)
        List<EstadoPago> estadosConfirmados = Arrays.asList(
                EstadoPago.CONFIRMADO,
                EstadoPago.EFECTIVO_CONFIRMADO
        );

        // Total comisiones CONFIRMADAS del mes
        BigDecimal comisionesConfirmadas = pagoRepository.sumComisionesByEstadoPagoAndFechaBetween(
                estadosConfirmados, inicioMes, finMes
        );
        stats.put("comisionesConfirmadas", comisionesConfirmadas != null ? comisionesConfirmadas : BigDecimal.ZERO);

        // Transferencias pendientes
        List<Pago> transferenciasPendientes = obtenerPagosPendientesVerificacion();
        stats.put("transferenciasPendientesCount", transferenciasPendientes.size());
        stats.put("transferenciasPendientes", transferenciasPendientes);

        // Cantidad por método de pago CONFIRMADO
        Long transferenciasConfirmadas = pagoRepository.countByMetodoPagoAndEstadoPagoInAndFechaPagoBetween(
                MetodoPago.TRANSFERENCIA, estadosConfirmados, inicioMes, finMes
        );
        Long efectivoConfirmado = pagoRepository.countByMetodoPagoAndEstadoPagoInAndFechaPagoBetween(
                MetodoPago.EFECTIVO, estadosConfirmados, inicioMes, finMes
        );

        stats.put("transferenciasConfirmadas", transferenciasConfirmadas != null ? transferenciasConfirmadas : 0);
        stats.put("efectivoConfirmado", efectivoConfirmado != null ? efectivoConfirmado : 0);

        // Total de movimientos pendientes
        Long movimientosPendientes = movimientoRepository.countPendientes();
        stats.put("movimientosPendientes", movimientosPendientes != null ? movimientosPendientes : 0);

        // Montos totales por método de pago
        BigDecimal totalEfectivo = pagoRepository.sumComisionesByMetodoPago(MetodoPago.EFECTIVO);
        BigDecimal totalTransferencias = pagoRepository.sumComisionesByMetodoPago(MetodoPago.TRANSFERENCIA);

        stats.put("totalEfectivo", totalEfectivo != null ? totalEfectivo : BigDecimal.ZERO);
        stats.put("totalTransferencias", totalTransferencias != null ? totalTransferencias : BigDecimal.ZERO);

        return stats;
    }

    /**
     * OBTENER TODOS LOS MOVIMIENTOS DEL CORREDOR
     */
    public List<MovimientoCorredor> obtenerMovimientosCorredor() {
        return movimientoRepository.findAllByOrderByFechaDesc();
    }

    /**
     * GENERAR NÚMERO DE FACTURA ÚNICO
     */
    private String generarNumeroFactura() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "FAC-" + timestamp.substring(timestamp.length() - 8);
    }

    /**
     * MÉTODO AUXILIAR PARA FRONTEND - OBTENER CUOTAID POR LOCATARIO
     */
    public Long obtenerCuotaIdPorLocatario(String emailLocatario) {
        // Buscar cuotas pendientes por email de locatario
        List<CuotaAlquiler> cuotas = cuotaRepository.findByContratoLocatarioEmailAndEstado(
                emailLocatario, EstadoCuota.PENDIENTE);

        if (!cuotas.isEmpty()) {
            return cuotas.get(0).getId();
        } else {
            throw new RuntimeException("No se encontró cuota pendiente para el locatario: " + emailLocatario);
        }
    }

    /**
     * OBTENER DETALLE COMPLETO DE UN PAGO
     */
    public Map<String, Object> obtenerDetallePago(Long pagoId) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        Map<String, Object> detalle = new HashMap<>();
        detalle.put("pago", pago);
        detalle.put("cuota", pago.getCuota());
        detalle.put("contrato", pago.getCuota().getContrato());
        detalle.put("propiedad", pago.getCuota().getContrato().getPropiedad());
        detalle.put("locatario", pago.getCuota().getContrato().getLocatario());
        detalle.put("locador", pago.getCuota().getContrato().getLocador());

        return detalle;
    }
}