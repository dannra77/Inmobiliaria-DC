package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.controller;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.*;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.service.CuotaGeneracionService;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.CuotaAlquilerRepository;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.ContratoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Controller
@RequestMapping("/cuotas")
public class CuotasController {

    @Autowired
    private CuotaGeneracionService cuotaGeneracionService;

    @Autowired
    private CuotaAlquilerRepository cuotaRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    // VER CUOTAS DE UN CONTRATO
    @GetMapping("/contrato/{contratoId}")
    public String verCuotasContrato(@PathVariable Long contratoId, Model model) {
        try {
            ContratoAlquiler contrato = contratoRepository.findById(contratoId)
                    .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado"));

            List<CuotaAlquiler> cuotas = cuotaGeneracionService.obtenerCuotasPorContrato(contratoId);
            CuotaGeneracionService.ResumenCuotas resumen = cuotaGeneracionService.obtenerResumenCuotas(contratoId);

            model.addAttribute("contrato", contrato);
            model.addAttribute("cuotas", cuotas);
            model.addAttribute("resumen", resumen);
            model.addAttribute("titulo", "Cuotas - " + contrato.getNumeroContrato());

            return "cuotas/lista-contrato";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar cuotas: " + e.getMessage());
            return "redirect:/contratos/lista";
        }
    }

    // VER CUOTAS POR MES
    @GetMapping("/{mes}/{anio}")
    public String mostrarCuotasMes(
            @PathVariable String mes,
            @PathVariable Integer anio,
            Model model) {
        try {
            // Convertir nombre del mes a número
            java.time.Month month = java.time.Month.valueOf(mes.toUpperCase());
            int mesNumero = month.getValue();

            List<CuotaAlquiler> cuotas = cuotaGeneracionService.obtenerCuotasPorMesYAnio(mesNumero, anio);

            // Calcular estadísticas
            BigDecimal totalMonto = cuotas.stream()
                    .map(CuotaAlquiler::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalMora = cuotas.stream()
                    .map(cuota -> cuota.getMora() != null ? cuota.getMora() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long cuotasPagadas = cuotas.stream()
                    .filter(cuota -> cuota.getEstado() == EstadoCuota.PAGADA)
                    .count();
            long cuotasPendientes = cuotas.stream()
                    .filter(cuota -> cuota.getEstado() == EstadoCuota.PENDIENTE)
                    .count();
            long cuotasVencidas = cuotas.stream()
                    .filter(CuotaAlquiler::isVencida)
                    .count();

            model.addAttribute("cuotas", cuotas);
            model.addAttribute("mesActual", mes);
            model.addAttribute("anioActual", anio);
            model.addAttribute("nombreMes", obtenerNombreMes(mes));
            model.addAttribute("totalMonto", totalMonto);
            model.addAttribute("totalMora", totalMora);
            model.addAttribute("cuotasPagadas", cuotasPagadas);
            model.addAttribute("cuotasPendientes", cuotasPendientes);
            model.addAttribute("cuotasVencidas", cuotasVencidas);

            // Navegación entre meses
            calcularNavegacionMeses(mes, anio, model);

            return "cuotas/lista-mes";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar cuotas: " + e.getMessage());
            return "cuotas/lista-mes";
        }
    }

    // CUOTAS DEL MES ACTUAL
    @GetMapping("/actual")
    public String mostrarCuotasMesActual(Model model) {
        LocalDate hoy = LocalDate.now();
        String mesActual = hoy.getMonth().toString().toLowerCase();
        Integer anioActual = hoy.getYear();

        return "redirect:/cuotas/" + mesActual + "/" + anioActual;
    }

    // REGENERAR CUOTAS (para correcciones)
    @PostMapping("/regenerar/{contratoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> regenerarCuotas(@PathVariable Long contratoId) {
        try {
            cuotaGeneracionService.regenerarCuotasParaContrato(contratoId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cuotas regeneradas exitosamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // VERIFICAR CUOTAS (para diagnóstico)
    @GetMapping("/verificar/{contratoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarCuotas(@PathVariable Long contratoId) {
        try {
            ContratoAlquiler contrato = contratoRepository.findById(contratoId)
                    .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado"));

            List<CuotaAlquiler> cuotas = cuotaRepository.findByContratoIdOrderByNumeroCuota(contratoId);
            boolean tieneCuotas = cuotaGeneracionService.contratoTieneCuotas(contratoId);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("contratoId", contratoId);
            resultado.put("numeroContrato", contrato.getNumeroContrato());
            resultado.put("tipoContrato", contrato.getEsContratoExistente() ? "EXISTENTE" : "NUEVO");
            resultado.put("tieneCuotas", tieneCuotas);
            resultado.put("totalCuotas", cuotas.size());
            resultado.put("fechaInicio", contrato.getFechaInicio());
            resultado.put("fechaFin", contrato.getFechaFin());
            resultado.put("montoActual", contrato.getMontoActual());
            resultado.put("diaVencimiento", contrato.getDiaVencimiento());

            if (contrato.getEsContratoExistente()) {
                resultado.put("fechaInicioOriginal", contrato.getFechaInicioOriginal());
                resultado.put("fechaInicioAdministracion", contrato.getFechaInicioAdministracion());
            }

            // Detalle de cuotas
            List<Map<String, Object>> detalleCuotas = new ArrayList<>();
            for (CuotaAlquiler cuota : cuotas) {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("numero", cuota.getNumeroCuota());
                detalle.put("vencimiento", cuota.getFechaVencimiento());
                detalle.put("monto", cuota.getMonto());
                detalle.put("estado", cuota.getEstado());
                detalle.put("mora", cuota.getMora());
                detalleCuotas.add(detalle);
            }
            resultado.put("cuotas", detalleCuotas);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // MÉTODOS AUXILIARES
    private String obtenerNombreMes(String mes) {
        try {
            java.time.Month month = java.time.Month.valueOf(mes.toUpperCase());
            return month.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        } catch (Exception e) {
            return mes;
        }
    }

    private void calcularNavegacionMeses(String mesActual, Integer anioActual, Model model) {
        try {
            java.time.Month month = java.time.Month.valueOf(mesActual.toUpperCase());
            int mesNumero = month.getValue();

            // Mes anterior
            int mesAnteriorNum = (mesNumero == 1) ? 12 : mesNumero - 1;
            int anioAnterior = (mesNumero == 1) ? anioActual - 1 : anioActual;
            java.time.Month mesAnterior = java.time.Month.of(mesAnteriorNum);

            // Mes siguiente
            int mesSiguienteNum = (mesNumero == 12) ? 1 : mesNumero + 1;
            int anioSiguiente = (mesNumero == 12) ? anioActual + 1 : anioActual;
            java.time.Month mesSiguiente = java.time.Month.of(mesSiguienteNum);

            model.addAttribute("mesAnterior", mesAnterior.toString().toLowerCase());
            model.addAttribute("anioAnterior", anioAnterior);
            model.addAttribute("mesSiguiente", mesSiguiente.toString().toLowerCase());
            model.addAttribute("anioSiguiente", anioSiguiente);
            model.addAttribute("nombreMesAnterior", obtenerNombreMes(mesAnterior.toString().toLowerCase()));
            model.addAttribute("nombreMesSiguiente", obtenerNombreMes(mesSiguiente.toString().toLowerCase()));

        } catch (Exception e) {
            System.err.println("Error calculando navegación: " + e.getMessage());
        }
    }
}