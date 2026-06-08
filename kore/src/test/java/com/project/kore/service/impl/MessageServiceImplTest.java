package com.project.kore.service.impl;

import com.project.kore.enums.MessageStatus;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.model.User;
import com.project.kore.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User user1;
    private User user2;
    private Chat chat;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@test.com");

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@test.com");

        chat = new Chat();
        chat.setId(10L);
        chat.setUser1(user1);
        chat.setUser2(user2);
    }

    // ---- saveMessage ----

    @Test
    @DisplayName("saveMessage: sets sentByUser1=true when sender is user1 of the chat")
    void saveMessage_senderIsUser1_setsSentByUser1True() {
        Message saved = new Message();
        saved.setId(100L);
        when(messageRepository.save(any(Message.class))).thenReturn(saved);

        Message result = messageService.saveMessage(chat, user1, "Hello from user1");

        assertThat(result).isSameAs(saved);
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        Message built = captor.getValue();
        assertThat(built.isSentByUser1()).isTrue();
        assertThat(built.getContent()).isEqualTo("Hello from user1");
        assertThat(built.getChat()).isSameAs(chat);
        assertThat(built.getTimeStamp()).isNotNull();
    }

    @Test
    @DisplayName("saveMessage: sets sentByUser1=false when sender is user2 of the chat")
    void saveMessage_senderIsUser2_setsSentByUser1False() {
        Message saved = new Message();
        saved.setId(101L);
        when(messageRepository.save(any(Message.class))).thenReturn(saved);

        messageService.saveMessage(chat, user2, "Hello from user2");

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().isSentByUser1()).isFalse();
    }

    @Test
    @DisplayName("saveMessage: persists message and returns saved entity")
    void saveMessage_persistsAndReturnsMessage() {
        Message saved = new Message();
        saved.setId(102L);
        when(messageRepository.save(any(Message.class))).thenReturn(saved);

        Message result = messageService.saveMessage(chat, user1, "content");

        assertThat(result).isSameAs(saved);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    // ---- getMessages ----

    @Test
    @DisplayName("getMessages: delegates to repository with chatId and PageRequest built from page and size")
    void getMessages_delegatesToRepositoryWithCorrectPageRequest() {
        Message msg = new Message();
        msg.setId(1L);
        when(messageRepository.findMessagesByChatId(10L, PageRequest.of(0, 20)))
                .thenReturn(List.of(msg));

        List<Message> result = messageService.getMessages(10L, 0, 20);

        assertThat(result).containsExactly(msg);
        verify(messageRepository).findMessagesByChatId(10L, PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("getMessages: returns empty list when chat has no messages on the requested page")
    void getMessages_emptyPage_returnsEmpty() {
        when(messageRepository.findMessagesByChatId(10L, PageRequest.of(5, 20)))
                .thenReturn(List.of());

        assertThat(messageService.getMessages(10L, 5, 20)).isEmpty();
    }

    // ---- markAsDelivered ----

    @Test
    @DisplayName("markAsDelivered: calls repository with chatId, userId, SENT and DELIVERED statuses")
    void markAsDelivered_callsRepositoryWithCorrectArgs() {
        messageService.markAsDelivered(10L, 1L);

        verify(messageRepository).markMessagesAsDelivered(10L, 1L, MessageStatus.SENT, MessageStatus.DELIVERED);
    }

    // ---- markAsRead ----

    @Test
    @DisplayName("markAsRead: calls repository with chatId, userId and READ status")
    void markAsRead_callsRepositoryWithCorrectArgs() {
        messageService.markAsRead(10L, 1L);

        verify(messageRepository).markMessagesAsRead(10L, 1L, MessageStatus.READ);
    }

    // ---- getTotalUnreadCount ----

    @Test
    @DisplayName("getTotalUnreadCount: delegates to repository and returns total unread count for user")
    void getTotalUnreadCount_returnsCountFromRepository() {
        when(messageRepository.countTotalUnreadMessagesByUserId(1L, MessageStatus.READ)).thenReturn(7);

        int result = messageService.getTotalUnreadCount(1L);

        assertThat(result).isEqualTo(7);
        verify(messageRepository).countTotalUnreadMessagesByUserId(1L, MessageStatus.READ);
    }

    @Test
    @DisplayName("getTotalUnreadCount: returns zero when user has no unread messages")
    void getTotalUnreadCount_noUnread_returnsZero() {
        when(messageRepository.countTotalUnreadMessagesByUserId(1L, MessageStatus.READ)).thenReturn(0);

        assertThat(messageService.getTotalUnreadCount(1L)).isZero();
    }

    // ---- getLastMessage ----

    @Test
    @DisplayName("getLastMessage: returns the most recent message in the chat")
    void getLastMessage_returnsLastMessage() {
        Message last = new Message();
        last.setId(50L);
        when(messageRepository.findLastMessageByChatId(10L)).thenReturn(last);

        Message result = messageService.getLastMessage(10L);

        assertThat(result).isSameAs(last);
        verify(messageRepository).findLastMessageByChatId(10L);
    }

    @Test
    @DisplayName("getLastMessage: returns null when chat is empty")
    void getLastMessage_emptyCHat_returnsNull() {
        when(messageRepository.findLastMessageByChatId(10L)).thenReturn(null);

        assertThat(messageService.getLastMessage(10L)).isNull();
    }

    // ---- getUnreadCount ----

    @Test
    @DisplayName("getUnreadCount: delegates to repository and returns unread count for user in chat")
    void getUnreadCount_returnsUnreadCountFromRepository() {
        when(messageRepository.countUnreadMessagesByChatIdAndUserId(10L, 1L, MessageStatus.READ)).thenReturn(3);

        int result = messageService.getUnreadCount(10L, 1L);

        assertThat(result).isEqualTo(3);
        verify(messageRepository).countUnreadMessagesByChatIdAndUserId(10L, 1L, MessageStatus.READ);
    }

    @Test
    @DisplayName("getUnreadCount: returns zero when all messages in chat are read")
    void getUnreadCount_allRead_returnsZero() {
        when(messageRepository.countUnreadMessagesByChatIdAndUserId(10L, 1L, MessageStatus.READ)).thenReturn(0);

        assertThat(messageService.getUnreadCount(10L, 1L)).isZero();
    }
}
