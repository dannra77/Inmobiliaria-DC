package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.controller;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.Persona;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository.PersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/personas")
@CrossOrigin(origins = "*")
public class PersonasRestController {

    @Autowired
    private PersonaRepository personaRepository;

    // GET - Obtener todas las personas con paginación opcional
    @GetMapping
    public ResponseEntity<List<Persona>> getAllPersonas(
            @RequestParam(defaultValue = "false") boolean ordenarPorNombre) {

        List<Persona> personas = ordenarPorNombre ?
                personaRepository.findAllByOrderByApellidoAscNombreAsc() :
                personaRepository.findAll();

        return ResponseEntity.ok(personas);
    }

    // GET - Por ID
    @GetMapping("/{id}")
    public ResponseEntity<Persona> getPersonaById(@PathVariable Long id) {
        return personaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST - Crear nueva persona con validación mejorada
    @PostMapping
    public ResponseEntity<?> createPersona(@Valid @RequestBody Persona persona) {
        // Validar DNI único
        if (personaRepository.existsByDni(persona.getDni())) {
            return ResponseEntity.badRequest()
                    .body(crearErrorResponse("Ya existe una persona con el DNI: " + persona.getDni()));
        }

        // Validar email único (si se proporciona)
        if (persona.getEmail() != null && !persona.getEmail().isEmpty() &&
                personaRepository.existsByEmail(persona.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(crearErrorResponse("Ya existe una persona con el email: " + persona.getEmail()));
        }

        Persona personaGuardada = personaRepository.save(persona);
        return ResponseEntity.ok(personaGuardada);
    }

    // PUT - Actualizar persona
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePersona(@PathVariable Long id, @Valid @RequestBody Persona personaDetails) {
        return personaRepository.findById(id)
                .map(personaExistente -> {
                    // Validar DNI único (si cambió)
                    if (!personaExistente.getDni().equals(personaDetails.getDni()) &&
                            personaRepository.existsByDni(personaDetails.getDni())) {
                        return ResponseEntity.badRequest()
                                .body(crearErrorResponse("Ya existe otra persona con el DNI: " + personaDetails.getDni()));
                    }

                    // Validar email único (si cambió)
                    if (personaDetails.getEmail() != null &&
                            !personaDetails.getEmail().equals(personaExistente.getEmail()) &&
                            personaRepository.existsByEmail(personaDetails.getEmail())) {
                        return ResponseEntity.badRequest()
                                .body(crearErrorResponse("Ya existe otra persona con el email: " + personaDetails.getEmail()));
                    }

                    // Actualizar campos
                    actualizarCamposPersona(personaExistente, personaDetails);
                    Persona personaActualizada = personaRepository.save(personaExistente);
                    return ResponseEntity.ok(personaActualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE - Eliminar persona
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersona(@PathVariable Long id) {
        if (personaRepository.existsById(id)) {
            personaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // BÚSQUEDAS ESPECÍFICAS
    @GetMapping("/dni/{dni}")
    public ResponseEntity<Persona> getPersonaByDni(@PathVariable String dni) {
        return personaRepository.findByDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Persona> getPersonaByEmail(@PathVariable String email) {
        return personaRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // BÚSQUEDA AVANZADA
    @GetMapping("/buscar")
    public ResponseEntity<List<Persona>> searchPersonas(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String localidad,
            @RequestParam(required = false) String criterio) {

        List<Persona> resultados;

        if (criterio != null && !criterio.trim().isEmpty()) {
            resultados = personaRepository.buscarPorCriterio(criterio.trim());
        } else if (nombre != null && apellido != null) {
            resultados = personaRepository.findByNombreContainingIgnoreCaseAndApellidoContainingIgnoreCase(nombre, apellido);
        } else if (nombre != null) {
            resultados = personaRepository.findByNombreContainingIgnoreCase(nombre);
        } else if (apellido != null) {
            resultados = personaRepository.findByApellidoContainingIgnoreCase(apellido);
        } else if (localidad != null) {
            resultados = personaRepository.findByLocalidadContainingIgnoreCase(localidad);
        } else {
            resultados = personaRepository.findAll();
        }

        return ResponseEntity.ok(resultados);
    }

    // ESTADÍSTICAS
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalPersonas", personaRepository.count());
        estadisticas.put("personasPorLocalidad", personaRepository.contarPersonasPorLocalidad());
        estadisticas.put("personasPorProfesion", personaRepository.contarPersonasPorProfesion());
        estadisticas.put("personasSinEmail", personaRepository.findByEmailIsNull().size());

        return ResponseEntity.ok(estadisticas);
    }

    // PERSONAS RECIENTES
    @GetMapping("/recientes")
    public ResponseEntity<List<Persona>> getPersonasRecientes() {
        LocalDate hace30Dias = LocalDate.now().minusDays(30);
        List<Persona> recientes = personaRepository.findByFechaAltaBetween(hace30Dias, LocalDate.now());
        return ResponseEntity.ok(recientes);
    }

    // MÉTODOS PRIVADOS DE AYUDA
    private void actualizarCamposPersona(Persona existente, Persona nuevosDatos) {
        existente.setNombre(nuevosDatos.getNombre());
        existente.setApellido(nuevosDatos.getApellido());
        existente.setDni(nuevosDatos.getDni());
        existente.setDireccion(nuevosDatos.getDireccion());
        existente.setLocalidad(nuevosDatos.getLocalidad());
        existente.setProvincia(nuevosDatos.getProvincia());
        existente.setNacionalidad(nuevosDatos.getNacionalidad()); // NUEVO
        existente.setEmail(nuevosDatos.getEmail());
        existente.setTelefono(nuevosDatos.getTelefono());
        existente.setCuitCuil(nuevosDatos.getCuitCuil());
        existente.setFechaNacimiento(nuevosDatos.getFechaNacimiento());
        existente.setLugarTrabajo(nuevosDatos.getLugarTrabajo());
        existente.setProfesion(nuevosDatos.getProfesion());
        existente.setNumeroCuentaBancaria(nuevosDatos.getNumeroCuentaBancaria());
        existente.setCajaAhorro(nuevosDatos.getCajaAhorro()); // NUEVO
        existente.setCbu(nuevosDatos.getCbu()); // NUEVO
        existente.setAlias(nuevosDatos.getAlias()); // NUEVO
        existente.setFacebook(nuevosDatos.getFacebook());
        existente.setInstagram(nuevosDatos.getInstagram());
        existente.setObservaciones(nuevosDatos.getObservaciones());
    }

    private Map<String, String> crearErrorResponse(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        error.put("timestamp", LocalDate.now().toString());
        return error;
    }
}