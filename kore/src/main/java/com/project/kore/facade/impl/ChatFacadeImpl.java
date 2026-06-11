package com.project.kore.facade.impl;

import com.project.kore.config.WebSocketEventListener;
import com.project.kore.dto.request.SendMessageRequest;
import com.project.kore.dto.response.ChatMessageResponse;
import com.project.kore.dto.response.ClientBasicInfoResponse;
import com.project.kore.dto.response.ConversationPreviewResponse;
import com.project.kore.dto.response.WsDispatch;
import com.project.kore.dto.response.WsMessageResponse;
import com.project.kore.enums.ChatStatus;
import com.project.kore.enums.MessageStatus;
import com.project.kore.enums.Role;
import com.project.kore.exception.chat.ChatNotAllowedException;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.project.kore.mapper.UserMapper;
import com.project.kore.facade.ChatFacade;
import com.project.kore.mapper.ChatMapper;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.model.User;
import com.project.kore.service.ChatAsyncService;
import com.project.kore.service.ChatService;
import com.project.kore.service.MessageService;
import com.project.kore.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gestisce le conversazioni: invio messaggi, anteprime, controllo dei permessi e scelta del moderatore.
 */
@Component
public class ChatFacadeImpl implements ChatFacade {

    private static final String NOTIFICATIONS_QUEUE = "/queue/notifications";

    private final ChatService chatService;
    private final MessageService messageService;
    private final ChatMapper chatMapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final ChatAsyncService chatAsyncService;
    private final WebSocketEventListener eventListener;

    public ChatFacadeImpl(ChatService chatService, MessageService messageService,
                          ChatMapper chatMapper, UserService userService, UserMapper userMapper,
                          ChatAsyncService chatAsyncService, WebSocketEventListener eventListener) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.chatMapper = chatMapper;
        this.userService = userService;
        this.userMapper = userMapper;
        this.chatAsyncService = chatAsyncService;
        this.eventListener = eventListener;
    }

    @Override
    @Transactional
    public Long createChat(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Non puoi avviare una chat con te stesso");
        }
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(receiverId);
        validateChatPermission(sender, receiver);
        return chatService.getOrCreateChat(sender, receiver);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(SendMessageRequest request, Long senderId) {
        Chat chat = chatService.getChatEntity(request.chatId());
        if (chat == null) {
            throw new CustomResourceNotFoundException("Chat", request.chatId());
        }
        // Scrivere in una chat chiusa la riapre automaticamente.
        if (chat.getStatus() == ChatStatus.CLOSED) {
            chat.setStatus(ChatStatus.OPEN);
            chatService.save(chat);
        }
        User sender = userService.getUserById(senderId);
        if (!chat.getUser1().getId().equals(senderId) && !chat.getUser2().getId().equals(senderId)) {
            throw new ChatNotAllowedException("Non sei parte di questa chat");
        }
        Message message = messageService.saveMessage(chat, sender, request.content());
        return chatMapper.toMessageResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversation(Long chatId, Long userId, int page, int size) {
        Chat chat = chatService.getChatEntity(chatId);
        if (chat == null) {
            throw new CustomResourceNotFoundException("Chat", chatId);
        }
        if (!chat.getUser1().getId().equals(userId) && !chat.getUser2().getId().equals(userId)) {
            throw new ChatNotAllowedException("Non sei parte di questa chat");
        }
        List<Message> messages = messageService.getMessages(chatId, page, size);
        return chatMapper.toMessageResponseList(messages);
    }

    /**
     * Anteprime delle conversazioni dell'utente, con ultimo messaggio e numero di non letti.
     * Per client e professionisti nasconde le chat ancora vuote con admin o moderatori.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ConversationPreviewResponse> getUserConversations(Long userId) {
        List<Chat> chats = chatService.getUserConversations(userId);
        User currentUser = userService.getUserById(userId);

        return chats.stream()
                .map(chat -> {
                    Message lastMsg = messageService.getLastMessage(chat.getId());
                    int unreadCount = messageService.getUnreadCount(chat.getId(), userId);
                    return chatMapper.toConversationPreview(chat, userId, lastMsg, unreadCount);
                })
                .filter(res -> {
                    if (res.getLastMessageTime() == null) {
                        Role role = currentUser.getRole();
                        if (role == Role.CLIENT || role == Role.PERSONAL_TRAINER || role == Role.NUTRITIONIST) {
                            if ("ADMIN".equals(res.getOtherUserRole()) || "MODERATOR".equals(res.getOtherUserRole())) {
                                return false;
                            }
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long chatId, Long userId) {
        messageService.markAsRead(chatId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalUnreadCount(Long userId) {
        return messageService.getTotalUnreadCount(userId);
    }

    @Override
    @Transactional
    public void closeChat(Long chatId, Long moderatorId) {
        User moderator = userService.getUserById(moderatorId);
        if (moderator.getRole() != Role.MODERATOR && moderator.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Solo i moderatori possono chiudere le chat");
        }
        chatService.closeChat(chatId, moderator);
    }

    /**
     * Sceglie il moderatore da assegnare all'utente per il supporto: riusa quello con cui c'è già
     * una conversazione, altrimenti prende il moderatore con meno chat aperte (load balancing).
     * Admin e moderatori non possono contattare il supporto.
     */
    @Override
    @Transactional(readOnly = true)
    public ClientBasicInfoResponse getModerator(User user) {
        if (user.getRole() == Role.MODERATOR || user.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("L'amministrazione non può contattare il supporto.");
        }
        List<User> moderators = userService.findByRole(Role.MODERATOR);
        if (moderators.isEmpty()) {
            throw new CustomResourceNotFoundException("Nessun moderatore trovato nel sistema.");
        }
        Optional<User> existing = findExistingModeratorConversation(user.getId(), moderators);
        User selected = existing.orElseGet(() ->
                moderators.stream()
                        .min(Comparator.comparingLong(m -> chatService.countOpenChatsByModerator(m.getId())))
                        .orElse(moderators.get(0))
        );
        return userMapper.toBasicInfoResponse(selected);
    }

    @Override
    public void joinRoom(String sessionId, String roomId) {
        eventListener.joinRoom(sessionId, roomId);
    }

    @Override
    public void leaveRoom(String sessionId, String roomId) {
        eventListener.leaveRoom(sessionId, roomId);
    }

    @Override
    @Transactional
    public List<WsDispatch> processIncomingMessage(Long chatId, Long senderId, String content) {
        List<WsDispatch> out = new ArrayList<>();
        String roomId = String.valueOf(chatId);

        Chat chat = chatService.getChatEntity(chatId);
        // Scrivere in una chat chiusa la riapre automaticamente.
        if (chat != null && chat.getStatus() == ChatStatus.CLOSED) {
            chat.setStatus(ChatStatus.OPEN);
            chatService.save(chat);
        }
        // La persistenza vera è asincrona: pubblichiamo sempre sulla coda.
        chatService.publishMessage(chatId, senderId, content);

        User sender;
        User receiver = null;
        if (chat != null) {
            sender = chat.getUser1().getId().equals(senderId) ? chat.getUser1() : chat.getUser2();
            receiver = counterpart(chat, senderId);
        } else {
            sender = userService.getUserById(senderId);
        }

        WsMessageResponse broadcast = chatMapper.toWsMessage(chatId, sender, receiver, content, "SENT");
        out.add(WsDispatch.builder()
                .destination("/topic/chat/" + roomId)
                .payload(broadcast)
                .build());

        if (receiver != null) {
            if (!eventListener.isUserInRoom(receiver.getId(), roomId)) {
                out.add(WsDispatch.builder()
                        .user(receiver.getEmail())
                        .destination(NOTIFICATIONS_QUEUE)
                        .payload(chatMapper.toNewMessageNotification(broadcast))
                        .build());
            }
            int unread = messageService.getTotalUnreadCount(receiver.getId());
            out.add(WsDispatch.builder()
                    .user(receiver.getEmail())
                    .destination(NOTIFICATIONS_QUEUE)
                    .payload(chatMapper.toUnreadUpdate(receiver.getId(), unread))
                    .build());
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WsDispatch> markDelivered(Long chatId, Long userId) {
        chatAsyncService.markAsDeliveredAsync(chatId, userId);
        Chat chat = chatService.getChatEntity(chatId);
        if (chat == null) {
            return List.of();
        }
        User sender = counterpart(chat, userId);
        return List.of(WsDispatch.builder()
                .user(sender.getEmail())
                .destination(NOTIFICATIONS_QUEUE)
                .payload(chatMapper.toStatusNotification(chatId, "DELIVERED_UPDATE", MessageStatus.DELIVERED))
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WsDispatch> markRead(Long chatId, Long userId) {
        chatAsyncService.markAsReadAsync(chatId, userId);
        Chat chat = chatService.getChatEntity(chatId);
        if (chat == null) {
            return List.of();
        }
        User reader = chat.getUser1().getId().equals(userId) ? chat.getUser1() : chat.getUser2();
        User sender = counterpart(chat, userId);

        List<WsDispatch> out = new ArrayList<>();
        int unread = messageService.getTotalUnreadCount(reader.getId());
        out.add(WsDispatch.builder()
                .user(reader.getEmail())
                .destination(NOTIFICATIONS_QUEUE)
                .payload(chatMapper.toUnreadUpdate(reader.getId(), unread))
                .build());
        out.add(WsDispatch.builder()
                .user(sender.getEmail())
                .destination(NOTIFICATIONS_QUEUE)
                .payload(chatMapper.toStatusNotification(chatId, "READ_UPDATE", MessageStatus.READ))
                .build());
        return out;
    }

    /** L'altro partecipante della chat rispetto a {@code userId}. */
    private User counterpart(Chat chat, Long userId) {
        return chat.getUser1().getId().equals(userId) ? chat.getUser2() : chat.getUser1();
    }

    private Optional<User> findExistingModeratorConversation(Long userId, List<User> moderators) {
        List<Chat> chats = chatService.getUserConversations(userId);
        if (chats == null || chats.isEmpty()) return Optional.empty();
        return chats.stream()
                .map(c -> c.getUser1().getId().equals(userId) ? c.getUser2() : c.getUser1())
                .filter(p -> moderators.stream().anyMatch(m -> m.getId().equals(p.getId())))
                .findFirst();
    }

    // L'ordine delle guardie conta: prima i casi che aprono sempre (admin), poi le regole
    // ristrette dell'insurance manager, infine il vincolo cliente-professionista assegnato.
    private void validateChatPermission(User uA, User uB) {
        if (uA.getRole() == Role.ADMIN || uB.getRole() == Role.ADMIN) return;

        if (uA.getRole() == Role.INSURANCE_MANAGER || uB.getRole() == Role.INSURANCE_MANAGER) {
            boolean otherIsAdmin = uA.getRole() == Role.ADMIN || uB.getRole() == Role.ADMIN;
            boolean otherIsModerator = uA.getRole() == Role.MODERATOR || uB.getRole() == Role.MODERATOR;
            if (!otherIsAdmin && !otherIsModerator) {
                throw new ChatNotAllowedException("Insurance manager può contattare solo admin e moderatori.");
            }
            return;
        }

        if (uA.getRole() == Role.MODERATOR || uB.getRole() == Role.MODERATOR) return;

        User client = null, prof = null;
        if (uA.getRole() == Role.CLIENT) { client = uA; prof = uB; }
        else if (uB.getRole() == Role.CLIENT) { client = uB; prof = uA; }

        boolean assigned = false;
        if (client != null && prof != null) {
            if (prof.getRole() == Role.PERSONAL_TRAINER && client.getAssignedPT() != null
                    && client.getAssignedPT().getId().equals(prof.getId())) assigned = true;
            if (prof.getRole() == Role.NUTRITIONIST && client.getAssignedNutritionist() != null
                    && client.getAssignedNutritionist().getId().equals(prof.getId())) assigned = true;
        }
        if (!assigned) throw new ChatNotAllowedException("Non sei assegnato a questo utente");
    }
}
