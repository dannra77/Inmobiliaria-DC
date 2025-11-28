package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.controller;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.*;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.service.*;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.dto.PagoRequest;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.dto.DescuentoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/administracion")
public class AdministracionController {

    @Autowired
    private CuotaGeneracionService cuotaGeneracionService;

    @Autowired
    private AjusteInflacionService ajusteInflacionService;

    @Autowired
    private PagoService pagoService;

    // === MÉTODO PRINCIPAL EXISTENTE (MODIFICADO) ===
    @GetMapping("/propiedades")
    public String administrarPropiedades(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) String estado,
            Model model) {

        System.out.println("🏢 PANEL ADMINISTRACIÓN - Mes: " + mes + ", Año: " + anio + ", Estado: " + estado);

        // === AGREGAR ESTO AL INICIO DEL MÉTODO ===
        // Crear mapa de meses en español
        Map<Integer, String> mesesEspanol = new LinkedHashMap<>();
        mesesEspanol.put(1, "Enero");
        mesesEspanol.put(2, "Febrero");
        mesesEspanol.put(3, "Marzo");
        mesesEspanol.put(4, "Abril");
        mesesEspanol.put(5, "Mayo");
        mesesEspanol.put(6, "Junio");
        mesesEspanol.put(7, "Julio");
        mesesEspanol.put(8, "Agosto");
        mesesEspanol.put(9, "Septiembre");
        mesesEspanol.put(10, "Octubre");
        mesesEspanol.put(11, "Noviembre");
        mesesEspanol.put(12, "Diciembre");

        model.addAttribute("mesesEspanol", mesesEspanol);
        // === FIN DE LA MODIFICACIÓN ===

        // Obtener cuotas según los filtros (tu código existente)
        List<CuotaAlquiler> cuotas;

        if (mes != null && anio != null) {
            cuotas = cuotaGeneracionService.obtenerCuotasPorMesYAnio(mes, anio);
            System.out.println("📊 Cuotas del mes " + mes + "/" + anio + ": " + cuotas.size());
        } else {
            cuotas = cuotaGeneracionService.obtenerCuotasPorEstado(EstadoCuota.PENDIENTE);
            System.out.println("📊 Todas las cuotas pendientes: " + cuotas.size());
        }

        // Filtrar por estado si se especifica
        if (estado != null && !estado.equals("todos")) {
            EstadoCuota estadoFiltro = EstadoCuota.valueOf(estado);
            cuotas = cuotas.stream()
                    .filter(c -> c.getEstado() == estadoFiltro)
                    .toList();
            System.out.println("🎯 Filtrado por estado " + estado + ": " + cuotas.size() + " cuotas");
        }

        // Calcular estadísticas MEJORADAS (tu código existente)
        long totalCuotas = cuotas.size();
        long pendientes = cuotas.stream().filter(c -> c.getEstado() == EstadoCuota.PENDIENTE).count();
        long pagadas = cuotas.stream().filter(c -> c.getEstado() == EstadoCuota.PAGADA).count();
        long vencidas = cuotas.stream().filter(c -> c.getEstado() == EstadoCuota.VENCIDA).count();

        BigDecimal totalMontoPendiente = cuotas.stream()
                .filter(c -> c.getEstado() == EstadoCuota.PENDIENTE)
                .map(CuotaAlquiler::getTotalAPagar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMontoPagado = cuotas.stream()
                .filter(c -> c.getEstado() == EstadoCuota.PAGADA)
                .map(c -> c.getTotalPagado() != null ? c.getTotalPagado() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular mora total pendiente
        BigDecimal totalMoraPendiente = cuotas.stream()
                .filter(c -> c.getEstado() == EstadoCuota.PENDIENTE || c.getEstado() == EstadoCuota.VENCIDA)
                .map(c -> c.getMora() != null ? c.getMora() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Agregar al modelo (tu código existente + el nuevo atributo)
        model.addAttribute("cuotas", cuotas);
        model.addAttribute("totalCuotas", totalCuotas);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("pagadas", pagadas);
        model.addAttribute("vencidas", vencidas);
        model.addAttribute("totalMontoPendiente", totalMontoPendiente);
        model.addAttribute("totalMontoPagado", totalMontoPagado);
        model.addAttribute("totalMoraPendiente", totalMoraPendiente);
        model.addAttribute("mesActual", mes != null ? mes : LocalDate.now().getMonthValue());
        model.addAttribute("anioActual", anio != null ? anio : LocalDate.now().getYear());
        model.addAttribute("estadoFiltro", estado);

        // Agregar métodos helper para Thymeleaf (tu código existente)
        model.addAttribute("calcularProximoAumento", new CalcularProximoAumentoHelper());
        model.addAttribute("calcularDiasVencidos", new CalcularDiasVencidosHelper());
        model.addAttribute("estaVencida", new EstaVencidaHelper());

        System.out.println("✅ PANEL CARGADO - " + totalCuotas + " cuotas totales");
        return "administracion/administracion-propiedades";
    }

    // === MÉTODOS DE ACCIÓN EXISTENTES (MODIFICADOS) ===

    @GetMapping("/generar-cuotas")
    public String generarCuotasMensuales(RedirectAttributes redirectAttributes) {
        try {
            System.out.println("🔄 SOLICITUD DE GENERACIÓN DE CUOTAS");
            cuotaGeneracionService.generarCuotasMensualesAutomaticas();
            redirectAttributes.addFlashAttribute("success", "✅ Cuotas generadas exitosamente para el mes actual");
            System.out.println("✅ CUOTAS GENERADAS EXITOSAMENTE");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error generando cuotas: " + e.getMessage());
            System.err.println("❌ ERROR GENERANDO CUOTAS: " + e.getMessage());
        }
        return "redirect:/administracion/propiedades";
    }

    // === NUEVOS MÉTODOS MEJORADOS ===

    /**
     * APLICAR AJUSTES POR INFLACIÓN
     */
    @PostMapping("/aplicar-ajustes")
    public String aplicarAjustesInflacion(RedirectAttributes redirectAttributes) {
        try {
            System.out.println("📈 SOLICITUD DE APLICACIÓN DE AJUSTES");
            ajusteInflacionService.aplicarAjustesAutomaticos();
            redirectAttributes.addFlashAttribute("success", "✅ Ajustes por inflación aplicados exitosamente");
            System.out.println("✅ AJUSTES APLICADOS EXITOSAMENTE");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error aplicando ajustes: " + e.getMessage());
            System.err.println("❌ ERROR APLICANDO AJUSTES: " + e.getMessage());
        }
        return "redirect:/administracion/propiedades";
    }

    /**
     * GENERAR PLAN DE PAGOS COMPLETO PARA UN CONTRATO
     */
    @PostMapping("/generar-plan-pagos/{contratoId}")
    public String generarPlanPagosCompleto(@PathVariable Long contratoId, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("📊 SOLICITUD DE GENERACIÓN DE PLAN DE PAGOS - Contrato: " + contratoId);
            cuotaGeneracionService.generarPlanDePagosCompleto(contratoId);
            redirectAttributes.addFlashAttribute("success", "✅ Plan de pagos generado exitosamente");
            System.out.println("✅ PLAN DE PAGOS GENERADO EXITOSAMENTE");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error generando plan de pagos: " + e.getMessage());
            System.err.println("❌ ERROR GENERANDO PLAN DE PAGOS: " + e.getMessage());
        }
        return "redirect:/administracion/propiedades";
    }

    /**
     * APLICAR AJUSTE MANUAL A UN CONTRATO
     */
    @PostMapping("/aplicar-ajuste-manual")
    public String aplicarAjusteManual(
            @RequestParam Long contratoId,
            @RequestParam BigDecimal porcentajeAjuste,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("🎛️ SOLICITUD DE AJUSTE MANUAL - Contrato: " + contratoId + ", Porcentaje: " + porcentajeAjuste + "%");
            ajusteInflacionService.aplicarAjusteManual(contratoId, porcentajeAjuste, observaciones);
            redirectAttributes.addFlashAttribute("success", "✅ Ajuste manual del " + porcentajeAjuste + "% aplicado exitosamente");
            System.out.println("✅ AJUSTE MANUAL APLICADO EXITOSAMENTE");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error aplicando ajuste manual: " + e.getMessage());
            System.err.println("❌ ERROR APLICANDO AJUSTE MANUAL: " + e.getMessage());
        }
        return "redirect:/administracion/propiedades";
    }

    // === ENDPOINTS API PARA FRONTEND ===

    /**
     * PROCESAR PAGO COMPLETO DESDE EL PANEL DE ADMINISTRACIÓN
     */
    @PostMapping("/procesar-pago-completo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> procesarPagoCompleto(@RequestBody PagoRequest pagoRequest) {
        try {
            System.out.println("💰 PANEL ADMIN - SOLICITUD DE PAGO COMPLETA:");
            System.out.println("   • Cuota ID: " + pagoRequest.getCuotaId());
            System.out.println("   • Monto: $" + pagoRequest.getMonto());
            System.out.println("   • Método: " + pagoRequest.getMetodoPago());
            System.out.println("   • Descuentos: " + (pagoRequest.getDescuentos() != null ? pagoRequest.getDescuentos().size() : 0));

            // Validaciones básicas
            if (pagoRequest.getCuotaId() == null) {
                throw new IllegalArgumentException("El ID de cuota es requerido");
            }
            if (pagoRequest.getMonto() == null || pagoRequest.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El monto debe ser mayor a cero");
            }
            if (pagoRequest.getMetodoPago() == null || pagoRequest.getMetodoPago().trim().isEmpty()) {
                throw new IllegalArgumentException("El método de pago es requerido");
            }

            // Convertir método de pago
            MetodoPago metodoPago = MetodoPago.valueOf(pagoRequest.getMetodoPago().toUpperCase());

            // Procesar pago
            Pago pago = pagoService.procesarPago(
                    pagoRequest.getCuotaId(),
                    pagoRequest.getMonto(),
                    metodoPago,
                    pagoRequest.getDescuentos(),
                    pagoRequest.getObservaciones()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pagoId", pago.getId());
            response.put("numeroFactura", pago.getNumeroFactura());
            response.put("comisionCorredor", pago.getComisionCorredor());
            response.put("montoLocador", pago.getMontoLocador());
            response.put("estado", pago.getEstadoPago().toString());
            response.put("metodoPago", pago.getMetodoPago().toString());
            response.put("fechaPago", pago.getFechaPago());
            response.put("distribuido", pago.getDistribuido());

            System.out.println("✅ PAGO PROCESADO EXITOSAMENTE DESDE PANEL ADMIN:");
            System.out.println("   • Factura: " + pago.getNumeroFactura());
            System.out.println("   • Comisión corredor: $" + pago.getComisionCorredor());
            System.out.println("   • Monto locador: $" + pago.getMontoLocador());
            System.out.println("   • Estado: " + pago.getEstadoPago());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ ERROR PROCESANDO PAGO DESDE PANEL ADMIN: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * OBTENER DETALLES DE CUOTA PARA MODAL DE PAGO
     */
    @GetMapping("/detalle-cuota/{cuotaId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetalleCuota(@PathVariable Long cuotaId) {
        try {
            System.out.println("📋 SOLICITUD DETALLE CUOTA - ID: " + cuotaId);


            CuotaAlquiler cuota = cuotaGeneracionService.findById(cuotaId);

            Map<String, Object> detalle = new HashMap<>();
            detalle.put("cuota", cuota);
            detalle.put("contrato", cuota.getContrato());
            detalle.put("propiedad", cuota.getContrato().getPropiedad());
            detalle.put("locatario", cuota.getContrato().getLocatario());
            detalle.put("locador", cuota.getContrato().getLocador());
            detalle.put("mora", cuota.getMora() != null ? cuota.getMora() : BigDecimal.ZERO);
            detalle.put("totalAPagar", cuota.getTotalAPagar());
            detalle.put("diasVencidos", calcularDiasVencidos(cuota));

            System.out.println("✅ DETALLE CUOTA OBTENIDO - Total a pagar: $" + cuota.getTotalAPagar());

            return ResponseEntity.ok(detalle);

        } catch (Exception e) {
            System.err.println("❌ ERROR OBTENIENDO DETALLE CUOTA: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // === MÉTODOS HELPER PARA THYMELEAF ===

    /**
     * Clases helper para usar en Thymeleaf
     */
    public static class CalcularProximoAumentoHelper {
        public String calcularProximoAumento(ContratoAlquiler contrato) {
            if (contrato.getFechaInicio() == null) return "No definido";

            LocalDate fechaInicio = contrato.getFechaInicio();
            Integer frecuencia = contrato.getFrecuenciaMeses() != null ? contrato.getFrecuenciaMeses() : 6;

            LocalDate proximoAumento = fechaInicio;
            while (proximoAumento.isBefore(LocalDate.now()) || proximoAumento.isEqual(LocalDate.now())) {
                proximoAumento = proximoAumento.plusMonths(frecuencia);
            }

            return proximoAumento.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    public static class CalcularDiasVencidosHelper {
        public long calcularDiasVencidos(CuotaAlquiler cuota) {
            if (cuota.getEstado() != EstadoCuota.PENDIENTE ||
                    cuota.getFechaVencimiento() == null ||
                    !cuota.getFechaVencimiento().isBefore(LocalDate.now())) {
                return 0;
            }
            return java.time.temporal.ChronoUnit.DAYS.between(
                    cuota.getFechaVencimiento(), LocalDate.now());
        }
    }

    public static class EstaVencidaHelper {
        public boolean estaVencida(CuotaAlquiler cuota) {
            return cuota.getEstado() == EstadoCuota.PENDIENTE &&
                    cuota.getFechaVencimiento() != null &&
                    cuota.getFechaVencimiento().isBefore(LocalDate.now());
        }
    }

    // === MÉTODOS PRIVADOS AUXILIARES ===

    private long calcularDiasVencidos(CuotaAlquiler cuota) {
        if (cuota.getEstado() != EstadoCuota.PENDIENTE ||
                cuota.getFechaVencimiento() == null ||
                !cuota.getFechaVencimiento().isBefore(LocalDate.now())) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
                cuota.getFechaVencimiento(), LocalDate.now());
    }
}