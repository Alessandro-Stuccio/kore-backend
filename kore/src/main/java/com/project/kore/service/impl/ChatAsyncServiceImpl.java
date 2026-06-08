package com.project.kore.service.impl;

import com.project.kore.enums.ChatStatus;
import com.project.kore.model.Chat;
import com.project.kore.model.User;
import com.project.kore.service.ChatAsyncService;
import com.project.kore.service.ChatService;
import com.project.kore.service.MessageService;
import com.project.kore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operazioni di chat eseguite fuori dal thread chiamante, sul pool 'emailTaskExecutor'. */
@Service
public class ChatAsyncServiceImpl implements ChatAsyncService {

    private static final Logger log = LoggerFactory.getLogger(ChatAsyncServiceImpl.class);
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;

    public ChatAsyncServiceImpl(ChatService chatService, MessageService messageService, UserService userService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void saveChatMessage(Long chatId, Long senderId, String content) {
        doSave(chatId, senderId, content);
    }

    @Override
    @Async("emailTaskExecutor")
    @Transactional
    public void markAsDeliveredAsync(Long chatId, Long userId) {
        try {
            messageService.markAsDelivered(chatId, userId);
        } catch (Exception e) {
            log.error("[WS] MarkAsDelivered error chatId={} userId={}: {}", chatId, userId, e.getMessage(), e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    @Transactional
    public void markAsReadAsync(Long chatId, Long userId) {
        try {
            messageService.markAsRead(chatId, userId);
        } catch (Exception e) {
            log.error("[WS] MarkAsRead error chatId={} userId={}: {}", chatId, userId, e.getMessage(), e);
        }
    }

    // Salva solo se la chat esiste, è ancora aperta e il mittente ne fa davvero parte.
    private void doSave(Long chatId, Long senderId, String content) {
        Chat chat = chatService.getChatEntity(chatId);
        if (chat == null) {
            log.warn("[Chat] doSave: chat {} non trovata.", chatId);
            return;
        }
        if (chat.getStatus() == ChatStatus.CLOSED) {
            log.warn("[Chat] doSave: chat {} è CLOSED, save annullato.", chatId);
            return;
        }
        User sender = userService.getUserById(senderId);
        if (!chat.getUser1().getId().equals(senderId) && !chat.getUser2().getId().equals(senderId)) {
            log.warn("[Chat] doSave: utente {} non è parte della chat {}.", senderId, chatId);
            return;
        }
        messageService.saveMessage(chat, sender, content);
        log.info("[Chat] Messaggio persistito chatId={} senderId={}", chatId, senderId);
    }
}
