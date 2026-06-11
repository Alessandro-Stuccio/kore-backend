package com.project.kore.mapper;

import com.project.kore.dto.response.ChatMessageResponse;
import com.project.kore.dto.response.ConversationPreviewResponse;
import com.project.kore.dto.response.WsMessageResponse;
import com.project.kore.dto.response.WsNotificationResponse;
import com.project.kore.dto.response.WsUnreadUpdateResponse;
import com.project.kore.enums.MessageStatus;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte messaggi e chat nei DTO usati dalla messaggistica.
 */
@Component
public class ChatMapper {

    /**
     * Converte un messaggio nel suo DTO; mittente e destinatario si ricavano dal flag
     * {@code sentByUser1} sulla chat collegata.
     *
     * @param message il messaggio da convertire
     * @return il DTO del messaggio
     */
    public ChatMessageResponse toMessageResponse(Message message) {
        User sender = message.isSentByUser1()
                ? message.getChat().getUser1()
                : message.getChat().getUser2();
        User receiver = message.isSentByUser1()
                ? message.getChat().getUser2()
                : message.getChat().getUser1();
        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .receiverId(receiver.getId())
                .receiverName(receiver.getFullName())
                .content(message.getContent())
                .createdAt(message.getTimeStamp())
                .status(message.getStatus())
                .build();
    }

    /**
     * Converte una lista di messaggi nei rispettivi DTO.
     *
     * @param messages i messaggi da convertire
     * @return i DTO dei messaggi
     */
    public List<ChatMessageResponse> toMessageResponseList(List<Message> messages) {
        return messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Costruisce l'anteprima della chat per la lista conversazioni: il partner è l'altro
     * utente rispetto a {@code currentUserId}.
     *
     * @param chat          la chat
     * @param currentUserId id dell'utente che visualizza la lista
     * @param lastMsg       ultimo messaggio della chat (può essere {@code null})
     * @param unreadCount   numero di messaggi non letti per l'utente corrente
     * @return l'anteprima della conversazione
     */
    public ConversationPreviewResponse toConversationPreview(Chat chat, Long currentUserId,
                                                              Message lastMsg, int unreadCount) {
        User partner = chat.getUser1().getId().equals(currentUserId)
                ? chat.getUser2() : chat.getUser1();
        return ConversationPreviewResponse.builder()
                .chatId(chat.getId())
                .otherUserId(partner.getId())
                .otherUserName(partner.getFullName())
                .otherUserRole(partner.getRole() != null ? partner.getRole().name() : null)
                .lastMessage(lastMsg != null ? lastMsg.getContent() : "")
                .lastMessageTime(lastMsg != null ? lastMsg.getTimeStamp() : null)
                .unreadCount(unreadCount)
                .terminated(chat.getStatus() == com.project.kore.enums.ChatStatus.CLOSED)
                .build();
    }

    /**
     * Costruisce il DTO del messaggio per l'invio in tempo reale via STOMP. {@code id} e {@code createdAt}
     * sono sintetici perché al momento del broadcast la persistenza è ancora asincrona (via coda).
     *
     * @param chatId   id della chat (usato anche come {@code roomId})
     * @param sender   mittente del messaggio
     * @param receiver destinatario, oppure {@code null} se non determinabile (chat assente)
     * @param content  testo del messaggio
     * @param status   stato del messaggio (es. {@code "SENT"})
     * @return il DTO pronto per l'invio WebSocket
     */
    public WsMessageResponse toWsMessage(Long chatId, User sender, User receiver, String content, String status) {
        return WsMessageResponse.builder()
                .id(System.currentTimeMillis())
                .chatId(chatId)
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .receiverId(receiver != null ? receiver.getId() : null)
                .receiverName(receiver != null ? receiver.getFullName() : null)
                .content(content)
                .status(status)
                .createdAt(LocalDateTime.now().toString())
                .roomId(String.valueOf(chatId))
                .build();
    }

    /**
     * Avvolge un messaggio in una notifica privata di tipo {@code NEW_MESSAGE}.
     *
     * @param msg il messaggio da notificare
     * @return la notifica
     */
    public WsNotificationResponse toNewMessageNotification(WsMessageResponse msg) {
        return WsNotificationResponse.builder()
                .type("NEW_MESSAGE")
                .message(msg)
                .build();
    }

    /**
     * Costruisce una notifica di cambio stato (es. {@code DELIVERED_UPDATE} / {@code READ_UPDATE}) che porta
     * solo l'id della chat e il nuovo stato.
     *
     * @param chatId id della chat interessata
     * @param type   tipo di notifica
     * @param status nuovo stato dei messaggi
     * @return la notifica
     */
    public WsNotificationResponse toStatusNotification(Long chatId, String type, MessageStatus status) {
        return WsNotificationResponse.builder()
                .type(type)
                .message(WsMessageResponse.builder()
                        .chatId(chatId)
                        .status(status.name())
                        .build())
                .build();
    }

    /**
     * Costruisce l'aggiornamento del badge dei messaggi non letti (type {@code UNREAD_UPDATE}).
     *
     * @param userId id dell'utente a cui aggiornare il badge
     * @param count  totale dei messaggi non letti
     * @return l'aggiornamento
     */
    public WsUnreadUpdateResponse toUnreadUpdate(Long userId, int count) {
        return WsUnreadUpdateResponse.builder()
                .type("UNREAD_UPDATE")
                .userId(userId)
                .unreadCount(count)
                .build();
    }
}
