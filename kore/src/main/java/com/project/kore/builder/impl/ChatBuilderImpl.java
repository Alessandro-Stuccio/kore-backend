package com.project.kore.builder.impl;

import com.project.kore.builder.ChatBuilder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.project.kore.model.*;


/**
 * Builder concreto per Chat. In fase di build verifica che i due utenti siano presenti e diversi tra loro.
 */
public class ChatBuilderImpl implements ChatBuilder {
    private Long id;
    private User user1;
    private User user2;
    private List<Message> messages= new ArrayList<>();
    private LocalDateTime createdAt;

    @Override
    public ChatBuilder id(Long id) {
        this.id = id;
        return this;
    }
    @Override
    public ChatBuilder user1(User user1) {
        this.user1 = user1;
        return this;
    }
    @Override
    public ChatBuilder user2(User user2) {
        this.user2 = user2;
        return this;
    }
    @Override
    public ChatBuilder messages(List<Message> messages) {
        this.messages = messages;
        return this;
    }
    @Override
    public ChatBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public Chat build() {
        Objects.requireNonNull(this.user1, "user1 è obbligatorio");
        Objects.requireNonNull(this.user2, "user2 è obbligatorio");

        if (this.user1.getId() != null && this.user2.getId() != null
                && this.user1.getId().equals(this.user2.getId()))
            throw new IllegalStateException("user1 e user2 non possono essere lo stesso utente");

        Chat obj = new Chat();
        obj.setId(this.id);
        obj.setUser1(this.user1);
        obj.setUser2(this.user2);
        obj.setMessages(this.messages);
        obj.setCreatedAt(this.createdAt);
        return obj;
    }
}
