package com.project.kore.service.impl;

import com.project.kore.enums.ChatStatus;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Chat;
import com.project.kore.model.User;
import com.project.kore.repository.ChatRepository;
import com.project.kore.service.ChatService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** Creazione e recupero delle chat tra utenti. */
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;

    public ChatServiceImpl(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    // Riusa la chat esistente tra i due utenti; se non c'è ne crea una nuova al volo.
    @Override
    public Long getOrCreateChat(User sender, User receiver) {
        return chatRepository.findChatBetweenUsers(sender.getId(), receiver.getId())
                .orElseGet(() -> {
                    Chat newChat = Chat.builder()
                            .user1(sender)
                            .user2(receiver)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return chatRepository.save(newChat);
                })
                .getId();
    }

    @Override
    public List<Chat> getUserConversations(Long userId) {
        return chatRepository.findAllChatsByUserId(userId);
    }

    @Override
    public Chat getChatEntity(Long chatId) {
        return chatRepository.findById(chatId).orElse(null);
    }

    @Override
    public Chat save(Chat chat) {
        return chatRepository.save(chat);
    }

    @Override
    public long countOpenChatsByModerator(Long moderatorId) {
        return chatRepository.countOpenChatsByModerator(moderatorId);
    }

    @Override
    public void closeChat(Long chatId, User moderator) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new CustomResourceNotFoundException("Chat", chatId));
        chat.setStatus(ChatStatus.CLOSED);
        chat.setClosedAt(LocalDateTime.now());
        chat.setClosedBy(moderator);
        chatRepository.save(chat);
    }
}
