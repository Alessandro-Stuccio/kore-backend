package com.project.kore.messaging;

import com.project.kore.config.RabbitMQConfig;
import com.project.kore.service.ChatAsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * Consuma i messaggi della chat dalla coda RabbitMQ e li salva tramite
 * {@link ChatAsyncService}. Un errore di integrità DB manda il messaggio in DLQ
 * senza retry.
 */
@Component
public class ChatMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageConsumer.class);
    private final ChatAsyncService chatAsyncService;

    public ChatMessageConsumer(ChatAsyncService chatAsyncService) {
        this.chatAsyncService = chatAsyncService;
    }

    // Logga i messaggi finiti nella Dead Letter Queue.
    @RabbitListener(queues = RabbitMQConfig.CHAT_DLQ)
    public void handleDeadLetter(ChatMessagePayload payload) {
        log.error("[DLQ] Messaggio chat non consegnato: chatId={}, senderId={}, content={}",
                payload.chatId(), payload.senderId(), payload.content());
    }

    // Salva il messaggio dalla coda principale. Un errore di integrità DB è
    // permanente, quindi va in DLQ senza retry; gli altri errori lasciano che
    // RabbitMQ ritenti.
    @RabbitListener(queues = RabbitMQConfig.CHAT_QUEUE)
    public void consume(ChatMessagePayload payload) {
        log.info("[RabbitMQ] Consume chat message chatId={} senderId={}", payload.chatId(), payload.senderId());
        try {
            chatAsyncService.saveChatMessage(payload.chatId(), payload.senderId(), payload.content());
        } catch (DataIntegrityViolationException e) {
            log.error("[RabbitMQ] Errore permanente (DataIntegrity) chatId={} senderId={}: {} — invio a DLQ senza retry",
                    payload.chatId(), payload.senderId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Errore permanente di integrità DB", e);
        } catch (Exception e) {
            log.error("[RabbitMQ] Errore durante save asincrono chatId={} senderId={}: {}",
                    payload.chatId(), payload.senderId(), e.getMessage(), e);
            throw e;
        }
    }
}
