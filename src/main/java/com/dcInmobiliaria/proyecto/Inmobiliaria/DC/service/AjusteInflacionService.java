package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.service;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.*;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.ContratoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AjusteInflacionService {

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private CuotaGeneracionService cuotaGeneracionService;

    /**
     * Aplica ajustes por inflación a contratos que lo requieran
     */
    @Transactional
    public void aplicarAjustesAutomaticos() {
        System.out.println("📈 APLICANDO AJUSTES POR INFLACIÓN");

        List<ContratoAlquiler> contratosParaAjustar = contratoRepository.findByEstado(EstadoContrato.ACTIVO);
        LocalDate hoy = LocalDate.now();

        for (ContratoAlquiler contrato : contratosParaAjustar) {
            if (debeAplicarAjuste(contrato, hoy)) {
                aplicarAjusteContrato(contrato);
            }
        }
    }

    /**
     * Verifica si un contrato debe aplicar ajuste
     */
    private boolean debeAplicarAjuste(ContratoAlquiler contrato, LocalDate fechaActual) {
        LocalDate ultimoAjuste = contrato.getFechaInicio();
        if (contrato.getFechaInicioAdministracion() != null) {
            ultimoAjuste = contrato.getFechaInicioAdministracion();
        }

        LocalDate proximoAjuste = ultimoAjuste.plusMonths(contrato.getFrecuenciaMeses());
        return !fechaActual.isBefore(proximoAjuste);
    }

    /**
     * Aplica ajuste a un contrato específico
     */
    @Transactional
    public void aplicarAjusteContrato(ContratoAlquiler contrato) {
        try {
            System.out.println("🔄 Aplicando ajuste a contrato: " + contrato.getNumeroContrato());

            // Simular obtención de índice real (en producción, conectar con API)
            BigDecimal porcentajeAjuste = obtenerPorcentajeAjuste(contrato.getTipoIndice());

            // Calcular nuevo monto
            BigDecimal montoActual = contrato.getMontoActual() != null ?
                    contrato.getMontoActual() : contrato.getMontoMensual();
            BigDecimal nuevoMonto = montoActual.multiply(BigDecimal.ONE.add(
                    porcentajeAjuste.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));

            // Redondear a 2 decimales
            nuevoMonto = nuevoMonto.setScale(2, RoundingMode.HALF_UP);

            // Actualizar contrato
            contrato.setMontoActual(nuevoMonto);
            contratoRepository.save(contrato);

            System.out.println("✅ Ajuste aplicado: " + porcentajeAjuste + "% - Nuevo monto: $" + nuevoMonto);

            // Registrar en observaciones
            String observacion = "Ajuste " + contrato.getTipoIndice() + " aplicado: " +
                    porcentajeAjuste + "%. Monto anterior: $" + montoActual + " - Nuevo: $" + nuevoMonto;

            if (contrato.getObservaciones() != null) {
                contrato.setObservaciones(contrato.getObservaciones() + "\n" + observacion);
            } else {
                contrato.setObservaciones(observacion);
            }

        } catch (Exception e) {
            System.err.println("❌ Error aplicando ajuste: " + e.getMessage());
            throw new RuntimeException("Error al aplicar ajuste al contrato", e);
        }
    }

    /**
     * Obtiene el porcentaje de ajuste según el tipo de índice
     * EN PRODUCCIÓN: Conectar con APIs oficiales (INDEC, BCRA, etc.)
     */
    private BigDecimal obtenerPorcentajeAjuste(TipoIndice tipoIndice) {
        // Valores de ejemplo - En producción obtener de APIs oficiales
        switch (tipoIndice) {
            case ICL:
                return new BigDecimal("12.5"); // Ejemplo ICL
            case IPC:
                return new BigDecimal("10.2"); // Ejemplo IPC
            case MIXTO:
                return new BigDecimal("11.0"); // Ejemplo mixto
            default:
                return new BigDecimal("8.0"); // Valor por defecto
        }
    }

    /**
     * Aplica un ajuste manual con porcentaje específico
     */
    @Transactional
    public void aplicarAjusteManual(Long contratoId, BigDecimal porcentajeAjuste, String observaciones) {
        ContratoAlquiler contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        BigDecimal montoActual = contrato.getMontoActual() != null ?
                contrato.getMontoActual() : contrato.getMontoMensual();

        BigDecimal nuevoMonto = montoActual.multiply(BigDecimal.ONE.add(
                porcentajeAjuste.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));

        nuevoMonto = nuevoMonto.setScale(2, RoundingMode.HALF_UP);
        contrato.setMontoActual(nuevoMonto);

        String observacion = "Ajuste manual aplicado: " + porcentajeAjuste +
                "%. Monto anterior: $" + montoActual + " - Nuevo: $" + nuevoMonto;
        if (observaciones != null) {
            observacion += " - " + observaciones;
        }

        if (contrato.getObservaciones() != null) {
            contrato.setObservaciones(contrato.getObservaciones() + "\n" + observacion);
        } else {
            contrato.setObservaciones(observacion);
        }

        contratoRepository.save(contrato);
    }
}