package com.project.kore.controller;

import com.project.kore.config.WebSocketEventListener;
import com.project.kore.dto.request.ws.JoinRoomRequest;
import com.project.kore.dto.request.ws.LeaveRoomRequest;
import com.project.kore.dto.request.ws.WsMarkReadRequest;
import com.project.kore.dto.request.ws.WsSendMessageRequest;
import com.project.kore.dto.response.WsMessageResponse;
import com.project.kore.dto.response.WsNotificationResponse;
import com.project.kore.dto.response.WsUnreadUpdateResponse;
import com.project.kore.enums.ChatStatus;
import com.project.kore.enums.MessageStatus;
import com.project.kore.messaging.ChatMessagePublisher;
import com.project.kore.model.Chat;
import com.project.kore.model.User;
import com.project.kore.service.ChatAsyncService;
import com.project.kore.service.ChatService;
import com.project.kore.service.MessageService;
import com.project.kore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * Chat in tempo reale via STOMP: join/leave stanza, invio messaggi e ricevute DELIVERED/READ.
 * I messaggi vanno su /topic/chat/{roomId}, le notifiche private su /queue/notifications.
 */
@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);
    private final SimpMessageSendingOperations messagingTemplate;
    private final WebSocketEventListener eventListener;
    private final ChatService chatService;
    private final MessageService messageService;
    private final ChatAsyncService chatAsyncService;
    private final ChatMessagePublisher chatMessagePublisher;
    private final UserService userService;

    public ChatWebSocketController(SimpMessageSendingOperations messagingTemplate,
                                   WebSocketEventListener eventListener,
                                   ChatService chatService,
                                   MessageService messageService,
                                   ChatAsyncService chatAsyncService,
                                   ChatMessagePublisher chatMessagePublisher,
                                   UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.eventListener = eventListener;
        this.chatService = chatService;
        this.messageService = messageService;
        this.chatAsyncService = chatAsyncService;
        this.chatMessagePublisher = chatMessagePublisher;
        this.userService = userService;
    }

    /**
     * Registra la sessione corrente nella stanza, così sa quando il ricevitore è "presente".
     *
     * @param request payload con l'id della stanza (chat) a cui unirsi
     * @param ha       accessor degli header STOMP, da cui si legge il session id
     */
    @MessageMapping("/chat.join")
    public void joinRoom(@Payload JoinRoomRequest request, SimpMessageHeaderAccessor ha) {
        String sid = ha.getSessionId();
        if (sid != null && request.roomId() != null) {
            eventListener.joinRoom(sid, request.roomId());
        }
    }

    /**
     * Toglie la sessione corrente dalla stanza.
     *
     * @param request payload con l'id della stanza (chat) da abbandonare
     * @param ha       accessor degli header STOMP, da cui si legge il session id
     */
    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload LeaveRoomRequest request, SimpMessageHeaderAccessor ha) {
        String sid = ha.getSessionId();
        if (sid != null && request.roomId() != null) {
            eventListener.leaveRoom(sid, request.roomId());
        }
    }

    /**
     * Smista un messaggio sul topic della stanza e ne pubblica una copia asincrona (persistenza via coda).
     * Se la chat era CLOSED viene riaperta. Quando il ricevitore non è nella stanza gli mando una notifica
     * NEW_MESSAGE privata; in ogni caso aggiorno il suo contatore di non letti.
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

        Long senderId = sender.getId();
        Long chatId = request.chatId();
        String content = request.content();
        String roomId = String.valueOf(chatId);

        log.info("[WS] /chat.send ricevuto: senderId={}, chatId={}, contentLen={}",
                senderId, chatId, content != null ? content.length() : 0);

        WsMessageResponse msg = WsMessageResponse.builder()
                .id(System.currentTimeMillis())
                .senderId(senderId)
                .senderName(userService.getUserById(senderId).getFullName())
                .chatId(chatId)
                .content(content)
                .status("SENT")
                .createdAt(LocalDateTime.now().toString())
                .roomId(roomId)
                .build();

        Long receiverId = null;
        String receiverEmail = null;
        try {
            Chat chat = chatService.getChatEntity(chatId);
            if (chat == null) {
                log.warn("[WS] /chat.send: chat {} non trovata.", chatId);
            } else {
                if (chat.getStatus() == ChatStatus.CLOSED) {
                    chat.setStatus(ChatStatus.OPEN);
                    chatService.save(chat);
                    log.info("[WS] /chat.send: chat {} riaperta (era CLOSED).", chatId);
                }
                User receiver = chat.getUser1().getId().equals(senderId)
                        ? chat.getUser2()
                        : chat.getUser1();
                receiverId = receiver.getId();
                receiverEmail = receiver.getEmail();
                msg = WsMessageResponse.builder()
                        .id(msg.getId())
                        .senderId(msg.getSenderId())
                        .senderName(msg.getSenderName())
                        .chatId(msg.getChatId())
                        .content(msg.getContent())
                        .status(msg.getStatus())
                        .createdAt(msg.getCreatedAt())
                        .roomId(msg.getRoomId())
                        .receiverId(receiverId)
                        .receiverName(userService.getUserById(receiverId).getFullName())
                        .build();
            }
        } catch (Exception e) {
            log.warn("[WS] /chat.send: errore nel recupero chat {} — {}", chatId, e.getMessage());
        }

        messagingTemplate.convertAndSend("/topic/chat/" + roomId, msg);

        chatMessagePublisher.publish(chatId, senderId, content);

        if (receiverEmail != null) {
            if (!eventListener.isUserInRoom(receiverId, roomId)) {
                try {
                    messagingTemplate.convertAndSendToUser(
                            receiverEmail, "/queue/notifications",
                            WsNotificationResponse.builder().type("NEW_MESSAGE").message(msg).build());
                } catch (Exception e) {
                    log.warn("[WS] notifica NEW_MESSAGE non recapitata a {}: {}", receiverEmail, e.getMessage());
                }
            }
            sendUnreadUpdate(receiverId, receiverEmail);
        }
    }

    /**
     * Segna i messaggi come DELIVERED e avvisa il mittente con un evento DELIVERED_UPDATE.
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
        chatAsyncService.markAsDeliveredAsync(request.chatId(), user.getId());
        try {
            Chat chat = chatService.getChatEntity(request.chatId());
            User sender = chat.getUser1().getId().equals(user.getId())
                    ? chat.getUser2() : chat.getUser1();
            messagingTemplate.convertAndSendToUser(
                    sender.getEmail(), "/queue/notifications",
                    WsNotificationResponse.builder()
                            .type("DELIVERED_UPDATE")
                            .message(WsMessageResponse.builder()
                                    .chatId(request.chatId())
                                    .status(MessageStatus.DELIVERED.name())
                                    .build())
                            .build());
        } catch (Exception e) {
            log.warn("[WS] DELIVERED_UPDATE non recapitata chatId={}: {}", request.chatId(), e.getMessage());
        }
    }

    /**
     * Segna i messaggi come READ, aggiorna i non letti del lettore e avvisa il mittente con READ_UPDATE.
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
        chatAsyncService.markAsReadAsync(request.chatId(), user.getId());
        sendUnreadUpdate(user.getId(), user.getEmail());
        try {
            Chat chat = chatService.getChatEntity(request.chatId());
            User sender = chat.getUser1().getId().equals(user.getId())
                    ? chat.getUser2() : chat.getUser1();
            messagingTemplate.convertAndSendToUser(
                    sender.getEmail(), "/queue/notifications",
                    WsNotificationResponse.builder()
                            .type("READ_UPDATE")
                            .message(WsMessageResponse.builder()
                                    .chatId(request.chatId())
                                    .status(MessageStatus.READ.name())
                                    .build())
                            .build());
        } catch (Exception e) {
            log.warn("[WS] READ_UPDATE non recapitata chatId={}: {}", request.chatId(), e.getMessage());
        }
    }

    /** Spinge al destinatario il conteggio aggiornato dei non letti (l'email è la chiave di routing STOMP). */
    private void sendUnreadUpdate(Long userId, String userEmail) {
        try {
            int count = messageService.getTotalUnreadCount(userId);
            messagingTemplate.convertAndSendToUser(
                    userEmail, "/queue/notifications",
                    WsUnreadUpdateResponse.builder().type("UNREAD_UPDATE").userId(userId).unreadCount(count).build());
        } catch (Exception e) {
            log.warn("[WS] UNREAD_UPDATE non recapitata a {}: {}", userEmail, e.getMessage());
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
