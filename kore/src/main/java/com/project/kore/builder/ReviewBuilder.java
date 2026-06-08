package com.project.kore.builder;

import java.time.LocalDateTime;
import com.project.kore.model.*;


/**
 * Costruisce una Review, cioè una recensione, con interfaccia fluente.
 */
public interface ReviewBuilder {

    /**
     * Imposta l'id della recensione.
     *
     * @param id identificativo della recensione
     * @return questo builder, per concatenare le chiamate
     */
    ReviewBuilder id(Long id);

    /**
     * Imposta il cliente autore della recensione.
     *
     * @param client il cliente che recensisce
     * @return questo builder, per concatenare le chiamate
     */
    ReviewBuilder client(User client);

    /**
     * Imposta il professionista recensito.
     *
     * @param professional il professionista oggetto della recensione
     * @return questo builder, per concatenare le chiamate
     */
    ReviewBuilder professional(User professional);

    /**
     * Imposta il voto assegnato.
     *
     * @param rating voto della recensione
     * @return questo builder, per concatenare le chiamate
     */
    ReviewBuilder rating(int rating);

    /**
     * Imposta il commento testuale.
     *
     * @param comment testo della recensione
     * @return questo builder, per concatenare le chiamate
     */
    ReviewBuilder comment(String comment);

    /**
     * Imposta la data di creazione della recensione.
     *
     * @param createdAt data/ora di creazione
     * @return questo builder, per concatenare le chiamate
     */
    ReviewBuilder createdAt(LocalDateTime createdAt);

    /**
     * Costruisce la Review con i valori impostati.
     *
     * @return la recensione costruita
     */
    Review build();
}
