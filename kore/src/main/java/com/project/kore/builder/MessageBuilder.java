package com.project.kore.builder;

import com.project.kore.enums.MessageStatus;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;

import java.time.LocalDateTime;

/**
 * Costruisce un Message di chat con interfaccia fluente.
 */
public interface MessageBuilder {

    /**
     * Imposta l'id del messaggio.
     *
     * @param id identificativo del messaggio
     * @return questo builder, per concatenare le chiamate
     */
    MessageBuilder id(Long id);

    /**
     * Imposta il contenuto testuale del messaggio.
     *
     * @param content testo del messaggio
     * @return questo builder, per concatenare le chiamate
     */
    MessageBuilder content(String content);

    /**
     * Imposta il timestamp del messaggio.
     *
     * @param timeStamp data/ora di invio
     * @return questo builder, per concatenare le chiamate
     */
    MessageBuilder timeStamp(LocalDateTime timeStamp);

    /**
     * Imposta lo stato del messaggio (inviato, consegnato, letto).
     *
     * @param status stato del messaggio
     * @return questo builder, per concatenare le chiamate
     */
    MessageBuilder status(MessageStatus status);

    /**
     * Indica se il mittente è il primo utente della chat.
     *
     * @param sentByUser1 {@code true} se inviato da user1, {@code false} se da user2
     * @return questo builder, per concatenare le chiamate
     */
    MessageBuilder sentByUser1(boolean sentByUser1);

    /**
     * Imposta la chat di appartenenza.
     *
     * @param chat la chat che contiene il messaggio
     * @return questo builder, per concatenare le chiamate
     */
    MessageBuilder chat(Chat chat);

    /**
     * Costruisce il Message con i valori impostati.
     *
     * @return il messaggio costruito
     */
    Message build();
}
