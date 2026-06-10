package com.project.kore.dto.response;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Uno slot di disponibilità di un professionista. Serve sia in ingresso che in uscita.
 */
public class SlotResponse {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isAvailable;
    private Long professionalId;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public Long getProfessionalId() { return professionalId; }
    public void setProfessionalId(Long professionalId) { this.professionalId = professionalId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotResponse that = (SlotResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SlotResponse{id=" + id + ", professionalId=" + professionalId + ", startTime=" + startTime + ", endTime=" + endTime + ", isAvailable=" + isAvailable + "}";
    }

    public static class Builder {

        private Long id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean isAvailable;
        private Long professionalId;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public Builder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
        public Builder isAvailable(boolean isAvailable) { this.isAvailable = isAvailable; return this; }
        public Builder professionalId(Long professionalId) { this.professionalId = professionalId; return this; }

        public SlotResponse build() {
            SlotResponse dto = new SlotResponse();
            dto.id = this.id;
            dto.startTime = this.startTime;
            dto.endTime = this.endTime;
            dto.isAvailable = this.isAvailable;
            dto.professionalId = this.professionalId;
            return dto;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
