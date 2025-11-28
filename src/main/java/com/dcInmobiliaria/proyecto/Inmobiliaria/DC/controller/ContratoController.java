package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.controller;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.*;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.*;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.service.CuotaGeneracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/contratos")
public class ContratoController {

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private PropiedadRepository propiedadRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private ParticipacionContratoRepository participacionRepository;

    @Autowired
    private CuotaGeneracionService cuotaGeneracionService;

    // MOSTRAR FORMULARIO DE CREACIÓN
    @GetMapping("/nuevo")
    public String mostrarFormularioContrato(
            @RequestParam(required = false) Long propiedadId,
            Model model) {

        System.out.println("=== FORMULARIO NUEVO CONTRATO ===");
        System.out.println("Propiedad ID: " + propiedadId);

        List<Propiedad> propiedades = propiedadRepository.findByEstado("Disponible");
        List<Persona> personas = personaRepository.findAllByOrderByApellidoAscNombreAsc();

        ContratoAlquiler contrato = new ContratoAlquiler();

        if (propiedadId != null) {
            Optional<Propiedad> propiedadOptional = propiedadRepository.findById(propiedadId);
            if (propiedadOptional.isPresent()) {
                Propiedad propiedad = propiedadOptional.get();
                contrato.setPropiedad(propiedad);

                if (propiedad.getPrecioAlquiler() != null) {
                    contrato.setMontoMensual(propiedad.getPrecioAlquiler());
                }

                contrato.setFechaInicio(LocalDate.now());
                contrato.setFechaFin(LocalDate.now().plusMonths(12));

                System.out.println("Propiedad asignada: " + propiedad.getTitulo());
            }
        }

        inicializarModelo(model, contrato, "crear");
        model.addAttribute("propiedadSeleccionada", propiedadId);

        return "contratos/formulario";
    }

    // GUARDAR CONTRATO - VERSIÓN CORREGIDA
    @PostMapping("/guardar")
    public String guardarContrato(
            @Valid @ModelAttribute("contrato") ContratoAlquiler contrato,
            BindingResult result,
            @RequestParam Long propiedadId,
            @RequestParam Long locadorId,
            @RequestParam Long locatarioId,
            @RequestParam Long solicitanteId,
            @RequestParam(required = false) List<Long> garantesIds,
            @RequestParam(required = false) List<Long> habitantesIds,
            @RequestParam Integer modeloContrato,
            @RequestParam String expensasOption,
            @RequestParam(required = false) Boolean esContratoExistente,
            @RequestParam(required = false) LocalDate fechaInicioOriginal,
            @RequestParam(required = false) LocalDate fechaInicioAdministracion,
            @RequestParam(required = false) LocalDate fechaFinExistente,
            @RequestParam(required = false) BigDecimal montoInicial,
            @RequestParam(required = false) BigDecimal montoMensualExistente,
            @RequestParam(required = false) String vehiculoMarca,
            @RequestParam(required = false) String vehiculoModelo,
            @RequestParam(required = false) String vehiculoColor,
            @RequestParam(required = false) String vehiculoPatente,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("🎯 === INICIANDO GUARDADO DE CONTRATO ===");
        System.out.println("Tipo de contrato: " + (Boolean.TRUE.equals(esContratoExistente) ? "EXISTENTE" : "NUEVO"));

        try {
            // ⭐⭐ VALIDACIÓN MEJORADA - CORREGIDA
            List<String> erroresValidacion = new ArrayList<>();

            // Validar campos básicos requeridos para todos los contratos
            if (propiedadId == null) {
                erroresValidacion.add("La propiedad es requerida");
            }
            if (locadorId == null) {
                erroresValidacion.add("El locador es requerido");
            }
            if (locatarioId == null) {
                erroresValidacion.add("El locatario es requerido");
            }
            if (solicitanteId == null) {
                erroresValidacion.add("El solicitante es requerido");
            }
            if (modeloContrato == null) {
                erroresValidacion.add("El modelo de contrato es requerido");
            }

            // Validar campos según tipo de contrato
            if (Boolean.TRUE.equals(esContratoExistente)) {
                System.out.println("🔍 Validando contrato EXISTENTE...");

                if (fechaInicioOriginal == null) {
                    erroresValidacion.add("La fecha de inicio original es requerida para contratos existentes");
                }
                if (fechaInicioAdministracion == null) {
                    erroresValidacion.add("La fecha de inicio de administración es requerida para contratos existentes");
                }
                if (fechaFinExistente == null) {
                    erroresValidacion.add("La fecha de fin es requerida para contratos existentes");
                }
                if (montoInicial == null) {
                    erroresValidacion.add("El monto actual es requerido para contratos existentes");
                }

            } else {
                System.out.println("🔍 Validando contrato NUEVO...");

                if (contrato.getFechaInicio() == null) {
                    erroresValidacion.add("La fecha de inicio es requerida");
                }
                if (contrato.getFechaFin() == null) {
                    erroresValidacion.add("La fecha de fin es requerida");
                }
                if (contrato.getMontoMensual() == null) {
                    erroresValidacion.add("El monto mensual es requerido");
                }
            }

            // Si hay errores de validación personalizada
            if (!erroresValidacion.isEmpty()) {
                System.out.println("❌ ERRORES DE VALIDACIÓN:");
                erroresValidacion.forEach(System.out::println);

                inicializarModelo(model, contrato, "crear");
                model.addAttribute("error", String.join(", ", erroresValidacion));
                return "contratos/formulario";
            }

            // Validar errores de formulario de Spring
            if (result.hasErrors()) {
                System.out.println("❌ ERRORES DE FORMULARIO SPRING:");
                result.getAllErrors().forEach(error -> System.out.println(" - " + error.getDefaultMessage()));

                inicializarModelo(model, contrato, "crear");
                model.addAttribute("error", "Por favor, complete todos los campos requeridos correctamente");
                return "contratos/formulario";
            }

            // 1. OBTENER Y VALIDAR PROPIEDAD
            System.out.println("🏠 Obteniendo propiedad ID: " + propiedadId);
            Propiedad propiedad = propiedadRepository.findById(propiedadId)
                    .orElseThrow(() -> {
                        System.out.println("❌ Propiedad no encontrada: " + propiedadId);
                        return new IllegalArgumentException("Propiedad no encontrada");
                    });

            if (!"Disponible".equals(propiedad.getEstado())) {
                System.out.println("❌ Propiedad no disponible: " + propiedad.getEstado());
                model.addAttribute("error", "La propiedad no está disponible. Estado: " + propiedad.getEstado());
                inicializarModelo(model, contrato, "crear");
                return "contratos/formulario";
            }

            // 2. OBTENER PERSONAS
            System.out.println("👤 Obteniendo participantes...");
            Persona locador = personaRepository.findById(locadorId)
                    .orElseThrow(() -> new IllegalArgumentException("Locador no encontrado"));
            Persona locatario = personaRepository.findById(locatarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Locatario no encontrado"));
            Persona solicitante = personaRepository.findById(solicitanteId)
                    .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado"));

            // 3. CONFIGURAR CONTRATO - LÓGICA CORREGIDA
            System.out.println("⚙️ Configurando contrato...");

            // Establecer relaciones principales
            contrato.setPropiedad(propiedad);
            contrato.setLocador(locador);
            contrato.setLocatario(locatario);
            contrato.setSolicitante(solicitante);

            // Configurar campos adicionales
            contrato.setModeloContrato(modeloContrato);
            contrato.setExpensasOption(ContratoAlquiler.ExpensasOption.valueOf(expensasOption));
            contrato.setEstado(EstadoContrato.ACTIVO);

            // ⭐⭐ LÓGICA CORREGIDA PARA CONTRATOS EXISTENTES
            if (Boolean.TRUE.equals(esContratoExistente)) {
                System.out.println("📅 Configurando contrato EXISTENTE...");

                contrato.setEsContratoExistente(true);
                contrato.setFechaInicioOriginal(fechaInicioOriginal);
                contrato.setFechaInicioAdministracion(fechaInicioAdministracion);

                // ⭐⭐ CORRECCIÓN: Usar las fechas específicas para contratos existentes
                contrato.setFechaInicio(fechaInicioOriginal);
                contrato.setFechaFin(fechaFinExistente);

                // Configurar montos
                contrato.setMontoInicial(montoInicial);
                contrato.setMontoActual(montoInicial);

                // El monto mensual puede ser diferente del inicial
                if (montoMensualExistente != null) {
                    contrato.setMontoMensual(montoMensualExistente);
                } else {
                    contrato.setMontoMensual(montoInicial);
                }

                System.out.println("✅ Contrato existente configurado:");
                System.out.println("   - Inicio real: " + fechaInicioOriginal);
                System.out.println("   - Inicio tu gestión: " + fechaInicioAdministracion);
                System.out.println("   - Fin contrato: " + fechaFinExistente);
                System.out.println("   - Monto actual: " + montoInicial);

            } else {
                System.out.println("🆕 Configurando contrato NUEVO...");
                contrato.setEsContratoExistente(false);
                // Para contratos nuevos, el monto actual es igual al monto mensual
                contrato.setMontoActual(contrato.getMontoMensual());
                System.out.println("💰 Monto mensual (nuevo): " + contrato.getMontoMensual());
            }

            // Campos de vehículo
            contrato.setVehiculoMarca(vehiculoMarca);
            contrato.setVehiculoModelo(vehiculoModelo);
            contrato.setVehiculoColor(vehiculoColor);
            contrato.setVehiculoPatente(vehiculoPatente);

            // Generar número de contrato definitivo
            if (contrato.getNumeroContrato() == null || contrato.getNumeroContrato().startsWith("TEMP-")) {
                contrato.setNumeroContrato(generarNumeroContratoDefinitivo());
            }

            // 4. GUARDAR CONTRATO
            System.out.println("💾 Guardando contrato en BD...");
            ContratoAlquiler contratoGuardado = contratoRepository.save(contrato);
            System.out.println("✅ Contrato guardado ID: " + contratoGuardado.getId());

            // 5. AGREGAR PARTICIPACIONES
            System.out.println("👥 Agregando participaciones...");
            agregarTodasLasParticipaciones(contratoGuardado, garantesIds, habitantesIds);

            // 6. ACTUALIZAR ESTADO DE PROPIEDAD
            System.out.println("🔄 Actualizando estado de propiedad...");
            propiedad.setEstado("Alquilada");
            propiedadRepository.save(propiedad);

            // 7. GENERAR CUOTAS
            System.out.println("💰 GENERANDO CUOTAS AUTOMÁTICAMENTE...");
            try {
                cuotaGeneracionService.generarCuotasParaContrato(contratoGuardado);
                System.out.println("✅ Cuotas generadas exitosamente");

                // Opcional: Verificar cuotas generadas
                List<CuotaAlquiler> cuotasGeneradas = cuotaGeneracionService.obtenerCuotasPorContrato(contratoGuardado.getId());
                System.out.println("📊 Cuotas generadas: " + cuotasGeneradas.size());

            } catch (Exception e) {
                System.err.println("❌ Error generando cuotas: " + e.getMessage());
                e.printStackTrace();
                // No fallar el contrato completo si hay error en cuotas
            }

            String mensajeExito = "Contrato " + (Boolean.TRUE.equals(esContratoExistente) ? "existente" : "nuevo") +
                    " creado exitosamente. Número: " + contratoGuardado.getNumeroContrato();

            redirectAttributes.addFlashAttribute("success", mensajeExito);

            System.out.println("🎉 CONTRATO GUARDADO EXITOSAMENTE");
            return "redirect:/contratos/lista";

        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO: " + e.getMessage());
            e.printStackTrace();
            inicializarModelo(model, contrato, "crear");
            model.addAttribute("error", "Error al guardar el contrato: " + e.getMessage());
            return "contratos/formulario";
        }
    }

    // ACTUALIZAR CONTRATO - VERSIÓN CORREGIDA
    @PostMapping("/actualizar/{id}")
    public String actualizarContrato(
            @PathVariable Long id,
            @Valid @ModelAttribute("contrato") ContratoAlquiler contrato,
            BindingResult result,
            @RequestParam Long propiedadId,
            @RequestParam Long solicitanteId,
            @RequestParam Long locadorId,
            @RequestParam Long locatarioId,
            @RequestParam(required = false) List<Long> garantesIds,
            @RequestParam(required = false) List<Long> habitantesIds,
            @RequestParam Integer modeloContrato,
            @RequestParam String expensasOption,
            @RequestParam(required = false) Boolean esContratoExistente,
            @RequestParam(required = false) LocalDate fechaInicioOriginal,
            @RequestParam(required = false) LocalDate fechaInicioAdministracion,
            @RequestParam(required = false) LocalDate fechaFinExistente,
            @RequestParam(required = false) BigDecimal montoInicial,
            @RequestParam(required = false) BigDecimal montoMensualExistente,
            @RequestParam(required = false) String vehiculoMarca,
            @RequestParam(required = false) String vehiculoModelo,
            @RequestParam(required = false) String vehiculoColor,
            @RequestParam(required = false) String vehiculoPatente,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("🔄 === ACTUALIZANDO CONTRATO ID: " + id + " ===");

        if (result.hasErrors()) {
            inicializarModelo(model, contrato, "editar");
            model.addAttribute("error", "Por favor, corrija los errores del formulario");
            return "contratos/formulario";
        }

        try {
            ContratoAlquiler contratoExistente = contratoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado"));

            // Obtener propiedad
            Propiedad propiedad = propiedadRepository.findById(propiedadId)
                    .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada"));

            // Obtener personas
            Persona locador = personaRepository.findById(locadorId)
                    .orElseThrow(() -> new IllegalArgumentException("Locador no encontrado"));
            Persona locatario = personaRepository.findById(locatarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Locatario no encontrado"));
            Persona solicitante = personaRepository.findById(solicitanteId)
                    .orElseThrow(() -> new IllegalArgumentException("Solicitante no encontrado"));

            // Actualizar campos básicos
            contratoExistente.setPropiedad(propiedad);
            contratoExistente.setLocador(locador);
            contratoExistente.setLocatario(locatario);
            contratoExistente.setSolicitante(solicitante);

            contratoExistente.setFechaInicio(contrato.getFechaInicio());
            contratoExistente.setFechaFin(contrato.getFechaFin());
            contratoExistente.setMontoMensual(contrato.getMontoMensual());
            contratoExistente.setMontoExpensas(contrato.getMontoExpensas());
            contratoExistente.setDiaVencimiento(contrato.getDiaVencimiento());
            contratoExistente.setPorcentajeMora(contrato.getPorcentajeMora());
            contratoExistente.setTipoIndice(contrato.getTipoIndice());
            contratoExistente.setFrecuenciaMeses(contrato.getFrecuenciaMeses());
            contratoExistente.setObservaciones(contrato.getObservaciones());
            contratoExistente.setModeloContrato(modeloContrato);
            contratoExistente.setExpensasOption(ContratoAlquiler.ExpensasOption.valueOf(expensasOption));

            // Actualizar campos para contratos existentes
            contratoExistente.setEsContratoExistente(esContratoExistente);
            if (Boolean.TRUE.equals(esContratoExistente)) {
                contratoExistente.setFechaInicioOriginal(fechaInicioOriginal);
                contratoExistente.setFechaInicioAdministracion(fechaInicioAdministracion);
                contratoExistente.setMontoInicial(montoInicial);
                if (montoMensualExistente != null) {
                    contratoExistente.setMontoMensual(montoMensualExistente);
                }
            }

            // Actualizar información de vehículo
            contratoExistente.setVehiculoMarca(vehiculoMarca);
            contratoExistente.setVehiculoModelo(vehiculoModelo);
            contratoExistente.setVehiculoColor(vehiculoColor);
            contratoExistente.setVehiculoPatente(vehiculoPatente);

            // Actualizar participantes adicionales
            List<ParticipacionContrato> participacionesExistentes = participacionRepository.findByContratoId(id);
            participacionRepository.deleteAll(participacionesExistentes);
            agregarTodasLasParticipaciones(contratoExistente, garantesIds, habitantesIds);

            contratoRepository.save(contratoExistente);

            redirectAttributes.addFlashAttribute("success", "Contrato actualizado exitosamente");
            return "redirect:/contratos/lista";

        } catch (Exception e) {
            System.err.println("❌ ERROR ACTUALIZANDO CONTRATO: " + e.getMessage());
            e.printStackTrace();
            inicializarModelo(model, contrato, "editar");
            model.addAttribute("error", "Error al actualizar: " + e.getMessage());
            return "contratos/formulario";
        }
    }

    // MÉTODOS PRIVADOS DE AYUDA
    private void inicializarModelo(Model model, ContratoAlquiler contrato, String modo) {
        model.addAttribute("contrato", contrato);
        model.addAttribute("propiedades", propiedadRepository.findByEstado("Disponible"));
        model.addAttribute("personas", personaRepository.findAllByOrderByApellidoAscNombreAsc());
        model.addAttribute("tiposIndice", TipoIndice.values());
        model.addAttribute("responsables", ResponsableServicio.values());
        model.addAttribute("lugaresPago", LugarPago.values());
        model.addAttribute("lugaresAdministracion", LugarAdministracion.values());
        model.addAttribute("expensasOptions", ContratoAlquiler.ExpensasOption.values());
        model.addAttribute("estados", EstadoContrato.values());
        model.addAttribute("modelosContrato", List.of(1, 2, 3, 4));
        model.addAttribute("modo", modo);
        model.addAttribute("titulo", "crear".equals(modo) ? "Nuevo Contrato" : "Editar Contrato");
    }

    private void agregarTodasLasParticipaciones(ContratoAlquiler contrato, List<Long> garantesIds, List<Long> habitantesIds) {
        try {
            // Agregar participantes principales
            agregarParticipacion(contrato, contrato.getLocador().getId(), RolContrato.LOCADOR);
            agregarParticipacion(contrato, contrato.getLocatario().getId(), RolContrato.LOCATARIO);
            agregarParticipacion(contrato, contrato.getSolicitante().getId(), RolContrato.SOLICITANTE);

            // Agregar garantes
            if (garantesIds != null) {
                for (Long garanteId : garantesIds) {
                    if (garanteId != null) {
                        agregarParticipacion(contrato, garanteId, RolContrato.GARANTE);
                    }
                }
            }

            // Agregar habitantes
            if (habitantesIds != null) {
                for (Long habitanteId : habitantesIds) {
                    if (habitanteId != null) {
                        agregarParticipacion(contrato, habitanteId, RolContrato.HABITANTE);
                    }
                }
            }

            System.out.println("✅ Todas las participaciones agregadas");
        } catch (Exception e) {
            System.err.println("❌ Error en participaciones: " + e.getMessage());
            throw new RuntimeException("Error al agregar participaciones", e);
        }
    }

    private void agregarParticipacion(ContratoAlquiler contrato, Long personaId, RolContrato rol) {
        try {
            Persona persona = personaRepository.findById(personaId)
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + personaId));

            ParticipacionContrato participacion = new ParticipacionContrato();
            participacion.setPersona(persona);
            participacion.setContrato(contrato);
            participacion.setRol(rol);

            participacionRepository.save(participacion);
            System.out.println("✅ Participación: " + persona.getNombreCompleto() + " como " + rol);

        } catch (Exception e) {
            System.err.println("❌ Error agregando participación: " + e.getMessage());
            throw e;
        }
    }

    private String generarNumeroContratoDefinitivo() {
        long totalContratos = contratoRepository.count();
        String fechaActual = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        return String.format("CON-%s-%04d", fechaActual, totalContratos + 1);
    }

    // LISTAR CONTRATOS
    @GetMapping("/listar")
    public String listarContratos(Model model) {
        List<ContratoAlquiler> contratos = contratoRepository.findAllByOrderByFechaCreacionDesc();
        model.addAttribute("contratos", contratos);
        model.addAttribute("titulo", "Lista de Contratos");

        model.addAttribute("totalContratos", contratos.size());
        model.addAttribute("contratosActivos", contratos.stream()
                .filter(c -> c.getEstado() == EstadoContrato.ACTIVO)
                .count());

        return "contratos/lista";
    }

    // EDITAR CONTRATO
    @GetMapping("/editar/{id}")
    public String editarContrato(@PathVariable Long id, Model model) {
        Optional<ContratoAlquiler> contratoOptional = contratoRepository.findById(id);

        if (contratoOptional.isEmpty()) {
            model.addAttribute("error", "Contrato no encontrado");
            return "redirect:/contratos/lista";
        }

        ContratoAlquiler contrato = contratoOptional.get();
        inicializarModelo(model, contrato, "editar");
        cargarParticipantesEnModelo(model, contrato);

        return "contratos/formulario";
    }

    private void cargarParticipantesEnModelo(Model model, ContratoAlquiler contrato) {
        if (contrato.getLocador() != null) {
            model.addAttribute("locadorSeleccionado", contrato.getLocador().getId());
        }
        if (contrato.getLocatario() != null) {
            model.addAttribute("locatarioSeleccionado", contrato.getLocatario().getId());
        }
        if (contrato.getSolicitante() != null) {
            model.addAttribute("solicitanteSeleccionado", contrato.getSolicitante().getId());
        }

        List<Long> garantesIds = contrato.getGarantes().stream()
                .map(Persona::getId)
                .toList();
        model.addAttribute("garantesSeleccionados", garantesIds);

        List<Long> habitantesIds = contrato.getHabitantes().stream()
                .map(Persona::getId)
                .toList();
        model.addAttribute("habitantesSeleccionados", habitantesIds);
    }

    // ELIMINAR CONTRATO
    @GetMapping("/eliminar/{id}")
    public String eliminarContrato(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<ContratoAlquiler> contratoOptional = contratoRepository.findById(id);

            if (contratoOptional.isPresent()) {
                ContratoAlquiler contrato = contratoOptional.get();

                Propiedad propiedad = contrato.getPropiedad();
                if (propiedad != null) {
                    propiedad.setEstado("Disponible");
                    propiedadRepository.save(propiedad);
                }

                contratoRepository.delete(contrato);
                redirectAttributes.addFlashAttribute("success", "Contrato eliminado exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Contrato no encontrado");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }

        return "redirect:/contratos/lista";
    }

    // VER DETALLES DEL CONTRATO
    @GetMapping("/detalles/{id}")
    public String verDetallesContrato(@PathVariable Long id, Model model) {
        Optional<ContratoAlquiler> contratoOptional = contratoRepository.findById(id);

        if (contratoOptional.isEmpty()) {
            model.addAttribute("error", "Contrato no encontrado");
            return "redirect:/contratos/lista";
        }

        ContratoAlquiler contrato = contratoOptional.get();
        model.addAttribute("contrato", contrato);
        model.addAttribute("titulo", "Detalles - " + contrato.getNumeroContrato());

        return "contratos/detalles";
    }
}