package com.project.kore.model;

import com.project.kore.enums.MessageStatus;
import com.project.kore.util.BusinessConstants;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Un singolo messaggio di una chat; lo stato avanza da SENT a DELIVERED a READ. Per il mittente
 * ci basta il flag sentByUser1 invece di un'altra FK verso User: i due partecipanti li conosciamo
 * già dalla Chat.
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "content non può essere vuoto")
    @Size(max = BusinessConstants.MAX_MESSAGE_LENGTH, message = "content non può superare {max} caratteri")
    private String content;

    private LocalDateTime timeStamp;

    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;

    /** true se il mittente è user1 della chat, false se è user2. */
    private boolean sentByUser1;

    @NotNull(message = "chat è obbligatorio")
    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_chat_id"))
    private Chat chat;

    public Message() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimeStamp() { return timeStamp; }
    public void setTimeStamp(LocalDateTime timeStamp) { this.timeStamp = timeStamp; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public boolean isSentByUser1() { return sentByUser1; }
    public void setSentByUser1(boolean sentByUser1) { this.sentByUser1 = sentByUser1; }

    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message that = (Message) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Message{id=" + id + ", timeStamp=" + timeStamp + ", sentByUser1=" + sentByUser1 + "}";
    }
}
