package com.project.kore.controller;

import com.project.kore.dto.request.SendMessageRequest;
import com.project.kore.dto.response.ChatMessageResponse;
import com.project.kore.dto.response.ClientBasicInfoResponse;
import com.project.kore.dto.response.ConversationPreviewResponse;
import com.project.kore.facade.ChatFacade;
import com.project.kore.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST della chat: storico messaggi e stato delle conversazioni. Serve al caricamento
 * iniziale, prima che subentri il WebSocket per il tempo reale.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatFacade chatFacade;

    public ChatController(ChatFacade chatFacade) {
        this.chatFacade = chatFacade;
    }

    /**
     * Crea una nuova chat tra l'utente autenticato e il destinatario, o recupera quella esistente.
     *
     * @param user       utente autenticato che avvia la chat
     * @param receiverId id del destinatario
     * @return 200 con l'id della chat creata o esistente
     */
    @PostMapping("/create/{receiverId}")
    public ResponseEntity<Long> createChat(@AuthenticationPrincipal User user,
                                            @PathVariable("receiverId") Long receiverId) {
        return ResponseEntity.ok(chatFacade.createChat(user.getId(), receiverId));
    }

    /**
     * Invia un nuovo messaggio da parte dell'utente autenticato.
     *
     * @param user    utente autenticato mittente
     * @param request dati del messaggio (id chat e contenuto)
     * @return 200 con il messaggio persistito
     */
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@AuthenticationPrincipal User user,
                                                            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(chatFacade.sendMessage(request, user.getId()));
    }

    /**
     * Recupera la cronologia dei messaggi di una chat (paginata).
     *
     * @param user   utente autenticato
     * @param chatId id della chat
     * @param page   indice di pagina (default 0)
     * @param size   dimensione della pagina (default 50)
     * @return 200 con i messaggi della pagina richiesta
     */
    @GetMapping("/conversation/{chatId}")
    public ResponseEntity<List<ChatMessageResponse>> getConversation(
            @AuthenticationPrincipal User user,
            @PathVariable("chatId") Long chatId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        return ResponseEntity.ok(chatFacade.getConversation(chatId, user.getId(), page, size));
    }

    /**
     * Tutte le conversazioni dell'utente, con anteprima dell'ultimo messaggio.
     *
     * @param user utente autenticato
     * @return 200 con le anteprime delle conversazioni
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationPreviewResponse>> getUserConversations(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatFacade.getUserConversations(user.getId()));
    }

    /**
     * Segna come letti tutti i messaggi ricevuti in una chat.
     *
     * @param user   utente autenticato che legge
     * @param chatId id della chat
     * @return 200 senza corpo
     */
    @PutMapping("/read/{chatId}")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal User user,
                                            @PathVariable("chatId") Long chatId) {
        chatFacade.markAsRead(chatId, user.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Restituisce il conteggio totale dei messaggi non letti per l'utente autenticato.
     *
     * @param user utente autenticato
     * @return 200 con il totale dei messaggi non letti
     */
    @GetMapping("/unread")
    public ResponseEntity<Integer> getTotalUnreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatFacade.getTotalUnreadCount(user.getId()));
    }

    /**
     * Moderatore di supporto dell'utente: riusa quello già assegnato o sceglie il meno carico.
     *
     * @param user utente autenticato
     * @return 200 con i dati di base del moderatore di supporto
     */
    @GetMapping("/moderator")
    public ResponseEntity<ClientBasicInfoResponse> getModerator(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatFacade.getModerator(user));
    }

    /**
     * Chiude una chat (solo moderatore o admin). L'utente può riaprirla inviando un nuovo messaggio.
     *
     * @param chatId id della chat da chiudere
     * @param user   utente autenticato (moderatore o admin)
     * @return 204 senza corpo
     */
    @PostMapping("/{chatId}/close")
    public ResponseEntity<Void> closeChat(@PathVariable("chatId") Long chatId,
                                           @AuthenticationPrincipal User user) {
        chatFacade.closeChat(chatId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
