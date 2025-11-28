package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.service;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.*;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.CuotaAlquilerRepository;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.ContratoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CuotaGeneracionService {

    @Autowired
    private CuotaAlquilerRepository cuotaRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    // ========== MÉTODOS DE CONSULTA ==========

    /**
     * Obtener cuotas por estado
     */
    public List<CuotaAlquiler> obtenerCuotasPorEstado(EstadoCuota estado) {
        return cuotaRepository.findByEstado(estado);
    }

    /**
     * Obtener cuotas pendientes
     */
    public List<CuotaAlquiler> obtenerCuotasPendientes() {
        return cuotaRepository.findByEstado(EstadoCuota.PENDIENTE);
    }

    /**
     * Obtener cuotas vencidas
     */
    public List<CuotaAlquiler> obtenerCuotasVencidas() {
        LocalDate hoy = LocalDate.now();
        return cuotaRepository.findCuotasVencidas(hoy);
    }

    /**
     * Obtener cuotas próximas a vencer (próximos 7 días)
     */
    public List<CuotaAlquiler> obtenerCuotasProximasAVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(7);
        return cuotaRepository.findCuotasProximasAVencer(hoy, limite);
    }

    /**
     * Obtener cuotas por contrato
     */
    public List<CuotaAlquiler> obtenerCuotasPorContrato(Long contratoId) {
        return cuotaRepository.findByContratoIdOrderByNumeroCuota(contratoId);
    }

    /**
     * Obtener cuotas por mes y año
     */
    public List<CuotaAlquiler> obtenerCuotasPorMesYAnio(Integer mes, Integer anio) {
        return cuotaRepository.findByMesAndAnio(mes, anio);
    }

    /**
     * Verificar si un contrato tiene cuotas
     */
    public boolean contratoTieneCuotas(Long contratoId) {
        List<CuotaAlquiler> cuotas = cuotaRepository.findByContratoId(contratoId);
        return !cuotas.isEmpty();
    }

    /**
     * Obtener una cuota por su ID
     */
    public CuotaAlquiler findById(Long cuotaId) {
        try {
            System.out.println("🔍 Buscando cuota por ID: " + cuotaId);
            Optional<CuotaAlquiler> cuotaOpt = cuotaRepository.findById(cuotaId);

            if (cuotaOpt.isPresent()) {
                CuotaAlquiler cuota = cuotaOpt.get();
                System.out.println("✅ Cuota encontrada - ID: " + cuotaId +
                        ", Contrato: " + cuota.getContrato().getId() +
                        ", Estado: " + cuota.getEstado());
                return cuota;
            } else {
                System.err.println("❌ Cuota no encontrada - ID: " + cuotaId);
                throw new IllegalArgumentException("Cuota no encontrada con ID: " + cuotaId);
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR buscando cuota por ID: " + cuotaId + " - " + e.getMessage());
            throw new RuntimeException("Error al buscar cuota: " + e.getMessage(), e);
        }
    }

    // ========== MÉTODOS DE GENERACIÓN ==========

    /**
     * Generar cuotas para un contrato
     */
    public void generarCuotasParaContrato(ContratoAlquiler contrato) {
        System.out.println("🎯 === INICIANDO GENERACIÓN DE CUOTAS ===");
        System.out.println("Contrato: " + contrato.getNumeroContrato());
        System.out.println("Tipo: " + (contrato.getEsContratoExistente() ? "EXISTENTE" : "NUEVO"));

        try {
            // Validaciones críticas
            if (contrato.getFechaInicio() == null) {
                throw new IllegalArgumentException("La fecha de inicio del contrato es requerida");
            }
            if (contrato.getFechaFin() == null) {
                throw new IllegalArgumentException("La fecha de fin del contrato es requerida");
            }
            if (contrato.getMontoActual() == null || contrato.getMontoActual().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El monto actual debe ser mayor a cero");
            }
            if (contrato.getDiaVencimiento() == null) {
                contrato.setDiaVencimiento(1);
                System.out.println("⚠️ Día de vencimiento no especificado, usando día 1 por defecto");
            }

            // Determinar fecha de inicio para generación de cuotas
            LocalDate fechaInicioCuotas = determinarFechaInicioCuotas(contrato);
            System.out.println("📅 Generando cuotas desde: " + fechaInicioCuotas);

            // Verificar si ya existen cuotas para este contrato
            List<CuotaAlquiler> cuotasExistentes = cuotaRepository.findByContratoId(contrato.getId());
            if (!cuotasExistentes.isEmpty()) {
                System.out.println("⚠️ Ya existen " + cuotasExistentes.size() + " cuotas para este contrato. Eliminando...");
                cuotaRepository.deleteAll(cuotasExistentes);
            }

            // Generar cuotas
            List<CuotaAlquiler> nuevasCuotas = new ArrayList<>();
            LocalDate fechaActual = fechaInicioCuotas;
            int numeroCuota = 1;
            int maxCuotas = 60;

            while (!fechaActual.isAfter(contrato.getFechaFin()) && numeroCuota <= maxCuotas) {
                CuotaAlquiler cuota = crearCuota(contrato, numeroCuota, fechaActual);
                nuevasCuotas.add(cuota);

                System.out.println("📋 Cuota #" + numeroCuota + " - Vence: " + cuota.getFechaVencimiento() + " - $" + cuota.getMonto());

                // Avanzar al próximo mes
                fechaActual = fechaActual.plusMonths(1);
                numeroCuota++;
            }

            // Guardar cuotas
            if (!nuevasCuotas.isEmpty()) {
                cuotaRepository.saveAll(nuevasCuotas);
                System.out.println("✅ " + nuevasCuotas.size() + " cuotas generadas exitosamente");

                // Actualizar estado del contrato
                contrato.setEstado(EstadoContrato.ACTIVO);
                contratoRepository.save(contrato);
                System.out.println("✅ Contrato marcado como ACTIVO");
            } else {
                System.out.println("⚠️ No se generaron cuotas para el contrato");
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR generando cuotas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al generar cuotas: " + e.getMessage(), e);
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private LocalDate determinarFechaInicioCuotas(ContratoAlquiler contrato) {
        if (Boolean.TRUE.equals(contrato.getEsContratoExistente()) &&
                contrato.getFechaInicioAdministracion() != null) {
            LocalDate fechaAdmin = contrato.getFechaInicioAdministracion();
            System.out.println("📊 Contrato existente - Usando fecha de administración: " + fechaAdmin);
            return fechaAdmin;
        }
        System.out.println("🆕 Contrato nuevo - Usando fecha de inicio: " + contrato.getFechaInicio());
        return contrato.getFechaInicio();
    }

    private CuotaAlquiler crearCuota(ContratoAlquiler contrato, int numeroCuota, LocalDate fechaBase) {
        CuotaAlquiler cuota = new CuotaAlquiler();
        cuota.setContrato(contrato);
        cuota.setNumeroCuota(numeroCuota);
        cuota.setMes(fechaBase.getMonthValue());
        cuota.setAnio(fechaBase.getYear());
        cuota.setMonto(contrato.getMontoActual());
        cuota.setFechaVencimiento(calcularFechaVencimiento(fechaBase, contrato.getDiaVencimiento()));
        cuota.setEstado(EstadoCuota.PENDIENTE);

        if (contrato.getMontoExpensas() != null && contrato.getMontoExpensas().compareTo(BigDecimal.ZERO) > 0) {
            cuota.setMontoExpensas(contrato.getMontoExpensas());
        }

        return cuota;
    }

    private LocalDate calcularFechaVencimiento(LocalDate fechaBase, Integer diaVencimiento) {
        if (diaVencimiento == null) {
            diaVencimiento = 1;
        }
        int dia = Math.min(diaVencimiento, fechaBase.lengthOfMonth());
        return fechaBase.withDayOfMonth(dia);
    }

    // ========== MÉTODOS DE ACTUALIZACIÓN ==========

    /**
     * Actualizar montos de cuotas pendientes
     */
    public void actualizarMontosCuotasPendientes(Long contratoId, BigDecimal nuevoMonto) {
        System.out.println("🔄 Actualizando montos de cuotas pendientes...");
        List<CuotaAlquiler> cuotasPendientes = cuotaRepository.findByContratoIdAndEstado(contratoId, EstadoCuota.PENDIENTE);

        for (CuotaAlquiler cuota : cuotasPendientes) {
            cuota.setMonto(nuevoMonto);
            System.out.println("💰 Cuota #" + cuota.getNumeroCuota() + " actualizada a $" + nuevoMonto);
        }

        cuotaRepository.saveAll(cuotasPendientes);
        System.out.println("✅ " + cuotasPendientes.size() + " cuotas actualizadas");
    }

    /**
     * Regenerar cuotas para un contrato
     */
    public void regenerarCuotasParaContrato(Long contratoId) {
        Optional<ContratoAlquiler> contratoOpt = contratoRepository.findById(contratoId);
        if (contratoOpt.isPresent()) {
            System.out.println("🔄 Regenerando cuotas para contrato ID: " + contratoId);
            generarCuotasParaContrato(contratoOpt.get());
        } else {
            throw new IllegalArgumentException("Contrato no encontrado: " + contratoId);
        }
    }

    // ========== MÉTODOS DE RESUMEN ==========

    /**
     * Obtener resumen de cuotas
     */
    public ResumenCuotas obtenerResumenCuotas(Long contratoId) {
        Long totalCuotas = (long) cuotaRepository.findByContratoId(contratoId).size();
        Long pagadas = cuotaRepository.countCuotasPagadasByContrato(contratoId);
        Long pendientes = cuotaRepository.countCuotasPendientesByContrato(contratoId);
        BigDecimal totalPagado = cuotaRepository.sumMontoPagadoByContrato(contratoId);
        BigDecimal totalPendiente = cuotaRepository.sumMontoPendienteByContrato(contratoId);

        return new ResumenCuotas(totalCuotas, pagadas, pendientes, totalPagado, totalPendiente);
    }

    // ========== CLASE INTERNA PARA RESUMEN ==========

    public static class ResumenCuotas {
        private Long totalCuotas;
        private Long cuotasPagadas;
        private Long cuotasPendientes;
        private BigDecimal totalPagado;
        private BigDecimal totalPendiente;

        public ResumenCuotas(Long totalCuotas, Long cuotasPagadas, Long cuotasPendientes,
                             BigDecimal totalPagado, BigDecimal totalPendiente) {
            this.totalCuotas = totalCuotas;
            this.cuotasPagadas = cuotasPagadas;
            this.cuotasPendientes = cuotasPendientes;
            this.totalPagado = totalPagado;
            this.totalPendiente = totalPendiente;
        }

        // Getters
        public Long getTotalCuotas() { return totalCuotas; }
        public Long getCuotasPagadas() { return cuotasPagadas; }
        public Long getCuotasPendientes() { return cuotasPendientes; }
        public BigDecimal getTotalPagado() { return totalPagado; }
        public BigDecimal getTotalPendiente() { return totalPendiente; }
    }

    // ========== SCHEDULED TASKS ==========

    /**
     * Verificar y marcar cuotas vencidas (ejecución automática)
     */
    @Scheduled(cron = "0 0 6 * * ?") // Todos los días a las 6 AM
    public void verificarCuotasVencidas() {
        System.out.println("⏰ Verificando cuotas vencidas...");
        LocalDate hoy = LocalDate.now();
        List<CuotaAlquiler> cuotasVencidas = cuotaRepository.findCuotasVencidas(hoy);

        for (CuotaAlquiler cuota : cuotasVencidas) {
            BigDecimal montoMora = calcularMora(cuota);
            if (montoMora.compareTo(BigDecimal.ZERO) > 0) {
                cuota.setMora(montoMora);
                System.out.println("⚠️ Cuota #" + cuota.getNumeroCuota() + " vencida - Mora: $" + montoMora);
            }
        }

        if (!cuotasVencidas.isEmpty()) {
            cuotaRepository.saveAll(cuotasVencidas);
            System.out.println("✅ " + cuotasVencidas.size() + " cuotas marcadas como vencidas");
        }
    }

    private BigDecimal calcularMora(CuotaAlquiler cuota) {
        if (!cuota.isVencida()) {
            return BigDecimal.ZERO;
        }

        long diasVencidos = cuota.getDiasVencidos();
        BigDecimal porcentajeMora = cuota.getContrato().getPorcentajeMora();

        if (porcentajeMora == null) {
            porcentajeMora = new BigDecimal("5.00");
        }

        BigDecimal montoDiario = cuota.getMonto().multiply(porcentajeMora)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

        return montoDiario.multiply(BigDecimal.valueOf(diasVencidos));
    }

    // ========== MÉTODO PARA GENERACIÓN AUTOMÁTICA MENSUAL ==========

    /**
     * Genera cuotas mensuales automáticas para todos los contratos activos
     * Este método es llamado desde el panel de administración
     */
    public void generarCuotasMensualesAutomaticas() {
        try {
            System.out.println("🔄 INICIANDO GENERACIÓN AUTOMÁTICA DE CUOTAS MENSUALES");

            // Obtener todos los contratos activos
            List<ContratoAlquiler> contratosActivos = contratoRepository.findByEstado(EstadoContrato.ACTIVO);

            int cuotasGeneradas = 0;
            LocalDate ahora = LocalDate.now();
            int mesActual = ahora.getMonthValue();
            int anioActual = ahora.getYear();

            System.out.println("📊 Contratos activos encontrados: " + contratosActivos.size());
            System.out.println("🎯 Generando cuotas para: " + mesActual + "/" + anioActual);

            for (ContratoAlquiler contrato : contratosActivos) {
                try {
                    // Verificar si ya existe una cuota para este mes
                    boolean cuotaExistente = cuotaRepository.existsByContratoIdAndMesAndAnio(contrato.getId(), mesActual, anioActual);

                    if (!cuotaExistente) {
                        // Generar cuota para el mes actual
                        CuotaAlquiler cuota = crearCuotaParaMesActual(contrato, mesActual, anioActual);
                        cuotaRepository.save(cuota);
                        cuotasGeneradas++;

                        System.out.println("📄 Cuota generada - Contrato: " + contrato.getId() +
                                ", Mes: " + mesActual + "/" + anioActual +
                                ", Monto: $" + cuota.getMonto());
                    } else {
                        System.out.println("⏭️ Cuota ya existe para contrato " + contrato.getId() +
                                " - Mes: " + mesActual + "/" + anioActual);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error generando cuota para contrato " + contrato.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("✅ GENERACIÓN AUTOMÁTICA COMPLETADA: " + cuotasGeneradas + " cuotas generadas");

        } catch (Exception e) {
            System.err.println("❌ ERROR EN GENERACIÓN AUTOMÁTICA: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error generando cuotas automáticas: " + e.getMessage(), e);
        }
    }

    /**
     * Crea una cuota individual para el mes actual
     */
    private CuotaAlquiler crearCuotaParaMesActual(ContratoAlquiler contrato, int mes, int anio) {
        CuotaAlquiler cuota = new CuotaAlquiler();

        // Configurar fecha base para este mes
        LocalDate fechaBase = LocalDate.of(anio, mes, 1);

        // Calcular número de cuota secuencial
        List<CuotaAlquiler> cuotasExistentes = cuotaRepository.findByContratoIdOrderByNumeroCuota(contrato.getId());
        int numeroCuota = cuotasExistentes.size() + 1;

        cuota.setContrato(contrato);
        cuota.setNumeroCuota(numeroCuota);
        cuota.setMes(mes);
        cuota.setAnio(anio);
        cuota.setMonto(contrato.getMontoActual());
        cuota.setFechaVencimiento(calcularFechaVencimiento(fechaBase, contrato.getDiaVencimiento()));
        cuota.setEstado(EstadoCuota.PENDIENTE);

        // Si el contrato tiene expensas, agregarlas al monto
        if (contrato.getMontoExpensas() != null && contrato.getMontoExpensas().compareTo(BigDecimal.ZERO) > 0) {
            cuota.setMontoExpensas(contrato.getMontoExpensas());
        }

        return cuota;
    }

    // ========== MÉTODO PARA GENERAR PLAN DE PAGOS COMPLETO ==========

    /**
     * Genera un plan de pagos completo para un contrato específico
     * Este método es llamado desde el panel de administración
     */
    public void generarPlanDePagosCompleto(Long contratoId) {
        try {
            System.out.println("📊 INICIANDO GENERACIÓN DE PLAN DE PAGOS COMPLETO");
            System.out.println("🎯 Contrato ID: " + contratoId);

            // Buscar el contrato
            Optional<ContratoAlquiler> contratoOpt = contratoRepository.findById(contratoId);
            if (contratoOpt.isEmpty()) {
                throw new IllegalArgumentException("Contrato no encontrado con ID: " + contratoId);
            }

            ContratoAlquiler contrato = contratoOpt.get();

            // Verificar si el contrato está activo
            if (contrato.getEstado() != EstadoContrato.ACTIVO) {
                throw new IllegalArgumentException("El contrato no está activo. Estado actual: " + contrato.getEstado());
            }

            // Validar datos del contrato
            if (contrato.getFechaInicio() == null) {
                throw new IllegalArgumentException("La fecha de inicio del contrato es requerida");
            }
            if (contrato.getFechaFin() == null) {
                throw new IllegalArgumentException("La fecha de fin del contrato es requerida");
            }
            if (contrato.getMontoActual() == null || contrato.getMontoActual().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El monto actual debe ser mayor a cero");
            }

            // Eliminar cuotas existentes si las hay
            List<CuotaAlquiler> cuotasExistentes = cuotaRepository.findByContratoId(contratoId);
            if (!cuotasExistentes.isEmpty()) {
                System.out.println("🗑️ Eliminando " + cuotasExistentes.size() + " cuotas existentes...");
                cuotaRepository.deleteAll(cuotasExistentes);
            }

            // Generar nuevas cuotas
            generarCuotasParaContrato(contrato);

            // Obtener las cuotas generadas para mostrar resumen
            List<CuotaAlquiler> cuotasGeneradas = cuotaRepository.findByContratoIdOrderByNumeroCuota(contratoId);

            System.out.println("✅ PLAN DE PAGOS GENERADO EXITOSAMENTE");
            System.out.println("📈 Resumen:");
            System.out.println("   • Contrato: " + contrato.getNumeroContrato());
            System.out.println("   • Período: " + contrato.getFechaInicio() + " al " + contrato.getFechaFin());
            System.out.println("   • Cuotas generadas: " + cuotasGeneradas.size());
            System.out.println("   • Monto mensual: $" + contrato.getMontoActual());

            if (contrato.getMontoExpensas() != null && contrato.getMontoExpensas().compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("   • Expensas: $" + contrato.getMontoExpensas());
            }

            // Mostrar primeras cuotas como ejemplo
            if (!cuotasGeneradas.isEmpty()) {
                System.out.println("   • Primera cuota: Mes " + cuotasGeneradas.get(0).getMes() + "/" +
                        cuotasGeneradas.get(0).getAnio() + " - Vence: " +
                        cuotasGeneradas.get(0).getFechaVencimiento());
                if (cuotasGeneradas.size() > 1) {
                    System.out.println("   • Última cuota: Mes " + cuotasGeneradas.get(cuotasGeneradas.size()-1).getMes() + "/" +
                            cuotasGeneradas.get(cuotasGeneradas.size()-1).getAnio() + " - Vence: " +
                            cuotasGeneradas.get(cuotasGeneradas.size()-1).getFechaVencimiento());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR GENERANDO PLAN DE PAGOS: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error generando plan de pagos: " + e.getMessage(), e);
        }
    }
}