package com.project.kore.service.impl;

import com.project.kore.enums.MessageStatus;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.model.User;
import com.project.kore.repository.MessageRepository;
import com.project.kore.service.MessageService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** Persistenza e lettura dei messaggi di chat. */
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // Il mittente non si memorizza direttamente: si tiene solo il flag sentByUser1, ricavato qui.
    @Override
    public Message saveMessage(Chat chat, User sender, String content) {
        boolean sentByUser1 = chat.getUser1().getId().equals(sender.getId());
        Message message = Message.builder()
                .chat(chat)
                .sentByUser1(sentByUser1)
                .content(content)
                .timeStamp(LocalDateTime.now())
                .build();
        return messageRepository.save(message);
    }

    @Override
    public List<Message> getMessages(Long chatId, int page, int size) {
        return messageRepository.findMessagesByChatId(chatId, PageRequest.of(page, size));
    }

    // Update bulk: porta da SENT a DELIVERED i messaggi non ancora recapitati all'utente.
    @Override
    public void markAsDelivered(Long chatId, Long userId) {
        messageRepository.markMessagesAsDelivered(chatId, userId, MessageStatus.SENT, MessageStatus.DELIVERED);
    }

    // Update bulk: porta a READ i messaggi della chat non ancora letti dall'utente.
    @Override
    public void markAsRead(Long chatId, Long userId) {
        messageRepository.markMessagesAsRead(chatId, userId, MessageStatus.READ);
    }

    @Override
    public int getTotalUnreadCount(Long userId) {
        return messageRepository.countTotalUnreadMessagesByUserId(userId, MessageStatus.READ);
    }

    @Override
    public Message getLastMessage(Long chatId) {
        return messageRepository.findLastMessageByChatId(chatId);
    }

    @Override
    public int getUnreadCount(Long chatId, Long userId) {
        return messageRepository.countUnreadMessagesByChatIdAndUserId(chatId, userId, MessageStatus.READ);
    }
}
