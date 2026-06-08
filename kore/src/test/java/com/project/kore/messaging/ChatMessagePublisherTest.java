package com.project.kore.messaging;

import com.project.kore.config.RabbitMQConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessagePublisher unit tests")
class ChatMessagePublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ChatMessagePublisher publisher;

    @Test
    @DisplayName("publish: invokes convertAndSend with correct exchange and routing key")
    void publish_callsConvertAndSendWithCorrectExchangeAndRoutingKey() {
        publisher.publish(1L, 2L, "Hello");

        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                payloadCaptor.capture()
        );

        assertThat(exchangeCaptor.getValue()).isEqualTo(RabbitMQConfig.CHAT_EXCHANGE);
        assertThat(routingKeyCaptor.getValue()).isEqualTo(RabbitMQConfig.CHAT_ROUTING_KEY);
    }

    @Test
    @DisplayName("publish: payload contains correct chatId, senderId, and content")
    void publish_payloadHasCorrectFields() {
        publisher.publish(10L, 20L, "Test message");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                payloadCaptor.capture()
        );

        Object captured = payloadCaptor.getValue();
        assertThat(captured).isInstanceOf(ChatMessagePayload.class);
        ChatMessagePayload payload = (ChatMessagePayload) captured;
        assertThat(payload.chatId()).isEqualTo(10L);
        assertThat(payload.senderId()).isEqualTo(20L);
        assertThat(payload.content()).isEqualTo("Test message");
    }

    @Test
    @DisplayName("publish: uses CHAT_EXCHANGE constant value 'chat.exchange'")
    void publish_exchangeIsCorrectValue() {
        publisher.publish(1L, 1L, "msg");

        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
                exchangeCaptor.capture(),
                org.mockito.ArgumentMatchers.anyString(),
                (Object) org.mockito.ArgumentMatchers.any()
        );

        assertThat(exchangeCaptor.getValue()).isEqualTo("chat.exchange");
    }

    @Test
    @DisplayName("publish: uses CHAT_ROUTING_KEY constant value 'chat.message'")
    void publish_routingKeyIsCorrectValue() {
        publisher.publish(1L, 1L, "msg");

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.anyString(),
                routingKeyCaptor.capture(),
                (Object) org.mockito.ArgumentMatchers.any()
        );

        assertThat(routingKeyCaptor.getValue()).isEqualTo("chat.message");
    }

    @Test
    @DisplayName("publish: null content is forwarded as-is to payload")
    void publish_nullContent_forwardedToPayload() {
        publisher.publish(5L, 3L, null);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                payloadCaptor.capture()
        );

        ChatMessagePayload payload = (ChatMessagePayload) payloadCaptor.getValue();
        assertThat(payload.content()).isNull();
    }
}
