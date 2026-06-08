package com.project.kore.service;

import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** Gestione dei messaggi dentro una chat. */
@Validated
public interface MessageService {

    /**
     * Salva un nuovo messaggio nella chat.
     *
     * @param chat    la chat di destinazione
     * @param sender  l'utente mittente
     * @param content testo del messaggio
     * @return il messaggio salvato
     */
    Message saveMessage(@NotNull Chat chat, @NotNull User sender, @NotBlank String content);

    /**
     * Messaggi della chat in pagine (page 0-based).
     *
     * @param chatId id della chat
     * @param page   indice di pagina, a partire da 0
     * @param size   dimensione della pagina
     * @return i messaggi della pagina richiesta
     */
    List<Message> getMessages(@NotNull @Min(1) Long chatId, @Min(0) int page, @Min(1) int size);

    /**
     * Segna come consegnati i messaggi della chat ancora in sospeso per il destinatario.
     *
     * @param chatId id della chat
     * @param userId id del destinatario
     */
    void markAsDelivered(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);

    /**
     * Segna come letti i messaggi non letti della chat per l'utente.
     *
     * @param chatId id della chat
     * @param userId id dell'utente che legge
     */
    void markAsRead(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);

    /**
     * Messaggi non letti dell'utente su tutte le sue chat.
     *
     * @param userId id dell'utente
     * @return il totale dei messaggi non letti
     */
    int getTotalUnreadCount(@NotNull @Min(1) Long userId);

    /**
     * L'ultimo messaggio della chat.
     *
     * @param chatId id della chat
     * @return il messaggio più recente, oppure {@code null} se la chat è vuota
     */
    Message getLastMessage(@NotNull @Min(1) Long chatId);

    /**
     * Messaggi non letti dell'utente nella singola chat.
     *
     * @param chatId id della chat
     * @param userId id dell'utente
     * @return il numero di messaggi non letti in quella chat
     */
    int getUnreadCount(@NotNull @Min(1) Long chatId, @NotNull @Min(1) Long userId);
}
