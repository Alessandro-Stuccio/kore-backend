package com.project.kore.service;

import com.project.kore.exception.common.CustomResourceNotFoundException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import com.project.kore.model.Plan;
import java.util.List;

/** Gestione dei piani di abbonamento. */
@Validated
public interface PlanService {

    /**
     * Tutti i piani, anche quelli disabilitati: serve alla vista admin e alle statistiche.
     *
     * @return l'elenco completo dei piani
     */
    List<Plan> getAllPlans();

    /**
     * Solo i piani attivi, per la vista pubblica/client.
     *
     * @return i piani attualmente attivi
     */
    List<Plan> getActivePlans();

    /**
     * Recupera un piano dal suo id.
     *
     * @param id id del piano
     * @return il piano trovato
     * @throws CustomResourceNotFoundException se il piano non esiste
     */
    Plan getPlanById(@NotNull @Min(1) Long id);

    /**
     * Crea un nuovo piano.
     *
     * @param plan il piano da creare
     * @return il piano salvato
     */
    Plan createPlan(@NotNull Plan plan);

    /**
     * Attiva o disabilita un piano. È un soft-disable: il record resta in DB.
     *
     * @param id     id del piano
     * @param active {@code true} per attivarlo, {@code false} per disabilitarlo
     * @return il piano aggiornato
     * @throws CustomResourceNotFoundException se il piano non esiste
     */
    Plan setActive(@NotNull @Min(1) Long id, boolean active);

    /**
     * Verifica se esiste già un piano con quel nome.
     *
     * @param name nome del piano
     * @return {@code true} se il nome è già usato
     */
    boolean existsByName(@NotNull String name);
}
