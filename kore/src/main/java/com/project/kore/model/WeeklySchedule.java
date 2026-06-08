package com.project.kore.model;

import com.project.kore.builder.WeeklyScheduleBuilder;
import com.project.kore.builder.impl.WeeklyScheduleBuilderImpl;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * La fascia oraria ricorrente in cui un professionista è disponibile in un certo giorno.
 * Una sola fascia per giorno per professionista (vincolo unico): da qui lo scheduler genera
 * automaticamente gli slot futuri.
 */
@Entity
@Table(name = "weekly_schedules", uniqueConstraints = {
        @UniqueConstraint(name = "uq_weekly_schedule_prof_day", columnNames = {"professional_id", "dayOfWeek"})
})
public class WeeklySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false, foreignKey = @ForeignKey(name = "fk_weekly_schedule_professional_id"))
    private User professional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    public WeeklySchedule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getProfessional() { return professional; }
    public void setProfessional(User professional) { this.professional = professional; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public static WeeklyScheduleBuilder builder() {
        return new WeeklyScheduleBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeeklySchedule that = (WeeklySchedule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WeeklySchedule{id=" + id + ", dayOfWeek=" + dayOfWeek + ", startTime=" + startTime + ", endTime=" + endTime + "}";
    }
}
