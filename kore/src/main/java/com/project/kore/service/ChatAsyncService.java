package com.project.kore.service;

/**
 * Operazioni sulla chat eseguite in modo asincrono, così da non bloccare
 * il thread che gestisce la richiesta o il messaggio in arrivo.
 */
public interface ChatAsyncService {

    /**
     * Salva un messaggio nella chat fuori dal thread chiamante.
     *
     * @param chatId   id della chat
     * @param senderId id del mittente
     * @param content  testo del messaggio
     */
    void saveChatMessage(Long chatId, Long senderId, String content);

    /**
     * Segna come consegnati i messaggi della chat per il destinatario.
     *
     * @param chatId id della chat
     * @param userId id del destinatario
     */
    void markAsDeliveredAsync(Long chatId, Long userId);

    /**
     * Segna come letti i messaggi della chat per l'utente.
     *
     * @param chatId id della chat
     * @param userId id dell'utente che legge
     */
    void markAsReadAsync(Long chatId, Long userId);
}
