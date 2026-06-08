package com.project.kore.enums;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Ruoli degli utenti: stabiliscono permessi e funzionalità accessibili.
 */
public enum Role {
    CLIENT,
    PERSONAL_TRAINER,
    NUTRITIONIST,
    MODERATOR,
    INSURANCE_MANAGER,
    ADMIN;

    /**
     * Ruoli che il ruolo dato può gestire (creare, modificare, eliminare).
     *
     * @param role il ruolo di cui calcolare i permessi di gestione
     * @return l'insieme dei ruoli gestibili, eventualmente vuoto
     */
    public static Set<Role> getManagebleRoles(Role role) {
        return switch (role) {
            case MODERATOR -> EnumSet.of(Role.CLIENT, Role.PERSONAL_TRAINER, Role.NUTRITIONIST);
            case INSURANCE_MANAGER -> EnumSet.of(Role.ADMIN);
            case ADMIN -> EnumSet.of(Role.CLIENT, Role.PERSONAL_TRAINER, Role.NUTRITIONIST, Role.MODERATOR, Role.INSURANCE_MANAGER);
            default -> new HashSet<>();
        };
    }

}