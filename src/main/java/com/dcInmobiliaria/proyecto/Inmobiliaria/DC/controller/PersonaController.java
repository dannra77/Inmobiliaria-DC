package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.controller;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.Persona;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/personas")
public class PersonaController {

    @Autowired
    private PersonaRepository personaRepository;

    // MOSTRAR FORMULARIO DE CREACIÓN
    @GetMapping("/nuevo")
    public String mostrarFormularioPersona(Model model) {
        model.addAttribute("persona", new Persona());
        return "personas/formulario";  // ✅ CORREGIDO: ahora en carpeta personas
    }

    // MOSTRAR FORMULARIO DE EDICIÓN
    @GetMapping("/editar/{id}")
    public String editarPersona(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Persona persona = personaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));

            model.addAttribute("persona", persona);
            return "personas/formulario";  // ✅ CORREGIDO: ahora en carpeta personas

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/personas/listar";
        }
    }

    // GUARDAR PERSONA (CREAR O ACTUALIZAR)
    @PostMapping("/guardar")
    public String guardarPersona(
            @Valid @ModelAttribute("persona") Persona persona,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validar errores de formulario
        if (result.hasErrors()) {
            model.addAttribute("persona", persona);
            return "personas/formulario";  // ✅ CORREGIDO: ahora en carpeta personas
        }

        try {
            // Validar DNI único para nuevas personas
            if (persona.getId() == null && personaRepository.existsByDni(persona.getDni())) {
                result.rejectValue("dni", "dni.duplicado",
                        "Ya existe una persona con el DNI: " + persona.getDni());
                model.addAttribute("persona", persona);
                return "personas/formulario";  // ✅ CORREGIDO: ahora en carpeta personas
            }

            // Validar DNI único para personas existentes (si cambió)
            if (persona.getId() != null) {
                Persona personaExistente = personaRepository.findById(persona.getId()).orElse(null);
                if (personaExistente != null &&
                        !personaExistente.getDni().equals(persona.getDni()) &&
                        personaRepository.existsByDni(persona.getDni())) {
                    result.rejectValue("dni", "dni.duplicado",
                            "Ya existe otra persona con el DNI: " + persona.getDni());
                    model.addAttribute("persona", persona);
                    return "personas/formulario";  // ✅ CORREGIDO: ahora en carpeta personas
                }
            }

            // Guardar la persona
            Persona personaGuardada = personaRepository.save(persona);

            redirectAttributes.addFlashAttribute("success",
                    "Persona " + (persona.getId() == null ? "creada" : "actualizada") + " exitosamente");

            return "redirect:/personas/listar";

        } catch (Exception e) {
            model.addAttribute("persona", persona);
            model.addAttribute("error", "Error al guardar la persona: " + e.getMessage());
            return "personas/formulario";  // ✅ CORREGIDO: ahora en carpeta personas
        }
    }

    // LISTAR PERSONAS
    @GetMapping("/listar")
    public String listarPersonas(Model model) {
        List<Persona> personas = personaRepository.findAllByOrderByApellidoAscNombreAsc();
        model.addAttribute("personas", personas);
        model.addAttribute("totalPersonas", personas.size());
        return "personas/lista";  // ✅ CORREGIDO: ahora en carpeta personas
    }

    // VER DETALLES
    @GetMapping("/ver/{id}")
    public String verPersona(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Persona persona = personaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));
            model.addAttribute("persona", persona);
            return "personas/detalle";  // ✅ CORREGIDO: ahora en carpeta personas

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/personas/listar";
        }
    }

    // ELIMINAR PERSONA
    @GetMapping("/eliminar/{id}")
    public String eliminarPersona(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Persona persona = personaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada con ID: " + id));

            String nombreCompleto = persona.getNombreCompleto();
            personaRepository.delete(persona);

            redirectAttributes.addFlashAttribute("success",
                    "Persona '" + nombreCompleto + "' eliminada exitosamente");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar la persona. Puede estar asociada a contratos.");
        }
        return "redirect:/personas/listar";
    }

    // BÚSQUEDA
    @GetMapping("/buscar")
    public String buscarPersonas(
            @RequestParam String q,
            Model model) {

        List<Persona> resultados = personaRepository.buscarPorCriterio(q.trim());

        model.addAttribute("personas", resultados);
        model.addAttribute("criterioBusqueda", q);
        model.addAttribute("totalResultados", resultados.size());
        model.addAttribute("esBusqueda", true);

        return "personas/lista";  // ✅ CORREGIDO: ahora en carpeta personas
    }

    // REDIRECCIÓN DESDE /personas
    @GetMapping
    public String redirigirALista() {
        return "redirect:/personas/listar";
    }
}