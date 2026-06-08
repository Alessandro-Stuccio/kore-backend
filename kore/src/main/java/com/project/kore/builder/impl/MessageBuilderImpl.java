package com.project.kore.builder.impl;

import com.project.kore.builder.MessageBuilder;
import com.project.kore.enums.MessageStatus;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.util.BusinessConstants;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Builder concreto per Message. In build pretende chat e content non nulli, content non vuoto ed entro
 * MAX_MESSAGE_LENGTH; se lo status non è indicato vale SENT.
 */
public class MessageBuilderImpl implements MessageBuilder {

    private Long id;
    private String content;
    private LocalDateTime timeStamp;
    private MessageStatus status;
    private boolean sentByUser1;
    private Chat chat;

    @Override
    public MessageBuilder id(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public MessageBuilder content(String content) {
        this.content = content;
        return this;
    }

    @Override
    public MessageBuilder timeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    @Override
    public MessageBuilder status(MessageStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public MessageBuilder sentByUser1(boolean sentByUser1) {
        this.sentByUser1 = sentByUser1;
        return this;
    }

    @Override
    public MessageBuilder chat(Chat chat) {
        this.chat = chat;
        return this;
    }

    @Override
    public Message build() {
        Objects.requireNonNull(this.chat, "chat è obbligatorio");
        Objects.requireNonNull(this.content, "content è obbligatorio");
        if (this.content.isBlank())
            throw new IllegalArgumentException("content non può essere vuoto");
        if (this.content.length() > BusinessConstants.MAX_MESSAGE_LENGTH)
            throw new IllegalArgumentException("content non può superare " + BusinessConstants.MAX_MESSAGE_LENGTH + " caratteri");

        Message obj = new Message();
        obj.setId(this.id);
        obj.setContent(this.content);
        obj.setTimeStamp(this.timeStamp);
        obj.setStatus(this.status != null ? this.status : MessageStatus.SENT);
        obj.setSentByUser1(this.sentByUser1);
        obj.setChat(this.chat);
        return obj;
    }
}
