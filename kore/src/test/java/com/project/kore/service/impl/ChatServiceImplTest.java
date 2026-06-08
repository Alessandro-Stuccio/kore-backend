package com.project.kore.service.impl;

import com.project.kore.enums.ChatStatus;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Chat;
import com.project.kore.model.User;
import com.project.kore.repository.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    private User sender;
    private User receiver;
    private Chat existingChat;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setEmail("sender@test.com");

        receiver = new User();
        receiver.setId(2L);
        receiver.setEmail("receiver@test.com");

        existingChat = new Chat();
        existingChat.setId(10L);
        existingChat.setUser1(sender);
        existingChat.setUser2(receiver);
        existingChat.setStatus(ChatStatus.OPEN);
        existingChat.setCreatedAt(LocalDateTime.now().minusDays(1));
    }

    // ---- getOrCreateChat ----

    @Test
    @DisplayName("getOrCreateChat: returns existing chat id when a chat between the two users already exists")
    void getOrCreateChat_existingChat_returnsExistingId() {
        when(chatRepository.findChatBetweenUsers(1L, 2L)).thenReturn(Optional.of(existingChat));

        Long result = chatService.getOrCreateChat(sender, receiver);

        assertThat(result).isEqualTo(10L);
        verify(chatRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrCreateChat: creates and persists a new chat when none exists between the two users")
    void getOrCreateChat_noExistingChat_createsAndPersists() {
        Chat savedChat = new Chat();
        savedChat.setId(99L);
        savedChat.setUser1(sender);
        savedChat.setUser2(receiver);

        when(chatRepository.findChatBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(chatRepository.save(any(Chat.class))).thenReturn(savedChat);

        Long result = chatService.getOrCreateChat(sender, receiver);

        assertThat(result).isEqualTo(99L);
        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        Chat created = captor.getValue();
        assertThat(created.getUser1()).isSameAs(sender);
        assertThat(created.getUser2()).isSameAs(receiver);
        assertThat(created.getCreatedAt()).isNotNull();
    }

    // ---- getUserConversations ----

    @Test
    @DisplayName("getUserConversations: delegates to repository and returns all chats for the user")
    void getUserConversations_returnsAllChats() {
        when(chatRepository.findAllChatsByUserId(1L)).thenReturn(List.of(existingChat));

        List<Chat> result = chatService.getUserConversations(1L);

        assertThat(result).containsExactly(existingChat);
        verify(chatRepository).findAllChatsByUserId(1L);
    }

    @Test
    @DisplayName("getUserConversations: returns empty list when user has no conversations")
    void getUserConversations_noChats_returnsEmpty() {
        when(chatRepository.findAllChatsByUserId(99L)).thenReturn(List.of());

        List<Chat> result = chatService.getUserConversations(99L);

        assertThat(result).isEmpty();
    }

    // ---- getChatEntity ----

    @Test
    @DisplayName("getChatEntity: returns chat when found by id")
    void getChatEntity_found_returnsChat() {
        when(chatRepository.findById(10L)).thenReturn(Optional.of(existingChat));

        Chat result = chatService.getChatEntity(10L);

        assertThat(result).isSameAs(existingChat);
    }

    @Test
    @DisplayName("getChatEntity: returns null when chat id does not exist")
    void getChatEntity_notFound_returnsNull() {
        when(chatRepository.findById(999L)).thenReturn(Optional.empty());

        Chat result = chatService.getChatEntity(999L);

        assertThat(result).isNull();
    }

    // ---- save ----

    @Test
    @DisplayName("save: persists chat and returns the saved entity")
    void save_persistsAndReturnsChat() {
        when(chatRepository.save(existingChat)).thenReturn(existingChat);

        Chat result = chatService.save(existingChat);

        assertThat(result).isSameAs(existingChat);
        verify(chatRepository).save(existingChat);
    }

    // ---- countOpenChatsByModerator ----

    @Test
    @DisplayName("countOpenChatsByModerator: delegates to repository and returns open chat count")
    void countOpenChatsByModerator_returnsCorrectCount() {
        when(chatRepository.countOpenChatsByModerator(5L)).thenReturn(3L);

        long result = chatService.countOpenChatsByModerator(5L);

        assertThat(result).isEqualTo(3L);
        verify(chatRepository).countOpenChatsByModerator(5L);
    }

    @Test
    @DisplayName("countOpenChatsByModerator: returns zero when moderator has no open chats")
    void countOpenChatsByModerator_noOpenChats_returnsZero() {
        when(chatRepository.countOpenChatsByModerator(5L)).thenReturn(0L);

        assertThat(chatService.countOpenChatsByModerator(5L)).isZero();
    }

    // ---- closeChat ----

    @Test
    @DisplayName("closeChat: sets status CLOSED, closedAt, closedBy and saves when chat exists")
    void closeChat_existingChat_setsClosedFieldsAndSaves() {
        User moderator = new User();
        moderator.setId(7L);
        when(chatRepository.findById(10L)).thenReturn(Optional.of(existingChat));
        when(chatRepository.save(existingChat)).thenReturn(existingChat);

        LocalDateTime before = LocalDateTime.now();
        chatService.closeChat(10L, moderator);
        LocalDateTime after = LocalDateTime.now();

        assertThat(existingChat.getStatus()).isEqualTo(ChatStatus.CLOSED);
        assertThat(existingChat.getClosedAt()).isBetween(before, after);
        assertThat(existingChat.getClosedBy()).isSameAs(moderator);
        verify(chatRepository).save(existingChat);
    }

    @Test
    @DisplayName("closeChat: throws ResourceNotFoundException when chat id does not exist")
    void closeChat_notFound_throwsResourceNotFoundException() {
        User moderator = new User();
        moderator.setId(7L);
        when(chatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.closeChat(999L, moderator))
                .isInstanceOf(CustomResourceNotFoundException.class);

        verify(chatRepository, never()).save(any());
    }
}
