package com.project.kore.model;

import com.project.kore.builder.ChatBuilder;
import com.project.kore.builder.impl.ChatBuilderImpl;
import com.project.kore.enums.ChatStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Una conversazione tra due utenti: il vincolo unico sulla coppia (user1, user2) evita di
 * aprirne due per le stesse persone. Lo stato OPEN/CLOSED lo gestiscono i moderatori, e
 * closedAt/closedBy restano vuoti finché la chat è aperta.
 */
@Entity
@Table(name = "chats", uniqueConstraints = {
        @UniqueConstraint(name = "uq_chat_users", columnNames = {"user1_id", "user2_id"})
})
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // È il riferimento rispetto a cui ogni messaggio imposta il flag sentByUser1
    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_user1_id"))
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_user2_id"))
    private User user2;

    // Cancellando la chat spariscono anche i suoi messaggi
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatStatus status = ChatStatus.OPEN;

    private LocalDateTime closedAt;

    // Il moderatore che l'ha chiusa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by_id", foreignKey = @ForeignKey(name = "fk_chat_closed_by_id"))
    private User closedBy;

    public Chat() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser1() { return user1; }
    public void setUser1(User user1) { this.user1 = user1; }

    public User getUser2() { return user2; }
    public void setUser2(User user2) { this.user2 = user2; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public ChatStatus getStatus() { return status; }
    public void setStatus(ChatStatus status) { this.status = status; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public User getClosedBy() { return closedBy; }
    public void setClosedBy(User closedBy) { this.closedBy = closedBy; }

    public static ChatBuilder builder() {
        return new ChatBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat that = (Chat) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Chat{id=" + id + ", createdAt=" + createdAt + ", status=" + status + "}";
    }
}
