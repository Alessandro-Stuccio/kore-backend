package com.project.kore.mapper;

import com.project.kore.dto.response.ChatMessageResponse;
import com.project.kore.dto.response.ConversationPreviewResponse;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.model.User;
import org.springframework.stereotype.Component;

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
}
