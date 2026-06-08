package com.project.kore.messaging;

/**
 * Payload serializzato in JSON per i messaggi chat su RabbitMQ.
 */
public record ChatMessagePayload(Long chatId, Long senderId, String content) {}
