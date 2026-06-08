package com.project.kore.builder.impl;

import com.project.kore.enums.MessageStatus;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.util.BusinessConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageBuilderImpl unit tests")
class MessageBuilderImplTest {

    private Chat chat;

    @BeforeEach
    void setUp() {
        chat = new Chat();
        chat.setId(1L);
    }

    // ─── happy path ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("build: all required fields set → returns a valid Message")
    void build_allRequiredFields_returnsMessage() {
        Message message = new MessageBuilderImpl()
                .chat(chat)
                .content("Hello World")
                .build();

        assertThat(message).isNotNull();
        assertThat(message.getChat()).isEqualTo(chat);
        assertThat(message.getContent()).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("build: status not set → defaults to SENT")
    void build_statusNotSet_defaultsToSent() {
        Message message = new MessageBuilderImpl()
                .chat(chat)
                .content("Hello")
                .build();

        assertThat(message.getStatus()).isEqualTo(MessageStatus.SENT);
    }

    @Test
    @DisplayName("build: sentByUser1 = true → reflected in built Message")
    void build_sentByUser1True_setsFlag() {
        Message message = new MessageBuilderImpl()
                .chat(chat)
                .content("Hello")
                .sentByUser1(true)
                .build();

        assertThat(message.isSentByUser1()).isTrue();
    }

    @Test
    @DisplayName("build: sentByUser1 = false → reflected in built Message")
    void build_sentByUser1False_setsFlag() {
        Message message = new MessageBuilderImpl()
                .chat(chat)
                .content("Hello")
                .sentByUser1(false)
                .build();

        assertThat(message.isSentByUser1()).isFalse();
    }

    @Test
    @DisplayName("build: custom status DELIVERED → reflected in built Message")
    void build_customStatusDelivered_setsStatus() {
        Message message = new MessageBuilderImpl()
                .chat(chat)
                .content("Hello")
                .status(MessageStatus.DELIVERED)
                .build();

        assertThat(message.getStatus()).isEqualTo(MessageStatus.DELIVERED);
    }

    @Test
    @DisplayName("build: custom status READ → reflected in built Message")
    void build_customStatusRead_setsStatus() {
        Message message = new MessageBuilderImpl()
                .chat(chat)
                .content("Hello")
                .status(MessageStatus.READ)
                .build();

        assertThat(message.getStatus()).isEqualTo(MessageStatus.READ);
    }

    @Test
    @DisplayName("build: id setter is reflected in built Message")
    void build_withId_setsIdOnMessage() {
        Message message = new MessageBuilderImpl()
                .id(99L)
                .chat(chat)
                .content("Hello")
                .build();

        assertThat(message.getId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("build: timeStamp setter is reflected in built Message")
    void build_withTimeStamp_setsTimeStampOnMessage() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 28, 10, 0);

        Message message = new MessageBuilderImpl()
                .chat(chat)
                .content("Hello")
                .timeStamp(now)
                .build();

        assertThat(message.getTimeStamp()).isEqualTo(now);
    }

    @Test
    @DisplayName("build: content exactly at MAX_MESSAGE_LENGTH → succeeds")
    void build_contentAtMaxLength_succeeds() {
        String maxContent = "A".repeat(BusinessConstants.MAX_MESSAGE_LENGTH);

        Message message = new MessageBuilderImpl()
                .chat(chat)
                .content(maxContent)
                .build();

        assertThat(message.getContent()).hasSize(BusinessConstants.MAX_MESSAGE_LENGTH);
    }

    // ─── validation: null fields ──────────────────────────────────────────────────

    @Test
    @DisplayName("build: null chat → throws NullPointerException")
    void build_nullChat_throwsNullPointerException() {
        assertThatThrownBy(() -> new MessageBuilderImpl()
                .chat(null)
                .content("Hello")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("chat");
    }

    @Test
    @DisplayName("build: chat not set → throws NullPointerException")
    void build_chatNotSet_throwsNullPointerException() {
        assertThatThrownBy(() -> new MessageBuilderImpl()
                .content("Hello")
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("build: null content → throws NullPointerException")
    void build_nullContent_throwsNullPointerException() {
        assertThatThrownBy(() -> new MessageBuilderImpl()
                .chat(chat)
                .content(null)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("content");
    }

    // ─── validation: blank and too-long content ───────────────────────────────────

    @Test
    @DisplayName("build: blank content (spaces only) → throws IllegalArgumentException")
    void build_blankContent_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new MessageBuilderImpl()
                .chat(chat)
                .content("   ")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vuoto");
    }

    @Test
    @DisplayName("build: empty string content → throws IllegalArgumentException")
    void build_emptyContent_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new MessageBuilderImpl()
                .chat(chat)
                .content("")
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("build: content exceeds MAX_MESSAGE_LENGTH (2001 chars) → throws IllegalArgumentException")
    void build_contentTooLong_throwsIllegalArgumentException() {
        String tooLong = "A".repeat(BusinessConstants.MAX_MESSAGE_LENGTH + 1);

        assertThatThrownBy(() -> new MessageBuilderImpl()
                .chat(chat)
                .content(tooLong)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(BusinessConstants.MAX_MESSAGE_LENGTH));
    }
}
