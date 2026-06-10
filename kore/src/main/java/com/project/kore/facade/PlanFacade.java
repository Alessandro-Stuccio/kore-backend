package com.project.kore.facade;

import com.project.kore.dto.response.PlanResponse;

import java.util.List;

/**
 * Gestione dei piani di abbonamento.
 */
public interface PlanFacade {

    /**
     * I soli piani attivi, per la vista pubblica/cliente.
     *
     * @return i piani attualmente attivi
     */
    List<PlanResponse> getAllPlans();
}
