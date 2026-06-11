package com.project.kore.dto.response;

/**
 * Envelope interno usato dal {@code ChatFacade} per dire al controller WebSocket cosa inviare e su quale
 * canale STOMP, senza che il controller conosca la logica di routing.
 * <ul>
 *   <li>{@code user == null} → broadcast: {@code convertAndSend(destination, payload)}</li>
 *   <li>{@code user != null} → privato: {@code convertAndSendToUser(user, destination, payload)}</li>
 * </ul>
 * Non viene serializzato verso il client: lo è solo {@code payload}.
 */
public class WsDispatch {

    private String user;
    private String destination;
    private Object payload;

    private WsDispatch(Builder b) {
        this.user = b.user;
        this.destination = b.destination;
        this.payload = b.payload;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Email/username destinatario per l'invio privato, oppure {@code null} per il broadcast. */
    public String getUser() {
        return user;
    }

    /** Destinazione STOMP (es. {@code /topic/chat/123} o {@code /queue/notifications}). */
    public String getDestination() {
        return destination;
    }

    /** Il DTO da inviare. */
    public Object getPayload() {
        return payload;
    }

    public static class Builder {
        private String user;
        private String destination;
        private Object payload;

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder destination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public WsDispatch build() {
            return new WsDispatch(this);
        }
    }
}
