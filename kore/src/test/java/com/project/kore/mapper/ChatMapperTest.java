package com.project.kore.mapper;

import com.project.kore.dto.response.ChatMessageResponse;
import com.project.kore.dto.response.ConversationPreviewResponse;
import com.project.kore.enums.ChatStatus;
import com.project.kore.enums.MessageStatus;
import com.project.kore.enums.Role;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMapperTest {

    private ChatMapper chatMapper;

    @BeforeEach
    void setUp() {
        chatMapper = new ChatMapper();
    }

    // ---- helpers ----

    private User buildUser(Long id, String firstName, String lastName, Role role) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setRole(role);
        return u;
    }

    private Chat buildChat(Long id, User user1, User user2, ChatStatus status) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setUser1(user1);
        chat.setUser2(user2);
        chat.setStatus(status);
        return chat;
    }

    private Message buildMessage(Long id, Chat chat, String content, boolean sentByUser1, LocalDateTime timestamp) {
        Message msg = new Message();
        msg.setId(id);
        msg.setChat(chat);
        msg.setContent(content);
        msg.setSentByUser1(sentByUser1);
        msg.setTimeStamp(timestamp);
        msg.setStatus(MessageStatus.SENT);
        return msg;
    }

    // ---- toMessageResponse: sent by user1 ----

    @Test
    @DisplayName("toMessageResponse: sender is user1 when sentByUser1=true")
    void toMessageResponse_sentByUser1_senderIsUser1() {
        User user1 = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        User user2 = buildUser(2L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        Chat chat = buildChat(10L, user1, user2, ChatStatus.OPEN);
        Message msg = buildMessage(100L, chat, "Ciao!", true, LocalDateTime.of(2025, 1, 1, 10, 0));

        ChatMessageResponse response = chatMapper.toMessageResponse(msg);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getChatId()).isEqualTo(10L);
        assertThat(response.getSenderId()).isEqualTo(1L);
        assertThat(response.getSenderName()).isEqualTo("Luca Bianchi");
        assertThat(response.getReceiverId()).isEqualTo(2L);
        assertThat(response.getReceiverName()).isEqualTo("Marco Rossi");
        assertThat(response.getContent()).isEqualTo("Ciao!");
        assertThat(response.getStatus()).isEqualTo(MessageStatus.SENT);
    }

    @Test
    @DisplayName("toMessageResponse: sender is user2 when sentByUser1=false")
    void toMessageResponse_sentByUser2_senderIsUser2() {
        User user1 = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        User user2 = buildUser(2L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        Chat chat = buildChat(10L, user1, user2, ChatStatus.OPEN);
        Message msg = buildMessage(101L, chat, "Risposta!", false, LocalDateTime.of(2025, 1, 1, 10, 5));

        ChatMessageResponse response = chatMapper.toMessageResponse(msg);

        assertThat(response.getSenderId()).isEqualTo(2L);
        assertThat(response.getSenderName()).isEqualTo("Marco Rossi");
        assertThat(response.getReceiverId()).isEqualTo(1L);
        assertThat(response.getReceiverName()).isEqualTo("Luca Bianchi");
    }

    @Test
    @DisplayName("toMessageResponse: timestamp is preserved from message")
    void toMessageResponse_timestampPreserved() {
        User user1 = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        User user2 = buildUser(2L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        Chat chat = buildChat(10L, user1, user2, ChatStatus.OPEN);
        LocalDateTime ts = LocalDateTime.of(2025, 3, 15, 14, 30);
        Message msg = buildMessage(102L, chat, "Test", true, ts);

        ChatMessageResponse response = chatMapper.toMessageResponse(msg);

        assertThat(response.getCreatedAt()).isEqualTo(ts);
    }

    // ---- toMessageResponseList ----

    @Test
    @DisplayName("toMessageResponseList: maps all messages in order")
    void toMessageResponseList_mapsAllMessages() {
        User user1 = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        User user2 = buildUser(2L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        Chat chat = buildChat(10L, user1, user2, ChatStatus.OPEN);
        Message msg1 = buildMessage(100L, chat, "Msg1", true, LocalDateTime.now());
        Message msg2 = buildMessage(101L, chat, "Msg2", false, LocalDateTime.now());

        List<ChatMessageResponse> result = chatMapper.toMessageResponseList(List.of(msg1, msg2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(100L);
        assertThat(result.get(1).getId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("toMessageResponseList: returns empty list for empty input")
    void toMessageResponseList_emptyInput_returnsEmptyList() {
        assertThat(chatMapper.toMessageResponseList(List.of())).isEmpty();
    }

    // ---- toConversationPreview: partner resolution ----

    @Test
    @DisplayName("toConversationPreview: partner is user2 when currentUserId matches user1")
    void toConversationPreview_currentUserIsUser1_partnerIsUser2() {
        User user1 = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        User user2 = buildUser(2L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        Chat chat = buildChat(10L, user1, user2, ChatStatus.OPEN);
        Message lastMsg = buildMessage(200L, chat, "Ultimo messaggio", true, LocalDateTime.of(2025, 2, 1, 9, 0));

        ConversationPreviewResponse preview = chatMapper.toConversationPreview(chat, 1L, lastMsg, 3);

        assertThat(preview.getChatId()).isEqualTo(10L);
        assertThat(preview.getOtherUserId()).isEqualTo(2L);
        assertThat(preview.getOtherUserName()).isEqualTo("Marco Rossi");
        assertThat(preview.getOtherUserRole()).isEqualTo("PERSONAL_TRAINER");
        assertThat(preview.getLastMessage()).isEqualTo("Ultimo messaggio");
        assertThat(preview.getUnreadCount()).isEqualTo(3);
        assertThat(preview.isTerminated()).isFalse();
    }

    @Test
    @DisplayName("toConversationPreview: partner is user1 when currentUserId matches user2")
    void toConversationPreview_currentUserIsUser2_partnerIsUser1() {
        User user1 = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        User user2 = buildUser(2L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        Chat chat = buildChat(10L, user1, user2, ChatStatus.OPEN);

        ConversationPreviewResponse preview = chatMapper.toConversationPreview(chat, 2L, null, 0);

        assertThat(preview.getOtherUserId()).isEqualTo(1L);
        assertThat(preview.getOtherUserName()).isEqualTo("Luca Bianchi");
    }

    @Test
    @DisplayName("toConversationPreview: lastMessage is empty string when lastMsg is null")
    void toConversationPreview_nullLastMsg_lastMessageIsEmpty() {
        User user1 = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        User user2 = buildUser(2L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        Chat chat = buildChat(10L, user1, user2, ChatStatus.OPEN);

        ConversationPreviewResponse preview = chatMapper.toConversationPreview(chat, 1L, null, 0);

        assertThat(preview.getLastMessage()).isEmpty();
        assertThat(preview.getLastMessageTime()).isNull();
    }

    @Test
    @DisplayName("toConversationPreview: terminated=true when chat status is CLOSED")
    void toConversationPreview_closedChat_terminatedIsTrue() {
        User user1 = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        User user2 = buildUser(2L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        Chat chat = buildChat(10L, user1, user2, ChatStatus.CLOSED);

        ConversationPreviewResponse preview = chatMapper.toConversationPreview(chat, 1L, null, 0);

        assertThat(preview.isTerminated()).isTrue();
    }

    @Test
    @DisplayName("toConversationPreview: partner role is null string when user role is null")
    void toConversationPreview_partnerNullRole_otherUserRoleIsNull() {
        User user1 = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        User user2 = buildUser(2L, "Marco", "Rossi", null);
        Chat chat = buildChat(10L, user1, user2, ChatStatus.OPEN);

        ConversationPreviewResponse preview = chatMapper.toConversationPreview(chat, 1L, null, 0);

        assertThat(preview.getOtherUserRole()).isNull();
    }
}
