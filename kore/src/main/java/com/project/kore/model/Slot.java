package com.project.kore.model;

import com.project.kore.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Una finestra di disponibilità di un professionista, sempre da 30 minuti. Quando viene
 * prenotata si valorizzano bookedBy e status; l'optimistic locking sulla version evita il
 * double-booking quando due clienti provano a prenotarla insieme.
 */
@Entity
@Table(
    name = "slots",
    indexes = {
        @Index(name = "idx_slot_time", columnList = "startTime"),
        @Index(name = "idx_slot_prof", columnList = "professional_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_slot_prof_start", columnNames = {"professional_id", "startTime"})
    }
)
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "professional è obbligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false, foreignKey = @ForeignKey(name = "fk_slot_professional_id"))
    private User professional;

    // start ed end distano sempre 30 minuti
    @NotNull(message = "startTime è obbligatorio")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "endTime è obbligatorio")
    @Column(nullable = false)
    private LocalDateTime endTime;

    // Chi ha prenotato; null se lo slot è ancora libero
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booked_by_id", foreignKey = @ForeignKey(name = "fk_slot_booked_by_id"))
    private User bookedBy;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    // Link Jitsi generato alla prenotazione; null se libero
    @Column
    private String meetingLink;

    // Diventa true una volta inviato il promemoria, così non lo si rimanda
    private boolean reminderSent = false;

    // @Version: optimistic locking, gestito da JPA
    @Version
    private Integer version;

    @Column(name = "booked_at")
    private LocalDateTime bookedAt;

    public Slot() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getProfessional() { return professional; }
    public void setProfessional(User professional) { this.professional = professional; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public User getBookedBy() { return bookedBy; }
    public void setBookedBy(User bookedBy) { this.bookedBy = bookedBy; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slot that = (Slot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Slot{id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", status=" + status + "}";
    }
}
