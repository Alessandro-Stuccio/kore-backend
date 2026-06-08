package com.project.kore.service.strategy;

import com.project.kore.enums.Role;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;

/**
 * Racchiude le regole di prenotazione che cambiano in base al tipo di professionista
 * (PT o nutrizionista): controllo dell'assegnazione cliente-professionista e gestione
 * dei crediti dedicati sull'abbonamento. SlotServiceImpl sceglie l'implementazione giusta
 * a runtime in base al ruolo.
 */
public interface BookingStrategy {

    /** Ruolo gestito da questa strategia (PERSONAL_TRAINER o NUTRITIONIST). */
    Role getSupportedRole();

    /**
     * Controlla che il cliente sia davvero assegnato a quel professionista.
     *
     * @throws com.project.kore.exception.booking.ProfessionalNotAssignedException se non lo è
     */
    void verifyAssignment(User client, User professional);

    /**
     * Scala un credito dall'abbonamento, ma solo se ce n'è almeno uno.
     *
     * @throws com.project.kore.exception.booking.InsufficientCreditsException se i crediti sono esauriti
     */
    void consumeCredits(Subscription subscription);

    /** Riaccredita un credito: è l'inverso di consumeCredits, usato quando si annulla una prenotazione. */
    void refundCredits(Subscription subscription);
}
