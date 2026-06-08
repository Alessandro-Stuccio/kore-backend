package com.project.kore.messaging;

import com.project.kore.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Pubblica i messaggi della chat sull'exchange RabbitMQ per il salvataggio asincrono.
 */
@Component
public class ChatMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public ChatMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Long chatId, Long senderId, String content) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CHAT_EXCHANGE,
                RabbitMQConfig.CHAT_ROUTING_KEY,
                new ChatMessagePayload(chatId, senderId, content));
    }
}
