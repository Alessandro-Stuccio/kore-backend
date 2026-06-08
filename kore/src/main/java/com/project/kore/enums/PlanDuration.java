package com.project.kore.enums;

/**
 * Durata di un piano di abbonamento. A ogni valore è legato il numero di mesi
 * (getMonths), da cui si calcola la data di scadenza.
 */
public enum PlanDuration {
    SEMESTRALE(6),
    ANNUALE(12);

    private final int months;

    PlanDuration(int months) {
        this.months = months;
    }

    /**
     * Numero di mesi coperti dal piano, usato per calcolare la data di scadenza.
     *
     * @return la durata in mesi
     */
    public int getMonths() {
        return months;
    }
}