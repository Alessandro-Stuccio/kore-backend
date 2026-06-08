package com.project.kore.service;

import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/** Gestione degli abbonamenti degli utenti. */
@Validated
public interface SubscriptionService {

    /**
     * Abbonamento attivo dell'utente.
     *
     * @param user l'utente
     * @return l'abbonamento attualmente attivo
     * @throws CustomResourceNotFoundException se l'utente non ha un abbonamento attivo
     */
    Subscription getSubscriptionStatus(@NotNull User user);

    /**
     * Persiste l'abbonamento (creazione o aggiornamento).
     *
     * @param sub l'abbonamento da salvare
     * @return l'abbonamento salvato
     */
    Subscription save(@NotNull Subscription sub);

    /**
     * Cerca l'abbonamento attivo senza prendere lock.
     *
     * @param user l'utente
     * @return l'abbonamento attivo, se presente
     */
    Optional<Subscription> findActiveByUser(@NotNull User user);

    /**
     * Come findActiveByUser, ma con lock pessimistico sulla riga: usato quando si scalano i crediti.
     *
     * @param user l'utente
     * @return l'abbonamento attivo bloccato in scrittura, se presente
     */
    Optional<Subscription> findActiveByUserWithLock(@NotNull User user);

    /**
     * Tutti gli abbonamenti del sistema.
     *
     * @return l'elenco completo degli abbonamenti
     */
    List<Subscription> getAllSubscriptions();

    /**
     * Imposta i crediti PT e nutrizionista dell'abbonamento.
     *
     * @param subscriptionId id dell'abbonamento
     * @param creditsPT      crediti per il personal trainer da impostare
     * @param creditsNutri   crediti per il nutrizionista da impostare
     * @return l'abbonamento aggiornato
     * @throws CustomResourceNotFoundException se l'abbonamento non esiste
     */
    Subscription updateSubscriptionCredits(Long subscriptionId, int creditsPT, int creditsNutri);

    /**
     * Dice se qualche abbonamento attivo usa quel piano (serve prima di disabilitarlo).
     *
     * @param planId id del piano
     * @return {@code true} se esiste almeno un abbonamento collegato al piano
     */
    boolean hasSubscribersByPlan(@NotNull @Min(1) Long planId);
}
