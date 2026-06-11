package com.project.kore.facade.impl;

import com.project.kore.enums.PaymentFrequency;
import com.project.kore.enums.PlanDuration;
import com.project.kore.model.Plan;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionFacadeImpl unit tests")
class SubscriptionFacadeImplTest {

    @Mock private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionFacadeImpl facade;

    private User user;
    private Plan semestralePlan;
    private Plan annualePlan;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        semestralePlan = new Plan();
        semestralePlan.setId(1L);
        semestralePlan.setName("Basic Semestrale");
        semestralePlan.setDuration(PlanDuration.SEMESTRALE);
        semestralePlan.setMonthlyCreditsPT(1);
        semestralePlan.setMonthlyCreditsNutri(1);
        semestralePlan.setMonthlyInstallmentPrice(20.0);
        semestralePlan.setFullPrice(100.0);

        annualePlan = new Plan();
        annualePlan.setId(2L);
        annualePlan.setName("Premium Annuale");
        annualePlan.setDuration(PlanDuration.ANNUALE);
        annualePlan.setMonthlyCreditsPT(2);
        annualePlan.setMonthlyCreditsNutri(2);
        annualePlan.setMonthlyInstallmentPrice(30.0);
        annualePlan.setFullPrice(300.0);
    }

    // ─── activateSubscription (UNICA_SOLUZIONE, SEMESTRALE) ──────────────────────

    @Test
    @DisplayName("activateSubscription UNICA_SOLUZIONE SEMESTRALE: endDate is 6 months after startDate")
    void activateSubscription_unicaSoluzioneSemestrale_endDateIs6MonthsLater() {
        Subscription saved = new Subscription();
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenReturn(saved);

        facade.activateSubscription(user, semestralePlan, PaymentFrequency.UNICA_SOLUZIONE);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionService).save(captor.capture());

        Subscription sub = captor.getValue();
        assertThat(sub.getEndDate()).isEqualTo(sub.getStartDate().plusMonths(6));
    }

    @Test
    @DisplayName("activateSubscription UNICA_SOLUZIONE: installmentsPaid=1, totalInstallments=1, nextPaymentDate=null")
    void activateSubscription_unicaSoluzione_installmentFields() {
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = facade.activateSubscription(user, semestralePlan, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(result.getInstallmentsPaid()).isEqualTo(1);
        assertThat(result.getTotalInstallments()).isEqualTo(1);
        assertThat(result.getNextPaymentDate()).isNull();
    }

    @Test
    @DisplayName("activateSubscription UNICA_SOLUZIONE: subscription is active with correct credits from plan")
    void activateSubscription_unicaSoluzione_creditsFromPlan() {
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = facade.activateSubscription(user, semestralePlan, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getCurrentCreditsPT()).isEqualTo(1);
        assertThat(result.getCurrentCreditsNutri()).isEqualTo(1);
        assertThat(result.getPlan()).isEqualTo(semestralePlan);
        assertThat(result.getUser()).isEqualTo(user);
    }

    // ─── activateSubscription (RATE_MENSILI, ANNUALE) ────────────────────────────

    @Test
    @DisplayName("activateSubscription RATE_MENSILI ANNUALE: endDate is 1 year after startDate")
    void activateSubscription_rateMensiliAnnuale_endDateIs1YearLater() {
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = facade.activateSubscription(user, annualePlan, PaymentFrequency.RATE_MENSILI);

        assertThat(result.getEndDate()).isEqualTo(result.getStartDate().plusYears(1));
    }

    @Test
    @DisplayName("activateSubscription RATE_MENSILI ANNUALE: totalInstallments=12, nextPaymentDate=1 month from start")
    void activateSubscription_rateMensiliAnnuale_installmentFields() {
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = facade.activateSubscription(user, annualePlan, PaymentFrequency.RATE_MENSILI);

        assertThat(result.getInstallmentsPaid()).isEqualTo(1);
        assertThat(result.getTotalInstallments()).isEqualTo(12);
        assertThat(result.getNextPaymentDate()).isEqualTo(result.getStartDate().plusMonths(1));
    }

    @Test
    @DisplayName("activateSubscription RATE_MENSILI SEMESTRALE: totalInstallments=6")
    void activateSubscription_rateMensiliSemestrale_totalInstallments6() {
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = facade.activateSubscription(user, semestralePlan, PaymentFrequency.RATE_MENSILI);

        assertThat(result.getTotalInstallments()).isEqualTo(6);
    }

    // ─── activateSubscription with existing active subscription ──────────────────

    @Test
    @DisplayName("activateSubscription: deactivates existing active subscription before creating new one")
    void activateSubscription_existingActiveSub_deactivatesIt() {
        Subscription existing = new Subscription();
        existing.setActive(true);

        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.of(existing));
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        facade.activateSubscription(user, semestralePlan, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(existing.isActive()).isFalse();
        // save is called twice: once for existing (deactivation), once for new sub
        verify(subscriptionService, times(2)).save(any(Subscription.class));
    }

    @Test
    @DisplayName("activateSubscription: when no existing subscription, save is called exactly once")
    void activateSubscription_noExistingActive_saveCalledOnce() {
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        facade.activateSubscription(user, semestralePlan, PaymentFrequency.UNICA_SOLUZIONE);

        verify(subscriptionService, times(1)).save(any(Subscription.class));
    }

    // ─── activateSubscription general field checks ───────────────────────────────

    @Test
    @DisplayName("activateSubscription: startDate and lastRenewalDate are today")
    void activateSubscription_startDateAndLastRenewalAreToday() {
        LocalDate today = LocalDate.now();

        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = facade.activateSubscription(user, semestralePlan, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(result.getStartDate()).isEqualTo(today);
        assertThat(result.getLastRenewalDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("activateSubscription: returns the saved subscription from service")
    void activateSubscription_returnsServiceResult() {
        Subscription savedSub = new Subscription();
        savedSub.setId(99L);

        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenReturn(savedSub);

        Subscription result = facade.activateSubscription(user, semestralePlan, PaymentFrequency.UNICA_SOLUZIONE);

        assertThat(result).isEqualTo(savedSub);
    }

    @Test
    @DisplayName("activateSubscription: paymentFrequency is preserved in saved subscription")
    void activateSubscription_paymentFrequencyPreserved() {
        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());
        when(subscriptionService.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = facade.activateSubscription(user, annualePlan, PaymentFrequency.RATE_MENSILI);

        assertThat(result.getPaymentFrequency()).isEqualTo(PaymentFrequency.RATE_MENSILI);
    }

    // ─── validateInvariants (crediti negativi dal piano) ─────────────────────────

    @Test
    @DisplayName("activateSubscription: crediti PT negativi dal piano → IllegalArgumentException")
    void activateSubscription_negativePtCredits_throws() {
        Plan badPlan = new Plan();
        badPlan.setDuration(PlanDuration.SEMESTRALE);
        badPlan.setMonthlyCreditsPT(-1);
        badPlan.setMonthlyCreditsNutri(0);

        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                facade.activateSubscription(user, badPlan, PaymentFrequency.UNICA_SOLUZIONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currentCreditsPT");

        verify(subscriptionService, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("activateSubscription: crediti Nutri negativi dal piano → IllegalArgumentException")
    void activateSubscription_negativeNutriCredits_throws() {
        Plan badPlan = new Plan();
        badPlan.setDuration(PlanDuration.ANNUALE);
        badPlan.setMonthlyCreditsPT(0);
        badPlan.setMonthlyCreditsNutri(-3);

        when(subscriptionService.findActiveByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                facade.activateSubscription(user, badPlan, PaymentFrequency.UNICA_SOLUZIONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currentCreditsNutri");

        verify(subscriptionService, never()).save(any(Subscription.class));
    }
}
