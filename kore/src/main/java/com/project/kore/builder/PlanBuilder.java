package com.project.kore.builder;

import com.project.kore.enums.PlanDuration;
import com.project.kore.model.*;


/**
 * Costruisce un Plan, cioè un piano di abbonamento, con interfaccia fluente.
 */
public interface PlanBuilder {

    /**
     * Imposta l'id del piano.
     *
     * @param id identificativo del piano
     * @return questo builder, per concatenare le chiamate
     */
    PlanBuilder id(Long id);

    /**
     * Imposta il nome del piano.
     *
     * @param name nome del piano
     * @return questo builder, per concatenare le chiamate
     */
    PlanBuilder name(String name);

    /**
     * Imposta la durata del piano (es. semestrale o annuale).
     *
     * @param duration durata del piano
     * @return questo builder, per concatenare le chiamate
     */
    PlanBuilder duration(PlanDuration duration);

    /**
     * Imposta il prezzo in un'unica soluzione.
     *
     * @param fullPrice prezzo totale lump-sum
     * @return questo builder, per concatenare le chiamate
     */
    PlanBuilder fullPrice(Double fullPrice);

    /**
     * Imposta il prezzo della singola rata mensile.
     *
     * @param monthlyInstallmentPrice importo della rata mensile
     * @return questo builder, per concatenare le chiamate
     */
    PlanBuilder monthlyInstallmentPrice(Double monthlyInstallmentPrice);

    /**
     * Imposta i crediti mensili per il personal trainer.
     *
     * @param monthlyCreditsPT crediti PT erogati ogni mese
     * @return questo builder, per concatenare le chiamate
     */
    PlanBuilder monthlyCreditsPT(int monthlyCreditsPT);

    /**
     * Imposta i crediti mensili per il nutrizionista.
     *
     * @param monthlyCreditsNutri crediti nutrizionista erogati ogni mese
     * @return questo builder, per concatenare le chiamate
     */
    PlanBuilder monthlyCreditsNutri(int monthlyCreditsNutri);

    /**
     * Imposta se il piano è attivo.
     *
     * @param active {@code true} se il piano è selezionabile
     * @return questo builder, per concatenare le chiamate
     */
    PlanBuilder active(boolean active);

    /**
     * Costruisce il Plan con i valori impostati.
     *
     * @return il piano costruito
     */
    Plan build();
}
