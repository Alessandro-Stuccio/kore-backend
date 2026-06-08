package com.project.kore.service.impl;

import com.project.kore.enums.ChatStatus;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.model.User;
import com.project.kore.service.ChatService;
import com.project.kore.service.MessageService;
import com.project.kore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatAsyncServiceImplTest {

    @Mock
    private ChatService chatService;

    @Mock
    private MessageService messageService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ChatAsyncServiceImpl chatAsyncService;

    private User user1;
    private User user2;
    private Chat openChat;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@test.com");

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@test.com");

        openChat = new Chat();
        openChat.setId(10L);
        openChat.setUser1(user1);
        openChat.setUser2(user2);
        openChat.setStatus(ChatStatus.OPEN);
    }

    // ---- saveChatMessage ----

    @Test
    @DisplayName("saveChatMessage: persists message when chat is open and sender is user1")
    void saveChatMessage_openChatSenderIsUser1_savesMessage() {
        when(chatService.getChatEntity(10L)).thenReturn(openChat);
        when(userService.getUserById(1L)).thenReturn(user1);
        Message saved = new Message();
        when(messageService.saveMessage(openChat, user1, "Hello")).thenReturn(saved);

        chatAsyncService.saveChatMessage(10L, 1L, "Hello");

        verify(messageService).saveMessage(openChat, user1, "Hello");
    }

    @Test
    @DisplayName("saveChatMessage: persists message when chat is open and sender is user2")
    void saveChatMessage_openChatSenderIsUser2_savesMessage() {
        when(chatService.getChatEntity(10L)).thenReturn(openChat);
        when(userService.getUserById(2L)).thenReturn(user2);

        chatAsyncService.saveChatMessage(10L, 2L, "Hi there");

        verify(messageService).saveMessage(openChat, user2, "Hi there");
    }

    @Test
    @DisplayName("saveChatMessage: does nothing when chat entity is null (not found)")
    void saveChatMessage_chatNotFound_doesNothing() {
        when(chatService.getChatEntity(999L)).thenReturn(null);

        chatAsyncService.saveChatMessage(999L, 1L, "Hello");

        verifyNoInteractions(userService);
        verifyNoInteractions(messageService);
    }

    @Test
    @DisplayName("saveChatMessage: does nothing when chat is CLOSED")
    void saveChatMessage_closedChat_doesNothing() {
        openChat.setStatus(ChatStatus.CLOSED);
        when(chatService.getChatEntity(10L)).thenReturn(openChat);

        chatAsyncService.saveChatMessage(10L, 1L, "Hello");

        verifyNoInteractions(userService);
        verifyNoInteractions(messageService);
    }

    @Test
    @DisplayName("saveChatMessage: does nothing when sender is neither user1 nor user2 of the chat")
    void saveChatMessage_senderNotPartOfChat_doesNothing() {
        User outsider = new User();
        outsider.setId(99L);
        when(chatService.getChatEntity(10L)).thenReturn(openChat);
        when(userService.getUserById(99L)).thenReturn(outsider);

        chatAsyncService.saveChatMessage(10L, 99L, "Hello");

        verify(messageService, never()).saveMessage(any(), any(), any());
    }

    // ---- markAsDeliveredAsync ----

    @Test
    @DisplayName("markAsDeliveredAsync: delegates to messageService.markAsDelivered with correct args")
    void markAsDeliveredAsync_delegatesToMessageService() {
        chatAsyncService.markAsDeliveredAsync(10L, 1L);

        verify(messageService).markAsDelivered(10L, 1L);
    }

    @Test
    @DisplayName("markAsDeliveredAsync: swallows exceptions thrown by messageService without propagating")
    void markAsDeliveredAsync_exceptionThrown_doesNotPropagate() {
        doThrow(new RuntimeException("DB error")).when(messageService).markAsDelivered(10L, 1L);

        // should not throw
        chatAsyncService.markAsDeliveredAsync(10L, 1L);

        verify(messageService).markAsDelivered(10L, 1L);
    }

    // ---- markAsReadAsync ----

    @Test
    @DisplayName("markAsReadAsync: delegates to messageService.markAsRead with correct args")
    void markAsReadAsync_delegatesToMessageService() {
        chatAsyncService.markAsReadAsync(10L, 1L);

        verify(messageService).markAsRead(10L, 1L);
    }

    @Test
    @DisplayName("markAsReadAsync: swallows exceptions thrown by messageService without propagating")
    void markAsReadAsync_exceptionThrown_doesNotPropagate() {
        doThrow(new RuntimeException("DB error")).when(messageService).markAsRead(10L, 1L);

        // should not throw
        chatAsyncService.markAsReadAsync(10L, 1L);

        verify(messageService).markAsRead(10L, 1L);
    }
}
