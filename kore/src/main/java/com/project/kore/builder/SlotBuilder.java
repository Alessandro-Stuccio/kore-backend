package com.project.kore.builder;

import com.project.kore.model.Slot;
import com.project.kore.model.User;

import java.time.LocalDateTime;

/**
 * Costruisce uno Slot di disponibilità di un professionista, con interfaccia fluente.
 */
public interface SlotBuilder {

    /**
     * Imposta l'id dello slot.
     *
     * @param id identificativo dello slot
     * @return questo builder, per concatenare le chiamate
     */
    SlotBuilder id(Long id);

    /**
     * Imposta il professionista proprietario dello slot.
     *
     * @param professional il professionista titolare della disponibilità
     * @return questo builder, per concatenare le chiamate
     */
    SlotBuilder professional(User professional);

    /**
     * Imposta l'orario di inizio dello slot.
     *
     * @param startTime data/ora di inizio
     * @return questo builder, per concatenare le chiamate
     */
    SlotBuilder startTime(LocalDateTime startTime);

    /**
     * Imposta l'orario di fine dello slot.
     *
     * @param endTime data/ora di fine
     * @return questo builder, per concatenare le chiamate
     */
    SlotBuilder endTime(LocalDateTime endTime);

    /**
     * Imposta il cliente che ha prenotato lo slot.
     *
     * @param bookedBy il cliente che prenota; {@code null} se lo slot è libero
     * @return questo builder, per concatenare le chiamate
     */
    SlotBuilder bookedBy(User bookedBy);

    /**
     * Imposta la versione usata per il locking ottimistico.
     *
     * @param version valore di versione
     * @return questo builder, per concatenare le chiamate
     */
    SlotBuilder version(Integer version);

    /**
     * Imposta il timestamp in cui lo slot è stato prenotato.
     *
     * @param bookedAt data/ora della prenotazione
     * @return questo builder, per concatenare le chiamate
     */
    SlotBuilder bookedAt(LocalDateTime bookedAt);

    /**
     * Costruisce lo Slot con i valori impostati.
     *
     * @return lo slot costruito
     */
    Slot build();
}
