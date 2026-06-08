package com.project.kore.controller;

import com.project.kore.dto.request.PlanCreateRequestDTO;
import com.project.kore.dto.response.PlanResponseDTO;
import com.project.kore.dto.response.stats.AdminStatsResponse;
import com.project.kore.facade.AdminFacade;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Operazioni di amministrazione sui piani e statistiche globali. /api/admin, richiede ruolo ADMIN. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminFacade adminFacade;

    public AdminController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }


    /**
     * Crea un nuovo piano di abbonamento.
     *
     * @param request dati del piano da creare
     * @return 200 con il piano creato
     */
    @PostMapping("/plans")
    public ResponseEntity<PlanResponseDTO> createPlan(@Valid @RequestBody PlanCreateRequestDTO request) {
        return ResponseEntity.ok(adminFacade.createPlan(request));
    }

    /**
     * Aggiorna un piano di abbonamento esistente.
     *
     * @param id      id del piano da aggiornare
     * @param request dati aggiornati del piano
     * @return 200 con il piano aggiornato
     */
    @PutMapping("/plans/{id}")
    public ResponseEntity<PlanResponseDTO> updatePlan(@PathVariable Long id,
            @Valid @RequestBody PlanCreateRequestDTO request) {
        return ResponseEntity.ok(adminFacade.updatePlan(id, request));
    }

    /**
     * Tutti i piani, inclusi quelli disabilitati (vista amministrativa, non quella pubblica).
     *
     * @return 200 con l'elenco completo dei piani
     */
    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponseDTO>> getAllPlans() {
        return ResponseEntity.ok(adminFacade.getAllPlansForAdmin());
    }

    /**
     * Disabilita un piano (resta in DB). Solo se non ha abbonamenti collegati.
     *
     * @param id id del piano da disabilitare
     * @return 200 con il piano aggiornato
     */
    @PatchMapping("/plans/{id}/disable")
    public ResponseEntity<PlanResponseDTO> disablePlan(@PathVariable Long id) {
        return ResponseEntity.ok(adminFacade.setPlanStatus(id, false));
    }

    /**
     * Riabilita un piano precedentemente disabilitato.
     *
     * @param id id del piano da riabilitare
     * @return 200 con il piano aggiornato
     */
    @PatchMapping("/plans/{id}/enable")
    public ResponseEntity<PlanResponseDTO> enablePlan(@PathVariable Long id) {
        return ResponseEntity.ok(adminFacade.setPlanStatus(id, true));
    }

    /**
     * Statistiche aggregate per la dashboard admin: utenti attivi, prenotazioni, abbonamenti, ecc.
     *
     * @return 200 con le statistiche globali del sistema
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminFacade.getAdminStats());
    }
}
