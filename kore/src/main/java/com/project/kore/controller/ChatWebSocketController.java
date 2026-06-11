package com.project.kore.controller;

import com.project.kore.dto.request.ws.JoinRoomRequest;
import com.project.kore.dto.request.ws.LeaveRoomRequest;
import com.project.kore.dto.request.ws.WsMarkReadRequest;
import com.project.kore.dto.request.ws.WsSendMessageRequest;
import com.project.kore.dto.response.WsDispatch;
import com.project.kore.facade.ChatFacade;
import com.project.kore.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

/**
 * Chat in tempo reale via STOMP: join/leave stanza, invio messaggi e ricevute DELIVERED/READ.
 * Il controller è "thin": si limita a leggere il payload, estrarre il Principal e inoltrare sui canali
 * STOMP gli invii decisi dal {@link ChatFacade}. Tutta la logica vive nel facade.
 */
@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ChatFacade chatFacade;
    private final SimpMessageSendingOperations messagingTemplate;

    public ChatWebSocketController(ChatFacade chatFacade, SimpMessageSendingOperations messagingTemplate) {
        this.chatFacade = chatFacade;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Registra la sessione corrente nella stanza, così sa quando il ricevitore è "presente".
     *
     * @param request payload con l'id della stanza (chat) a cui unirsi
     * @param ha      accessor degli header STOMP, da cui si legge il session id
     */
    @MessageMapping("/chat.join")
    public void joinRoom(@Payload JoinRoomRequest request, SimpMessageHeaderAccessor ha) {
        String sid = ha.getSessionId();
        if (sid != null && request.roomId() != null) {
            chatFacade.joinRoom(sid, request.roomId());
        }
    }

    /**
     * Toglie la sessione corrente dalla stanza.
     *
     * @param request payload con l'id della stanza (chat) da abbandonare
     * @param ha      accessor degli header STOMP, da cui si legge il session id
     */
    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload LeaveRoomRequest request, SimpMessageHeaderAccessor ha) {
        String sid = ha.getSessionId();
        if (sid != null && request.roomId() != null) {
            chatFacade.leaveRoom(sid, request.roomId());
        }
    }

    /**
     * Inoltra al facade un messaggio in arrivo e spinge sui canali STOMP gli invii che ne risultano.
     *
     * @param request   payload con id chat e contenuto del messaggio
     * @param principal il Principal STOMP del mittente autenticato
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload WsSendMessageRequest request, Principal principal) {
        User sender = extractUser(principal);
        if (sender == null) {
            log.warn("[WS] /chat.send rifiutato: Principal mancante o non valido.");
            return;
        }
        dispatch(chatFacade.processIncomingMessage(request.chatId(), sender.getId(), request.content()));
    }

    /**
     * Segna i messaggi come DELIVERED e inoltra l'eventuale notifica al mittente.
     *
     * @param request   payload con l'id della chat interessata
     * @param principal il Principal STOMP del destinatario autenticato
     */
    @MessageMapping("/chat.delivered")
    public void markAsDelivered(@Payload WsMarkReadRequest request, Principal principal) {
        User user = extractUser(principal);
        if (user == null) {
            log.warn("[WS] /chat.delivered rifiutato: Principal mancante o non valido.");
            return;
        }
        dispatch(chatFacade.markDelivered(request.chatId(), user.getId()));
    }

    /**
     * Segna i messaggi come READ e inoltra gli aggiornamenti (non letti del lettore + notifica al mittente).
     *
     * @param request   payload con l'id della chat interessata
     * @param principal il Principal STOMP del lettore autenticato
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload WsMarkReadRequest request, Principal principal) {
        User user = extractUser(principal);
        if (user == null) {
            log.warn("[WS] /chat.read rifiutato: Principal mancante o non valido.");
            return;
        }
        dispatch(chatFacade.markRead(request.chatId(), user.getId()));
    }

    /** Inoltra ogni invio sul canale corretto: broadcast su topic o notifica privata a un utente. */
    private void dispatch(List<WsDispatch> out) {
        for (WsDispatch d : out) {
            try {
                if (d.getUser() == null) {
                    messagingTemplate.convertAndSend(d.getDestination(), d.getPayload());
                } else {
                    messagingTemplate.convertAndSendToUser(d.getUser(), d.getDestination(), d.getPayload());
                }
            } catch (Exception e) {
                log.warn("[WS] invio non riuscito su {} -> {}: {}", d.getDestination(), d.getUser(), e.getMessage());
            }
        }
    }

    /** Tira fuori lo User dal Principal STOMP, o null se non c'è o non è del tipo atteso. */
    private User extractUser(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
