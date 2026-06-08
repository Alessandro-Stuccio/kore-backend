package com.project.kore.mapper;

import com.project.kore.dto.request.RegisterRequest;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.enums.PaymentFrequency;
import com.project.kore.enums.PlanDuration;
import com.project.kore.model.Plan;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionMapperTest {

    private SubscriptionMapper subscriptionMapper;

    @BeforeEach
    void setUp() {
        subscriptionMapper = new SubscriptionMapper();
    }

    // ---- helpers ----

    private User buildUser(Long id, String firstName, String lastName) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        return u;
    }

    private Plan buildPlan(PlanDuration duration, int creditsPT, int creditsNutri, double monthlyPrice) {
        Plan plan = new Plan();
        plan.setId(1L);
        plan.setName("Test Plan");
        plan.setDuration(duration);
        plan.setMonthlyCreditsPT(creditsPT);
        plan.setMonthlyCreditsNutri(creditsNutri);
        plan.setFullPrice(300.0);
        plan.setMonthlyInstallmentPrice(monthlyPrice);
        return plan;
    }

    // ---- toSubscription ----

    @Test
    @DisplayName("toSubscription: returns null when request is null")
    void toSubscription_nullRequest_returnsNull() {
        User user = buildUser(1L, "Luca", "Bianchi");
        Plan plan = buildPlan(PlanDuration.SEMESTRALE, 1, 1, 55.0);

        assertThat(subscriptionMapper.toSubscription(null, user, plan)).isNull();
    }

    @Test
    @DisplayName("toSubscription: returns null when user is null")
    void toSubscription_nullUser_returnsNull() {
        Plan plan = buildPlan(PlanDuration.SEMESTRALE, 1, 1, 55.0);
        RegisterRequest request = new RegisterRequest(
                "Luca", "Bianchi", "luca@test.com", "password", 1L, 2L, null, 1L, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(subscriptionMapper.toSubscription(request, null, plan)).isNull();
    }

    @Test
    @DisplayName("toSubscription: returns null when plan is null")
    void toSubscription_nullPlan_returnsNull() {
        User user = buildUser(1L, "Luca", "Bianchi");
        RegisterRequest request = new RegisterRequest(
                "Luca", "Bianchi", "luca@test.com", "password", 1L, 2L, null, 1L, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(subscriptionMapper.toSubscription(request, user, null)).isNull();
    }

    @Test
    @DisplayName("toSubscription: creates valid subscription from RegisterRequest with UNICA_SOLUZIONE")
    void toSubscription_unicaSoluzione_createsSubscription() {
        User user = buildUser(1L, "Luca", "Bianchi");
        Plan plan = buildPlan(PlanDuration.SEMESTRALE, 1, 1, 55.0);
        RegisterRequest request = new RegisterRequest(
                "Luca", "Bianchi", "luca@test.com", "password", 1L, 2L, null, 1L, PaymentFrequency.UNICA_SOLUZIONE);

        Subscription sub = subscriptionMapper.toSubscription(request, user, plan);

        assertThat(sub).isNotNull();
        assertThat(sub.getUser()).isSameAs(user);
        assertThat(sub.getPlan()).isSameAs(plan);
        assertThat(sub.isActive()).isTrue();
        assertThat(sub.getPaymentFrequency()).isEqualTo(PaymentFrequency.UNICA_SOLUZIONE);
        assertThat(sub.getTotalInstallments()).isEqualTo(1);
        assertThat(sub.getNextPaymentDate()).isNull();
        assertThat(sub.getCurrentCreditsPT()).isEqualTo(1);
        assertThat(sub.getCurrentCreditsNutri()).isEqualTo(1);
    }

    @Test
    @DisplayName("toSubscription: creates subscription with monthly installments for RATE_MENSILI")
    void toSubscription_rateMensili_setsInstallmentsAndNextPayment() {
        User user = buildUser(1L, "Luca", "Bianchi");
        Plan plan = buildPlan(PlanDuration.SEMESTRALE, 1, 1, 55.0);
        RegisterRequest request = new RegisterRequest(
                "Luca", "Bianchi", "luca@test.com", "password", 1L, 2L, null, 1L, PaymentFrequency.RATE_MENSILI);

        Subscription sub = subscriptionMapper.toSubscription(request, user, plan);

        assertThat(sub.getTotalInstallments()).isEqualTo(6); // SEMESTRALE = 6 months
        assertThat(sub.getNextPaymentDate()).isNotNull();
        assertThat(sub.getNextPaymentDate()).isEqualTo(LocalDate.now().plusMonths(1));
    }

    // ---- toSubscriptionFromAdmin ----

    @Test
    @DisplayName("toSubscriptionFromAdmin: creates subscription for UNICA_SOLUZIONE")
    void toSubscriptionFromAdmin_unicaSoluzione_noNextPayment() {
        User user = buildUser(2L, "Sara", "Verdi");
        Plan plan = buildPlan(PlanDuration.ANNUALE, 2, 2, 50.0);

        Subscription sub = subscriptionMapper.toSubscriptionFromAdmin(user, plan, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(sub).isNotNull();
        assertThat(sub.getUser()).isSameAs(user);
        assertThat(sub.getPlan()).isSameAs(plan);
        assertThat(sub.isActive()).isTrue();
        assertThat(sub.getTotalInstallments()).isEqualTo(1);
        assertThat(sub.getNextPaymentDate()).isNull();
    }

    @Test
    @DisplayName("toSubscriptionFromAdmin: ANNUALE plan sets endDate 12 months from start")
    void toSubscriptionFromAdmin_annualePlan_endDateIs12MonthsAhead() {
        User user = buildUser(2L, "Sara", "Verdi");
        Plan plan = buildPlan(PlanDuration.ANNUALE, 2, 2, 50.0);

        Subscription sub = subscriptionMapper.toSubscriptionFromAdmin(user, plan, PaymentFrequency.UNICA_SOLUZIONE);

        LocalDate expectedEnd = LocalDate.now().plusMonths(12);
        assertThat(sub.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(sub.getEndDate()).isEqualTo(expectedEnd);
    }

    @Test
    @DisplayName("toSubscriptionFromAdmin: SEMESTRALE plan sets endDate 6 months from start")
    void toSubscriptionFromAdmin_semestralePlan_endDateIs6MonthsAhead() {
        User user = buildUser(2L, "Sara", "Verdi");
        Plan plan = buildPlan(PlanDuration.SEMESTRALE, 1, 1, 55.0);

        Subscription sub = subscriptionMapper.toSubscriptionFromAdmin(user, plan, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(sub.getEndDate()).isEqualTo(LocalDate.now().plusMonths(6));
    }

    @Test
    @DisplayName("toSubscriptionFromAdmin: initialCreditsPT and CreditsNutri come from plan")
    void toSubscriptionFromAdmin_creditsFromPlan() {
        User user = buildUser(2L, "Sara", "Verdi");
        Plan plan = buildPlan(PlanDuration.ANNUALE, 3, 2, 50.0);

        Subscription sub = subscriptionMapper.toSubscriptionFromAdmin(user, plan, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(sub.getCurrentCreditsPT()).isEqualTo(3);
        assertThat(sub.getCurrentCreditsNutri()).isEqualTo(2);
    }

    @Test
    @DisplayName("toSubscriptionFromAdmin: installmentsPaid starts at 1")
    void toSubscriptionFromAdmin_installmentsPaidIsOne() {
        User user = buildUser(2L, "Sara", "Verdi");
        Plan plan = buildPlan(PlanDuration.SEMESTRALE, 1, 1, 55.0);

        Subscription sub = subscriptionMapper.toSubscriptionFromAdmin(user, plan, PaymentFrequency.RATE_MENSILI);

        assertThat(sub.getInstallmentsPaid()).isEqualTo(1);
    }

    // ---- toResponse ----

    @Test
    @DisplayName("toResponse: returns null for null subscription")
    void toResponse_nullSubscription_returnsNull() {
        assertThat(subscriptionMapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("toResponse: maps all fields from subscription")
    void toResponse_validSubscription_mapsAllFields() {
        User user = buildUser(1L, "Luca", "Bianchi");
        Plan plan = buildPlan(PlanDuration.SEMESTRALE, 1, 1, 55.0);
        Subscription sub = new Subscription();
        sub.setId(100L);
        sub.setUser(user);
        sub.setPlan(plan);
        sub.setActive(true);
        sub.setStartDate(LocalDate.of(2025, 1, 1));
        sub.setEndDate(LocalDate.of(2025, 7, 1));
        sub.setCurrentCreditsPT(1);
        sub.setCurrentCreditsNutri(1);

        SubscriptionResponse response = subscriptionMapper.toResponse(sub);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo("Luca Bianchi");
        assertThat(response.getPlanName()).isEqualTo("Test Plan");
        assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(response.getEndDate()).isEqualTo(LocalDate.of(2025, 7, 1));
        assertThat(response.isActive()).isTrue();
        assertThat(response.getCurrentCreditsPT()).isEqualTo(1);
        assertThat(response.getCurrentCreditsNutri()).isEqualTo(1);
        assertThat(response.getMonthlyPrice()).isEqualTo(55.0);
    }

    @Test
    @DisplayName("toResponse: userId and userName are null when user is null")
    void toResponse_nullUser_nullUserFields() {
        Plan plan = buildPlan(PlanDuration.SEMESTRALE, 1, 1, 55.0);
        Subscription sub = new Subscription();
        sub.setId(101L);
        sub.setUser(null);
        sub.setPlan(plan);

        SubscriptionResponse response = subscriptionMapper.toResponse(sub);

        assertThat(response.getUserId()).isNull();
        assertThat(response.getUserName()).isNull();
    }

    @Test
    @DisplayName("toResponse: planName and monthlyPrice are null when plan is null")
    void toResponse_nullPlan_nullPlanFields() {
        User user = buildUser(1L, "Luca", "Bianchi");
        Subscription sub = new Subscription();
        sub.setId(102L);
        sub.setUser(user);
        sub.setPlan(null);

        SubscriptionResponse response = subscriptionMapper.toResponse(sub);

        assertThat(response.getPlanName()).isNull();
        assertThat(response.getMonthlyPrice()).isNull();
    }

    // ---- toResponseList ----

    @Test
    @DisplayName("toResponseList: maps all subscriptions in list")
    void toResponseList_mapsAllSubscriptions() {
        User user1 = buildUser(1L, "Luca", "Bianchi");
        User user2 = buildUser(2L, "Sara", "Verdi");
        Plan plan = buildPlan(PlanDuration.SEMESTRALE, 1, 1, 55.0);

        Subscription sub1 = new Subscription();
        sub1.setId(1L);
        sub1.setUser(user1);
        sub1.setPlan(plan);

        Subscription sub2 = new Subscription();
        sub2.setId(2L);
        sub2.setUser(user2);
        sub2.setPlan(plan);

        List<SubscriptionResponse> result = subscriptionMapper.toResponseList(List.of(sub1, sub2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("toResponseList: returns empty list for empty input")
    void toResponseList_emptyInput_returnsEmptyList() {
        assertThat(subscriptionMapper.toResponseList(List.of())).isEmpty();
    }
}
