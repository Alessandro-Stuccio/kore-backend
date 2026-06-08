package com.project.kore.controller;

import com.project.kore.dto.response.PlanResponseDTO;
import com.project.kore.facade.PlanFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint REST per i piani di abbonamento (pubblico).
 */
@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanFacade planFacade;

    public PlanController(PlanFacade planFacade) {
        this.planFacade = planFacade;
    }

    /**
     * Restituisce la lista di tutti i piani di abbonamento disponibili.
     *
     * @return 200 con i piani attivi
     */
    @GetMapping
    public ResponseEntity<List<PlanResponseDTO>> getAllPlans() {
        return ResponseEntity.ok(planFacade.getAllPlans());
    }
}
