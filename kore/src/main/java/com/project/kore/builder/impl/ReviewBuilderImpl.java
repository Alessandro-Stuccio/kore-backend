package com.project.kore.builder.impl;

import com.project.kore.builder.ReviewBuilder;
import java.time.LocalDateTime;
import java.util.Objects;
import com.project.kore.model.*;


/**
 * Builder concreto per Review. In build controlla che il rating sia tra 1 e 5 e che client e professional
 * non coincidano.
 */
public class ReviewBuilderImpl implements ReviewBuilder {
    private Long id;
    private User client;
    private User professional;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    @Override
    public ReviewBuilder id(Long id) {
        this.id = id;
        return this;
    }
    @Override
    public ReviewBuilder client(User client) {
        this.client = client;
        return this;
    }
    @Override
    public ReviewBuilder professional(User professional) {
        this.professional = professional;
        return this;
    }
    @Override
    public ReviewBuilder rating(int rating) {
        this.rating = rating;
        return this;
    }
    @Override
    public ReviewBuilder comment(String comment) {
        this.comment = comment;
        return this;
    }
    @Override
    public ReviewBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public Review build() {
        Objects.requireNonNull(this.client, "client è obbligatorio");
        Objects.requireNonNull(this.professional, "professional è obbligatorio");

        if (this.rating < 1 || this.rating > 5)
            throw new IllegalArgumentException("rating deve essere compreso tra 1 e 5, valore ricevuto: " + this.rating);
        if (this.client.getId() != null && this.professional.getId() != null
                && this.client.getId().equals(this.professional.getId()))
            throw new IllegalStateException("client e professional non possono essere lo stesso utente");

        Review obj = new Review();
        obj.setId(this.id);
        obj.setClient(this.client);
        obj.setProfessional(this.professional);
        obj.setRating(this.rating);
        obj.setComment(this.comment);
        obj.setCreatedAt(this.createdAt);
        return obj;
    }
}
