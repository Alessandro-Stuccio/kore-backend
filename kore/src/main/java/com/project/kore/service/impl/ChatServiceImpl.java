package com.project.kore.service.impl;

import com.project.kore.enums.ChatStatus;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Chat;
import com.project.kore.model.User;
import com.project.kore.repository.ChatRepository;
import com.project.kore.service.ChatService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** Creazione e recupero delle chat tra utenti. */
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final Validator validator;

    public ChatServiceImpl(ChatRepository chatRepository, Validator validator) {
        this.chatRepository = chatRepository;
        this.validator = validator;
    }

    // Riusa la chat esistente tra i due utenti; se non c'è ne crea una nuova al volo.
    @Override
    public Long getOrCreateChat(User sender, User receiver) {
        return chatRepository.findChatBetweenUsers(sender.getId(), receiver.getId())
                .orElseGet(() -> createChat(sender, receiver))
                .getId();
    }

    // La chat si crea con repository.save diretto (non passa da save(@Valid Chat)), quindi qui
    // applichiamo a mano sia l'invariante relazionale sia la validazione di forma dell'entity.
    private Chat createChat(User sender, User receiver) {
        if (sender.getId() != null && receiver.getId() != null
                && sender.getId().equals(receiver.getId())) {
            throw new IllegalStateException("user1 e user2 non possono essere lo stesso utente");
        }
        Chat newChat = new Chat();
        newChat.setUser1(sender);
        newChat.setUser2(receiver);
        newChat.setCreatedAt(LocalDateTime.now());
        Set<ConstraintViolation<Chat>> violations = validator.validate(newChat);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return chatRepository.save(newChat);
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
