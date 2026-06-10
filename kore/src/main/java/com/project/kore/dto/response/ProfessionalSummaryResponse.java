package com.project.kore.dto.response;

import com.project.kore.enums.Role;
import java.util.Objects;

/**
 * Riepilogo di un professionista, mostrato nelle liste e nella dashboard del cliente.
 * isSoldOut segnala che il professionista ha raggiunto il limite di clienti attivi.
 */
public class ProfessionalSummaryResponse {

    private Long id;
    private String fullName;
    private Double averageRating;
    private Integer currentActiveClients;
    private boolean isSoldOut;
    private Role role;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getCurrentActiveClients() { return currentActiveClients; }
    public void setCurrentActiveClients(Integer currentActiveClients) { this.currentActiveClients = currentActiveClients; }

    public boolean isSoldOut() { return isSoldOut; }
    public void setSoldOut(boolean soldOut) { isSoldOut = soldOut; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfessionalSummaryResponse that = (ProfessionalSummaryResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProfessionalSummaryResponse{id=" + id + ", fullName='" + fullName + "', averageRating=" + averageRating + ", role=" + role + ", isSoldOut=" + isSoldOut + "}";
    }

    public static class Builder {
        private Long id;
        private String fullName;
        private Double averageRating;
        private Integer currentActiveClients;
        private boolean isSoldOut;
        private Role role;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder averageRating(Double averageRating) { this.averageRating = averageRating; return this; }
        public Builder currentActiveClients(Integer currentActiveClients) { this.currentActiveClients = currentActiveClients; return this; }
        public Builder isSoldOut(boolean isSoldOut) { this.isSoldOut = isSoldOut; return this; }
        public Builder role(Role role) { this.role = role; return this; }

        public ProfessionalSummaryResponse build() {
            ProfessionalSummaryResponse obj = new ProfessionalSummaryResponse();
            obj.id = this.id;
            obj.fullName = this.fullName;
            obj.averageRating = this.averageRating;
            obj.currentActiveClients = this.currentActiveClients;
            obj.isSoldOut = this.isSoldOut;
            obj.role = this.role;
            return obj;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
