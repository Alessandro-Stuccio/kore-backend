package com.project.kore.service;

import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Chat;
import com.project.kore.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** Gestione delle chat tra utenti, con qualche utility per i moderatori. */
@Validated
public interface ChatService {

    /**
     * Ritorna l'id della chat tra i due utenti, creandola al volo se non esiste.
     *
     * @param sender   primo partecipante
     * @param receiver secondo partecipante
     * @return l'id della chat esistente o appena creata
     */
    Long getOrCreateChat(@NotNull User sender, @NotNull User receiver);

    /**
     * Tutte le conversazioni a cui partecipa l'utente.
     *
     * @param userId id dell'utente
     * @return le chat di cui l'utente è partecipante
     */
    List<Chat> getUserConversations(@NotNull @Min(1) Long userId);

    /**
     * Recupera l'entità chat dal suo id.
     *
     * @param chatId id della chat
     * @return la chat, oppure {@code null} se non esiste
     */
    Chat getChatEntity(@NotNull @Min(1) Long chatId);

    /**
     * Persiste la chat (creazione o aggiornamento).
     *
     * @param chat la chat da salvare
     * @return la chat salvata
     */
    Chat save(@NotNull Chat chat);

    /**
     * Quante chat ancora aperte sono in carico a un moderatore.
     *
     * @param moderatorId id del moderatore
     * @return il numero di chat aperte assegnate
     */
    long countOpenChatsByModerator(@NotNull @Min(1) Long moderatorId);

    /**
     * Chiude la chat: da qui in poi non si possono più inviare messaggi.
     *
     * @param chatId    id della chat da chiudere
     * @param moderator moderatore che chiude la chat
     * @throws CustomResourceNotFoundException se la chat non esiste
     */
    void closeChat(@NotNull @Min(1) Long chatId, @NotNull User moderator);
}
