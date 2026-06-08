package com.project.kore.repository;

import com.project.kore.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Piani di abbonamento, con ricerca per nome univoco.
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    // Cerca un piano per nome (es. "Gold Annuale"): serve a evitare duplicati alla creazione.
    Optional<Plan> findByName(String name);

    // Solo i piani attivi, per la vista pubblica/client.
    List<Plan> findByActiveTrue();
}