package com.project.kore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Recensione lasciata da un cliente a un professionista. Il vincolo unico sulla
 * coppia (client, professional) garantisce una sola recensione per coppia; inoltre
 * può recensire solo chi ha almeno una prenotazione completata.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uq_review_client_professional", columnNames = {"client_id", "professional_id"})
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "client è obbligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_client_id"))
    private User client;

    @NotNull(message = "professional è obbligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_professional_id"))
    private User professional;

    // Punteggio da 1 a 5
    @Min(value = 1, message = "rating deve essere compreso tra 1 e 5")
    @Max(value = 5, message = "rating deve essere compreso tra 1 e 5")
    @Column(nullable = false)
    private int rating;

    // Commento libero e facoltativo
    @Column(length = 1000)
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Review() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }

    public User getProfessional() { return professional; }
    public void setProfessional(User professional) { this.professional = professional; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review that = (Review) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Review{id=" + id + ", rating=" + rating + ", createdAt=" + createdAt + "}";
    }
}
