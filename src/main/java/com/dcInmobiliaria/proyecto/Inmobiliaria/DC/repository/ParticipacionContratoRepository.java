package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.repository;

import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.ParticipacionContrato;
import com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model.RolContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipacionContratoRepository extends JpaRepository<ParticipacionContrato, Long> {

    // Buscar participaciones por contrato
    List<ParticipacionContrato> findByContratoId(Long contratoId);

    // Buscar participaciones por persona
    List<ParticipacionContrato> findByPersonaId(Long personaId);

    // Buscar participaciones por rol en un contrato
    List<ParticipacionContrato> findByContratoIdAndRol(Long contratoId, RolContrato rol);

    // Buscar participaciones activas por contrato
    @Query("SELECT p FROM ParticipacionContrato p WHERE p.contrato.id = :contratoId AND (p.fechaFinParticipacion IS NULL OR p.fechaFinParticipacion > CURRENT_DATE)")
    List<ParticipacionContrato> findParticipacionesActivasByContrato(@Param("contratoId") Long contratoId);

    // Verificar si una persona tiene un rol específico en un contrato
    @Query("SELECT COUNT(p) > 0 FROM ParticipacionContrato p WHERE p.persona.id = :personaId AND p.contrato.id = :contratoId AND p.rol = :rol")
    boolean existsByPersonaAndContratoAndRol(@Param("personaId") Long personaId,
                                             @Param("contratoId") Long contratoId,
                                             @Param("rol") RolContrato rol);
}