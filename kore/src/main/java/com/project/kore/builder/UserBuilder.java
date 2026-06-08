package com.project.kore.builder;

import com.project.kore.enums.Role;
import java.time.LocalDateTime;
import com.project.kore.model.*;


/**
 * Costruisce uno User della piattaforma con interfaccia fluente.
 */
public interface UserBuilder {

    /**
     * Imposta l'id dell'utente.
     *
     * @param id identificativo dell'utente
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder id(Long id);

    /**
     * Imposta l'email, che fa anche da username.
     *
     * @param email indirizzo email
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder email(String email);

    /**
     * Imposta la password (attesa già cifrata).
     *
     * @param password password cifrata
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder password(String password);

    /**
     * Imposta l'immagine di profilo.
     *
     * @param profilePicture riferimento all'immagine di profilo
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder profilePicture(String profilePicture);

    /**
     * Imposta il nome.
     *
     * @param firstName nome dell'utente
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder firstName(String firstName);

    /**
     * Imposta il cognome.
     *
     * @param lastName cognome dell'utente
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder lastName(String lastName);

    /**
     * Imposta il ruolo dell'utente.
     *
     * @param role ruolo che ne determina permessi e funzionalità
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder role(Role role);

    /**
     * Imposta il personal trainer assegnato (per i clienti).
     *
     * @param assignedPT il personal trainer assegnato
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder assignedPT(User assignedPT);

    /**
     * Imposta il nutrizionista assegnato (per i clienti).
     *
     * @param assignedNutritionist il nutrizionista assegnato
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder assignedNutritionist(User assignedNutritionist);

    /**
     * Imposta la data di creazione dell'utente.
     *
     * @param createdAt data/ora di creazione
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder createdAt(LocalDateTime createdAt);

    /**
     * Imposta la data dell'ultimo aggiornamento.
     *
     * @param updatedAt data/ora dell'ultimo aggiornamento
     * @return questo builder, per concatenare le chiamate
     */
    UserBuilder updatedAt(LocalDateTime updatedAt);

    /**
     * Costruisce lo User con i valori impostati.
     *
     * @return l'utente costruito
     */
    User build();
}
