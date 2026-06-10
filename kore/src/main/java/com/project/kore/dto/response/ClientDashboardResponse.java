package com.project.kore.dto.response;

import java.util.List;
import java.util.Objects;

/**
 * Aggrega tutto ciò che serve alla dashboard del cliente in un'unica risposta:
 * profilo, abbonamento, professionisti seguiti e prossime prenotazioni.
 */
public class ClientDashboardResponse {

    private UserResponse profile;
    private List<ProfessionalSummaryResponse> followingProfessionals;
    private SubscriptionResponse subscription;
    private List<BookingResponse> upcomingBookings;


    public UserResponse getProfile() { return profile; }
    public void setProfile(UserResponse profile) { this.profile = profile; }

    public List<ProfessionalSummaryResponse> getFollowingProfessionals() { return followingProfessionals; }
    public void setFollowingProfessionals(List<ProfessionalSummaryResponse> followingProfessionals) { this.followingProfessionals = followingProfessionals; }

    public SubscriptionResponse getSubscription() { return subscription; }
    public void setSubscription(SubscriptionResponse subscription) { this.subscription = subscription; }

    public List<BookingResponse> getUpcomingBookings() { return upcomingBookings; }
    public void setUpcomingBookings(List<BookingResponse> upcomingBookings) { this.upcomingBookings = upcomingBookings; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientDashboardResponse that = (ClientDashboardResponse) o;
        return Objects.equals(profile, that.profile) && Objects.equals(subscription, that.subscription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile, subscription);
    }

    @Override
    public String toString() {
        return "ClientDashboardResponse{profile=" + profile + "}";
    }

    public static class Builder {
        private UserResponse profile;
        private List<ProfessionalSummaryResponse> followingProfessionals;
        private SubscriptionResponse subscription;
        private List<BookingResponse> upcomingBookings;

        public Builder profile(UserResponse profile) { this.profile = profile; return this; }
        public Builder followingProfessionals(List<ProfessionalSummaryResponse> followingProfessionals) { this.followingProfessionals = followingProfessionals; return this; }
        public Builder subscription(SubscriptionResponse subscription) { this.subscription = subscription; return this; }
        public Builder upcomingBookings(List<BookingResponse> upcomingBookings) { this.upcomingBookings = upcomingBookings; return this; }

        public ClientDashboardResponse build() {
            ClientDashboardResponse obj = new ClientDashboardResponse();
            obj.profile = this.profile;
            obj.followingProfessionals = this.followingProfessionals;
            obj.subscription = this.subscription;
            obj.upcomingBookings = this.upcomingBookings;
            return obj;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
