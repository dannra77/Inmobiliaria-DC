package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.controller;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.Persona;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.Propiedad;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.PersonaRepository;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.PropiedadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/propiedades")
public class PropiedadController {

    @Autowired
    private PropiedadRepository propiedadRepository;

    @Autowired
    private PersonaRepository personaRepository;

    // LISTAR TODAS LAS PROPIEDADES (MÉTODO PRINCIPAL)
    @GetMapping("/listar")
    public String listarPropiedades(Model model,
                                    @RequestParam(required = false) String criterio,
                                    @RequestParam(required = false) String tipo,
                                    @RequestParam(required = false) String estado,
                                    @RequestParam(required = false) Boolean enAlquiler) {

        List<Propiedad> propiedades;
        String tituloEspecial = null;

        // Búsqueda por criterio
        if (criterio != null && !criterio.trim().isEmpty()) {
            propiedades = propiedadRepository.buscarPorCriterio(criterio.trim());
            model.addAttribute("criterio", criterio);
            tituloEspecial = "Resultados de búsqueda: " + criterio;
        }
        // Filtro por tipo
        else if (tipo != null && !tipo.trim().isEmpty()) {
            propiedades = propiedadRepository.findByTipo(tipo);
            model.addAttribute("tipo", tipo);
            tituloEspecial = "Propiedades tipo: " + tipo;
        }
        // Filtro por estado
        else if (estado != null && !estado.trim().isEmpty()) {
            propiedades = propiedadRepository.findByEstado(estado);
            model.addAttribute("estado", estado);
            tituloEspecial = "Propiedades estado: " + estado;
        }
        // Filtro por enAlquiler
        else if (enAlquiler != null) {
            if (enAlquiler) {
                propiedades = propiedadRepository.findByEnAlquilerTrue();
                tituloEspecial = "Propiedades en Alquiler";
            } else {
                propiedades = propiedadRepository.findByEnVentaTrue();
                tituloEspecial = "Propiedades en Venta";
            }
            model.addAttribute("enAlquiler", enAlquiler);
        }
        else {
            propiedades = propiedadRepository.findAll();
        }

        // Agregar atributos al modelo
        model.addAttribute("propiedades", propiedades);
        model.addAttribute("totalPropiedades", propiedades.size());

        if (tituloEspecial != null) {
            model.addAttribute("tituloEspecial", tituloEspecial);
            model.addAttribute("totalResultados", propiedades.size());
        }

        // Calcular estadísticas
        calcularEstadisticas(model, propiedades);

        // DEBUG: Mostrar en consola
        System.out.println("=== DEBUG PROPIEDADES ===");
        System.out.println("Total propiedades encontradas: " + propiedades.size());
        if (!propiedades.isEmpty()) {
            propiedades.forEach(p -> {
                System.out.println("ID: " + p.getId() +
                        " | Título: " + p.getTitulo() +
                        " | Estado: " + p.getEstado() +
                        " | Tipo: " + p.getTipo() +
                        " | En Alquiler: " + p.getEnAlquiler() +
                        " | En Venta: " + p.getEnVenta());
            });
        } else {
            System.out.println("NO SE ENCONTRARON PROPIEDADES");
        }

        return "propiedades/lista";
    }

    // MÉTODO AUXILIAR PARA CALCULAR ESTADÍSTICAS
    private void calcularEstadisticas(Model model, List<Propiedad> propiedades) {
        long totalDisponibles = propiedades.stream()
                .filter(p -> "Disponible".equals(p.getEstado()))
                .count();

        long totalEnAlquiler = propiedades.stream()
                .filter(p -> Boolean.TRUE.equals(p.getEnAlquiler()))
                .count();

        long totalEnVenta = propiedades.stream()
                .filter(p -> Boolean.TRUE.equals(p.getEnVenta()))
                .count();

        model.addAttribute("totalDisponibles", totalDisponibles);
        model.addAttribute("totalEnAlquiler", totalEnAlquiler);
        model.addAttribute("totalEnVenta", totalEnVenta);

        System.out.println("Estadísticas - Disponibles: " + totalDisponibles +
                ", Alquiler: " + totalEnAlquiler +
                ", Venta: " + totalEnVenta);
    }

    // PROPIEDADES DISPONIBLES PARA ALQUILER (USANDO TU MÉTODO DEL REPOSITORY)
    @GetMapping("/disponibles-alquiler")
    public String propiedadesDisponiblesAlquiler(Model model) {
        List<Propiedad> propiedades = propiedadRepository.findDisponiblesParaAlquiler();
        model.addAttribute("propiedades", propiedades);
        model.addAttribute("tituloEspecial", "Propiedades Disponibles para Alquiler");
        model.addAttribute("totalResultados", propiedades.size());
        calcularEstadisticas(model, propiedades);
        return "propiedades/lista";
    }

    // PROPIEDADES DISPONIBLES PARA VENTA (FILTRANDO MANUALMENTE)
    @GetMapping("/disponibles-venta")
    public String propiedadesDisponiblesVenta(Model model) {
        // Usamos findByEnVentaTrue y filtramos por estado "Disponible"
        List<Propiedad> todasPropiedades = propiedadRepository.findByEnVentaTrue();
        List<Propiedad> propiedades = todasPropiedades.stream()
                .filter(p -> "Disponible".equals(p.getEstado()))
                .toList();

        model.addAttribute("propiedades", propiedades);
        model.addAttribute("tituloEspecial", "Propiedades Disponibles para Venta");
        model.addAttribute("totalResultados", propiedades.size());
        calcularEstadisticas(model, propiedades);
        return "propiedades/lista";
    }

    // LOS DEMÁS MÉTODOS SE MANTIENEN IGUAL...
    @GetMapping("/nuevo")
    public String mostrarFormularioPropiedad(Model model) {
        List<Persona> personas = personaRepository.findAllByOrderByApellidoAscNombreAsc();
        model.addAttribute("propiedad", new Propiedad());
        model.addAttribute("personas", personas);
        model.addAttribute("modo", "crear");
        model.addAttribute("titulo", "Nueva Propiedad");
        return "propiedades/formulario";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Propiedad propiedad = propiedadRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada con ID: " + id));

            List<Persona> personas = personaRepository.findAllByOrderByApellidoAscNombreAsc();
            model.addAttribute("propiedad", propiedad);
            model.addAttribute("personas", personas);
            model.addAttribute("modo", "editar");
            model.addAttribute("titulo", "Editar Propiedad: " + propiedad.getTitulo());
            return "propiedades/formulario";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/propiedades/listar";
        }
    }

    @PostMapping("/guardar")
    public String guardarPropiedad(@Valid @ModelAttribute Propiedad propiedad,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            List<Persona> personas = personaRepository.findAllByOrderByApellidoAscNombreAsc();
            model.addAttribute("personas", personas);
            model.addAttribute("modo", propiedad.getId() == null ? "crear" : "editar");
            model.addAttribute("titulo", propiedad.getId() == null ? "Nueva Propiedad" : "Editar Propiedad");
            model.addAttribute("error", "Por favor, corrija los errores del formulario");
            return "propiedades/formulario";
        }

        try {
            if (!propiedad.getEnAlquiler() && !propiedad.getEnVenta()) {
                result.rejectValue("enAlquiler", "operacion.requerida",
                        "Debe seleccionar al menos una opción: En Venta o En Alquiler");

                List<Persona> personas = personaRepository.findAllByOrderByApellidoAscNombreAsc();
                model.addAttribute("personas", personas);
                model.addAttribute("modo", propiedad.getId() == null ? "crear" : "editar");
                return "propiedades/formulario";
            }

            if (propiedad.getPropietario() != null && propiedad.getPropietario().getId() != null) {
                Optional<Persona> propietario = personaRepository.findById(propiedad.getPropietario().getId());
                if (propietario.isEmpty()) {
                    result.rejectValue("propietario", "propietario.noEncontrado",
                            "El propietario seleccionado no existe");

                    List<Persona> personas = personaRepository.findAllByOrderByApellidoAscNombreAsc();
                    model.addAttribute("personas", personas);
                    model.addAttribute("modo", propiedad.getId() == null ? "crear" : "editar");
                    return "propiedades/formulario";
                }
            }

            if (propiedad.getId() == null && propiedad.getEstado() == null) {
                propiedad.setEstado("Disponible");
            }

            propiedadRepository.save(propiedad);

            String mensaje = propiedad.getId() == null ?
                    "Propiedad creada exitosamente" : "Propiedad actualizada exitosamente";

            redirectAttributes.addFlashAttribute("success", mensaje);
            return "redirect:/propiedades/listar";

        } catch (Exception e) {
            List<Persona> personas = personaRepository.findAllByOrderByApellidoAscNombreAsc();
            model.addAttribute("personas", personas);
            model.addAttribute("error", "Error al guardar la propiedad: " + e.getMessage());
            model.addAttribute("modo", propiedad.getId() == null ? "crear" : "editar");
            return "propiedades/formulario";
        }
    }

    @GetMapping("/ver/{id}")
    public String verPropiedad(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Propiedad propiedad = propiedadRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada con ID: " + id));

            model.addAttribute("propiedad", propiedad);
            model.addAttribute("esDetalle", true);
            return "propiedades/detalle";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/propiedades/listar";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPropiedad(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Propiedad propiedad = propiedadRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada con ID: " + id));

            String tituloPropiedad = propiedad.getTitulo();
            propiedadRepository.delete(propiedad);

            redirectAttributes.addFlashAttribute("success",
                    "Propiedad '" + tituloPropiedad + "' eliminada exitosamente");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar la propiedad. Puede tener contratos asociados.");
        }

        return "redirect:/propiedades/listar";
    }

    @GetMapping
    public String redirigirALista() {
        return "redirect:/propiedades/listar";
    }
}