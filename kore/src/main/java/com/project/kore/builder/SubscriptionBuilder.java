package com.project.kore.builder;

import com.project.kore.enums.PaymentFrequency;
import java.time.LocalDate;
import com.project.kore.model.*;


/**
 * Costruisce una Subscription, cioè l'abbonamento di un utente, con interfaccia fluente.
 */
public interface SubscriptionBuilder {

    /**
     * Imposta l'id dell'abbonamento.
     *
     * @param id identificativo dell'abbonamento
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder id(Long id);

    /**
     * Imposta l'utente titolare dell'abbonamento.
     *
     * @param user l'utente abbonato
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder user(User user);

    /**
     * Imposta il piano sottoscritto.
     *
     * @param plan il piano di abbonamento
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder plan(Plan plan);

    /**
     * Imposta la frequenza di pagamento.
     *
     * @param paymentFrequency unica soluzione o a rate
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder paymentFrequency(PaymentFrequency paymentFrequency);

    /**
     * Imposta il numero di rate già pagate.
     *
     * @param installmentsPaid rate pagate finora
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder installmentsPaid(int installmentsPaid);

    /**
     * Imposta il numero totale di rate previste.
     *
     * @param totalInstallments rate totali del piano di pagamento
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder totalInstallments(int totalInstallments);

    /**
     * Imposta la data del prossimo addebito.
     *
     * @param nextPaymentDate data del prossimo pagamento
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder nextPaymentDate(LocalDate nextPaymentDate);

    /**
     * Imposta la data di inizio dell'abbonamento.
     *
     * @param startDate data di decorrenza
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder startDate(LocalDate startDate);

    /**
     * Imposta la data di scadenza dell'abbonamento.
     *
     * @param endDate data di fine
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder endDate(LocalDate endDate);

    /**
     * Imposta se l'abbonamento è attivo.
     *
     * @param active {@code true} se attivo
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder active(boolean active);

    /**
     * Imposta i crediti correnti per il personal trainer.
     *
     * @param currentCreditsPT crediti PT residui nel mese
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder currentCreditsPT(int currentCreditsPT);

    /**
     * Imposta i crediti correnti per il nutrizionista.
     *
     * @param currentCreditsNutri crediti nutrizionista residui nel mese
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder currentCreditsNutri(int currentCreditsNutri);

    /**
     * Imposta la data dell'ultimo rinnovo mensile dei crediti.
     *
     * @param lastRenewalDate data dell'ultimo rinnovo
     * @return questo builder, per concatenare le chiamate
     */
    SubscriptionBuilder lastRenewalDate(LocalDate lastRenewalDate);

    /**
     * Costruisce la Subscription con i valori impostati.
     *
     * @return l'abbonamento costruito
     */
    Subscription build();
}
