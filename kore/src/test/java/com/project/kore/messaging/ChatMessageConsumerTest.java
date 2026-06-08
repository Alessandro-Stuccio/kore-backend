package com.project.kore.messaging;

import com.project.kore.service.ChatAsyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageConsumer unit tests")
class ChatMessageConsumerTest {

    @Mock private ChatAsyncService chatAsyncService;

    @InjectMocks
    private ChatMessageConsumer consumer;

    private ChatMessagePayload payload;

    @BeforeEach
    void setUp() {
        payload = new ChatMessagePayload(1L, 2L, "Hello!");
    }

    // ─── consume: happy path ──────────────────────────────────────────────────────

    @Test
    @DisplayName("consume: happy path → delegates to chatAsyncService.saveChatMessage")
    void consume_happyPath_delegatesToService() throws Exception {
        consumer.consume(payload);

        verify(chatAsyncService).saveChatMessage(1L, 2L, "Hello!");
    }

    @Test
    @DisplayName("consume: happy path → does not throw any exception")
    void consume_happyPath_doesNotThrow() {
        assertThatCode(() -> consumer.consume(payload)).doesNotThrowAnyException();
    }

    // ─── consume: DataIntegrityViolationException ─────────────────────────────────

    @Test
    @DisplayName("consume: DataIntegrityViolationException → throws AmqpRejectAndDontRequeueException")
    void consume_dataIntegrityViolation_throwsAmqpReject() throws Exception {
        doThrow(new DataIntegrityViolationException("constraint violation"))
                .when(chatAsyncService).saveChatMessage(anyLong(), anyLong(), anyString());

        assertThatThrownBy(() -> consumer.consume(payload))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    @Test
    @DisplayName("consume: DataIntegrityViolationException → AmqpRejectAndDontRequeueException wraps original cause")
    void consume_dataIntegrityViolation_causeIsOriginalException() throws Exception {
        DataIntegrityViolationException original = new DataIntegrityViolationException("fk violation");
        doThrow(original).when(chatAsyncService).saveChatMessage(anyLong(), anyLong(), anyString());

        assertThatThrownBy(() -> consumer.consume(payload))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }

    // ─── consume: other Exception ─────────────────────────────────────────────────

    @Test
    @DisplayName("consume: RuntimeException → rethrows the same exception")
    void consume_runtimeException_rethrown() throws Exception {
        RuntimeException cause = new RuntimeException("unexpected error");
        doThrow(cause).when(chatAsyncService).saveChatMessage(anyLong(), anyLong(), anyString());

        assertThatThrownBy(() -> consumer.consume(payload))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("unexpected error");
    }

    @Test
    @DisplayName("consume: generic Exception → rethrows (not wrapped)")
    void consume_genericException_rethrownAsIs() throws Exception {
        IllegalStateException cause = new IllegalStateException("state error");
        doThrow(cause).when(chatAsyncService).saveChatMessage(anyLong(), anyLong(), anyString());

        assertThatThrownBy(() -> consumer.consume(payload))
                .isInstanceOf(IllegalStateException.class)
                .isNotInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    @Test
    @DisplayName("consume: DataIntegrityViolationException → chatAsyncService called once before throw")
    void consume_dataIntegrityViolation_serviceCalledOnce() throws Exception {
        doThrow(new DataIntegrityViolationException("dup"))
                .when(chatAsyncService).saveChatMessage(anyLong(), anyLong(), anyString());

        try {
            consumer.consume(payload);
        } catch (AmqpRejectAndDontRequeueException ignored) {}

        verify(chatAsyncService, times(1)).saveChatMessage(1L, 2L, "Hello!");
    }

    // ─── handleDeadLetter ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleDeadLetter: does not throw any exception")
    void handleDeadLetter_doesNotThrow() {
        assertThatCode(() -> consumer.handleDeadLetter(payload)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("handleDeadLetter: does not call chatAsyncService")
    void handleDeadLetter_doesNotCallService() {
        consumer.handleDeadLetter(payload);

        verifyNoInteractions(chatAsyncService);
    }

    @Test
    @DisplayName("handleDeadLetter: accepts null content payload without throwing")
    void handleDeadLetter_nullContent_noException() {
        ChatMessagePayload nullContentPayload = new ChatMessagePayload(1L, 2L, null);

        assertThatCode(() -> consumer.handleDeadLetter(nullContentPayload)).doesNotThrowAnyException();
    }
}
