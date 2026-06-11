package com.project.kore.facade;

import com.project.kore.dto.request.SendMessageRequest;
import com.project.kore.dto.response.ChatMessageResponse;
import com.project.kore.dto.response.ClientBasicInfoResponse;
import com.project.kore.dto.response.ConversationPreviewResponse;
import com.project.kore.dto.response.WsDispatch;
import com.project.kore.exception.chat.ChatNotAllowedException;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.User;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Gestione delle chat, sia via WebSocket sia via REST.
 */
@Validated
public interface ChatFacade {

    /**
     * Crea la chat tra i due utenti, oppure restituisce quella già esistente.
     *
     * @param senderId   id di chi avvia la chat
     * @param receiverId id del destinatario
     * @return l'id della chat creata o già esistente
     * @throws IllegalArgumentException se i due id coincidono (chat con se stessi)
     * @throws ChatNotAllowedException  se i ruoli dei due utenti non sono autorizzati a chattare
     */
    Long createChat(Long senderId, Long receiverId);

    /**
     * Invia un messaggio in una chat esistente.
     *
     * @param request  dati del messaggio (id chat e contenuto)
     * @param senderId id del mittente
     * @return il messaggio persistito
     * @throws CustomResourceNotFoundException se la chat non esiste
     * @throws ChatNotAllowedException          se il mittente non è parte della chat
     */
    ChatMessageResponse sendMessage(@Valid SendMessageRequest request, Long senderId);

    /**
     * Restituisce i messaggi della conversazione in modo paginato (pagina base 0).
     *
     * @param chatId id della chat
     * @param userId id dell'utente che richiede la conversazione
     * @param page   indice di pagina (0-based)
     * @param size   dimensione della pagina
     * @return i messaggi della pagina richiesta
     * @throws CustomResourceNotFoundException se la chat non esiste
     * @throws ChatNotAllowedException          se l'utente non è parte della chat
     */
    List<ChatMessageResponse> getConversation(Long chatId, Long userId, int page, int size);

    /**
     * Restituisce l'anteprima di tutte le conversazioni dell'utente.
     *
     * @param userId id dell'utente
     * @return le anteprime delle sue conversazioni
     */
    List<ConversationPreviewResponse> getUserConversations(Long userId);

    /**
     * Segna come letti tutti i messaggi non letti della chat.
     *
     * @param chatId id della chat
     * @param userId id dell'utente che legge
     */
    void markAsRead(Long chatId, Long userId);

    /**
     * Conta tutti i messaggi non letti dell'utente, su tutte le sue chat.
     *
     * @param userId id dell'utente
     * @return il totale dei messaggi non letti
     */
    Integer getTotalUnreadCount(Long userId);

    /**
     * Chiude una chat: operazione riservata al moderatore.
     *
     * @param chatId      id della chat da chiudere
     * @param moderatorId id del moderatore che chiude
     * @throws AccessDeniedException se chi richiede l'operazione non è un moderatore
     */
    void closeChat(Long chatId, Long moderatorId);

    /**
     * Restituisce i dati di base del moderatore assegnato all'utente.
     *
     * @param user l'utente che richiede il contatto del supporto
     * @return i dati di base del moderatore
     * @throws AccessDeniedException            se l'utente non può contattare il supporto (es. admin)
     * @throws CustomResourceNotFoundException se non esiste alcun moderatore nel sistema
     */
    ClientBasicInfoResponse getModerator(User user);

    /**
     * Registra la sessione WebSocket nella stanza (presence), così si sa quando il destinatario è presente.
     *
     * @param sessionId id della sessione STOMP
     * @param roomId    id della stanza (chat) a cui unirsi
     */
    void joinRoom(String sessionId, String roomId);

    /**
     * Toglie la sessione WebSocket dalla stanza (presence).
     *
     * @param sessionId id della sessione STOMP
     * @param roomId    id della stanza (chat) da abbandonare
     */
    void leaveRoom(String sessionId, String roomId);

    /**
     * Gestisce un messaggio in arrivo via WebSocket: riapre la chat se chiusa, pubblica per la persistenza
     * asincrona, costruisce il broadcast e le eventuali notifiche/aggiornamenti non letti per il destinatario.
     *
     * @param chatId   id della chat
     * @param senderId id del mittente
     * @param content  testo del messaggio
     * @return gli invii STOMP da inoltrare (broadcast + eventuali notifiche private)
     */
    List<WsDispatch> processIncomingMessage(Long chatId, Long senderId, String content);

    /**
     * Segna i messaggi come consegnati e prepara la notifica {@code DELIVERED_UPDATE} per il mittente.
     *
     * @param chatId id della chat
     * @param userId id del destinatario che ha ricevuto i messaggi
     * @return gli invii STOMP da inoltrare (vuoto se la chat non esiste)
     */
    List<WsDispatch> markDelivered(Long chatId, Long userId);

    /**
     * Segna i messaggi come letti, prepara l'aggiornamento dei non letti per il lettore e la notifica
     * {@code READ_UPDATE} per il mittente.
     *
     * @param chatId id della chat
     * @param userId id del lettore
     * @return gli invii STOMP da inoltrare (vuoto se la chat non esiste)
     */
    List<WsDispatch> markRead(Long chatId, Long userId);
}
