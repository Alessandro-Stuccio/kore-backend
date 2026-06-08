package com.project.kore.enums;

/**
 * Tipologia di documento caricato sulla piattaforma. Ogni valore porta con sé
 * una descrizione leggibile (getDesc) usata nelle email e nel feed attività.
 */
public enum DocumentType {
    INSURANCE_POLICE("polizza"),
    DIET_PLAN("dieta"),
    WORKOUT_PLAN("scheda di allenamento");

    private final String desc;

    /**
     * Descrizione leggibile del tipo di documento, usata in email e feed attività.
     *
     * @return la descrizione in linguaggio naturale
     */
    public String getDesc() {
        return desc;
    }

    DocumentType(String desc) {
        this.desc = desc;
    }
}
