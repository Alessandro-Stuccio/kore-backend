package com.project.kore.facade;

import com.project.kore.dto.request.PlanCreateRequestDTO;
import com.project.kore.dto.response.PlanResponseDTO;
import com.project.kore.dto.response.stats.AdminStatsResponse;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Operazioni amministrative: oltre a quanto offerto da ModeratorFacade, aggiunge
 * la gestione dei piani e le statistiche globali.
 */
@Validated
public interface AdminFacade {

    /**
     * Crea un nuovo piano di abbonamento.
     *
     * @param request dati del piano da creare
     * @return il piano creato
     * @throws ResourceAlreadyExistsException se esiste già un piano con quel nome
     * @throws IllegalArgumentException       se i dati del piano non sono validi (es. durata non riconosciuta)
     */
    PlanResponseDTO createPlan(@Valid PlanCreateRequestDTO request);

    /**
     * Aggiorna un piano esistente.
     *
     * @param id      id del piano da aggiornare
     * @param request dati aggiornati del piano
     * @return il piano aggiornato
     * @throws ResourceAlreadyExistsException se il nuovo nome collide con un altro piano
     * @throws IllegalArgumentException       se i dati del piano non sono validi (es. durata non riconosciuta)
     */
    PlanResponseDTO updatePlan(Long id, @Valid PlanCreateRequestDTO request);

    /**
     * Tutti i piani, compresi quelli disabilitati, per la gestione amministrativa.
     *
     * @return l'elenco completo dei piani
     */
    List<PlanResponseDTO> getAllPlansForAdmin();

    /**
     * Abilita o disabilita un piano (soft-disable: il record resta in DB). Disabilitare
     * è permesso solo se al piano non sono collegati abbonamenti.
     *
     * @param id     id del piano
     * @param active {@code true} per abilitarlo, {@code false} per disabilitarlo
     * @return il piano aggiornato
     * @throws IllegalStateException se il piano ha abbonamenti collegati
     */
    PlanResponseDTO setPlanStatus(Long id, boolean active);

    /**
     * Statistiche globali della piattaforma.
     *
     * @return le statistiche aggregate del sistema
     */
    AdminStatsResponse getAdminStats();
}
