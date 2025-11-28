package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.controller;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.Pago;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.MovimientoCorredor;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/corredor")
public class PanelCorredorController {

    @Autowired
    private PagoService pagoService;

    @GetMapping("/panel")
    public String panelCorredor(Model model) {
        LocalDate mesActual = LocalDate.now();

        // Obtener estadísticas
        Map<String, Object> stats = pagoService.obtenerEstadisticasCorredor(mesActual);
        model.addAllAttributes(stats);

        // Obtener movimientos del corredor
        List<MovimientoCorredor> movimientos = pagoService.obtenerMovimientosCorredor();
        model.addAttribute("movimientos", movimientos);

        model.addAttribute("mesActual", mesActual);

        return "panel-corredor";
    }

    @PostMapping("/confirmar-transferencia")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmarTransferencia(
            @RequestParam Long pagoId,
            @RequestParam(required = false) String comprobante) {
        try {
            Pago pago = pagoService.confirmarTransferencia(pagoId, comprobante);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Transferencia confirmada correctamente");
            response.put("pagoId", pago.getId());
            response.put("estado", pago.getEstadoPago().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/rechazar-transferencia")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rechazarTransferencia(
            @RequestParam Long pagoId,
            @RequestParam String motivo) {
        try {
            Pago pago = pagoService.rechazarTransferencia(pagoId, motivo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Transferencia rechazada");
            response.put("pagoId", pago.getId());
            response.put("estado", pago.getEstadoPago().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/datos-actualizados")
    @ResponseBody
    public Map<String, Object> obtenerDatosActualizados() {
        LocalDate mesActual = LocalDate.now();
        return pagoService.obtenerEstadisticasCorredor(mesActual);
    }

    @GetMapping("/detalle-pago/{pagoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetallePago(@PathVariable Long pagoId) {
        try {
            // Aquí implementarías la lógica para obtener detalles específicos del pago
            Map<String, Object> detalle = new HashMap<>();
            detalle.put("pagoId", pagoId);
            detalle.put("mensaje", "Detalles del pago " + pagoId);
            return ResponseEntity.ok(detalle);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}