package com.project.kore.builder;

import java.time.LocalDateTime;
import java.util.List;
import com.project.kore.model.*;


/**
 * Costruisce una Chat, cioè la conversazione tra due utenti, con interfaccia fluente.
 */
public interface ChatBuilder {

    /**
     * Imposta l'id della chat.
     *
     * @param id identificativo della chat
     * @return questo builder, per concatenare le chiamate
     */
    ChatBuilder id(Long id);

    /**
     * Imposta il primo partecipante.
     *
     * @param user1 primo utente della conversazione
     * @return questo builder, per concatenare le chiamate
     */
    ChatBuilder user1(User user1);

    /**
     * Imposta il secondo partecipante.
     *
     * @param user2 secondo utente della conversazione
     * @return questo builder, per concatenare le chiamate
     */
    ChatBuilder user2(User user2);

    /**
     * Imposta i messaggi della chat.
     *
     * @param messages elenco dei messaggi
     * @return questo builder, per concatenare le chiamate
     */
    ChatBuilder messages(List<Message> messages);

    /**
     * Imposta la data di creazione della chat.
     *
     * @param createdAt data/ora di creazione
     * @return questo builder, per concatenare le chiamate
     */
    ChatBuilder createdAt(LocalDateTime createdAt);

    /**
     * Costruisce la Chat con i valori impostati.
     *
     * @return la chat costruita
     */
    Chat build();
}
