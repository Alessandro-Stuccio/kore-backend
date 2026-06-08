package com.project.kore.service;

import com.project.kore.model.Review;
import com.project.kore.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** Gestione delle recensioni lasciate ai professionisti. */
@Validated
public interface ReviewService {

    /**
     * Persiste una recensione.
     *
     * @param review la recensione da salvare
     * @return la recensione salvata
     */
    Review save(@NotNull Review review);

    /**
     * Dice se quel cliente ha già recensito quel professionista (ne è ammessa una sola).
     *
     * @param clientId       id del cliente
     * @param professionalId id del professionista
     * @return {@code true} se esiste già una recensione del cliente per quel professionista
     */
    boolean existsByClientAndProfessional(@NotNull Long clientId, @NotNull Long professionalId);

    /**
     * Recensioni ricevute dal professionista.
     *
     * @param professional il professionista
     * @return le recensioni a lui associate
     */
    List<Review> findByProfessional(@NotNull User professional);

    /**
     * Media dei voti ricevuti dal professionista.
     *
     * @param professionalId id del professionista
     * @return la media dei voti, oppure 0.0 se non ci sono recensioni
     */
    double getAverageRating(@NotNull Long professionalId);
}
