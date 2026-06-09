package com.project.kore.scheduler;

import com.project.kore.enums.PaymentFrequency;
import com.project.kore.model.Plan;
import com.project.kore.model.Subscription;
import com.project.kore.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionScheduler unit tests")
class SubscriptionSchedulerTest {

    @Mock private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionScheduler scheduler;

    private Plan plan;

    @BeforeEach
    void setUp() {
        plan = new Plan();
        plan.setId(1L);
        plan.setMonthlyCreditsPT(2);
        plan.setMonthlyCreditsNutri(2);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private Subscription buildActiveSubscription(PaymentFrequency freq) {
        Subscription sub = new Subscription();
        sub.setId(100L);
        sub.setActive(true);
        sub.setPaymentFrequency(freq);
        sub.setPlan(plan);
        sub.setCurrentCreditsPT(0);
        sub.setCurrentCreditsNutri(0);
        return sub;
    }

    // ─── No active subscriptions ──────────────────────────────────────────────────

    @Test
    @DisplayName("renewCredits: no active subscriptions → nothing saved")
    void renewCredits_noActiveSubs_nothingSaved() {
        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of());

        scheduler.renewCredits();

        verify(subscriptionRepository, never()).save(any());
    }

    // ─── Not the first of the month ───────────────────────────────────────────────

    @Test
    @DisplayName("renewCredits: not first of month → nothing saved")
    void renewCredits_notFirstOfMonth_nothingSaved() {
        Subscription sub = buildActiveSubscription(PaymentFrequency.UNICA_SOLUZIONE);
        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of(sub));

        // Use a date that is definitely not the 1st
        LocalDate notFirst = LocalDate.of(2025, 6, 15);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class, invocation -> {
            if (invocation.getMethod().getName().equals("now")) {
                return notFirst;
            }
            return invocation.callRealMethod();
        })) {
            scheduler.renewCredits();
        }

        verify(subscriptionRepository, never()).save(any());
    }

    // ─── UNICA_SOLUZIONE on first of month ────────────────────────────────────────

    @Test
    @DisplayName("renewCredits: UNICA_SOLUZIONE on first of month → resets credits and saves")
    void renewCredits_unicaSoluzione_firstOfMonth_resetsCredits() {
        Subscription sub = buildActiveSubscription(PaymentFrequency.UNICA_SOLUZIONE);
        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of(sub));

        LocalDate firstOfMonth = LocalDate.of(2025, 6, 1);
        sub.setLastRenewalDate(firstOfMonth.minusMonths(1)); // rinnovo mensile dovuto: ultimo rinnovo un mese fa

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class, invocation -> {
            if (invocation.getMethod().getName().equals("now")) {
                return firstOfMonth;
            }
            return invocation.callRealMethod();
        })) {
            scheduler.renewCredits();
        }

        assertThat(sub.getCurrentCreditsPT()).isEqualTo(plan.getMonthlyCreditsPT());
        assertThat(sub.getCurrentCreditsNutri()).isEqualTo(plan.getMonthlyCreditsNutri());
        assertThat(sub.getLastRenewalDate()).isEqualTo(firstOfMonth);
        verify(subscriptionRepository).save(sub);
    }

    // ─── RATE_MENSILI branches ────────────────────────────────────────────────────

    @Test
    @DisplayName("renewCredits: RATE_MENSILI on first of month, payment due and installments remaining → increments paid, advances nextPaymentDate, resets credits")
    void renewCredits_rateMensili_paymentDue_resetsCreditsAndAdvancesDate() {
        Subscription sub = buildActiveSubscription(PaymentFrequency.RATE_MENSILI);
        LocalDate firstOfMonth = LocalDate.of(2025, 6, 1);
        sub.setNextPaymentDate(firstOfMonth); // due today
        sub.setInstallmentsPaid(2);
        sub.setTotalInstallments(6);

        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of(sub));

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class, invocation -> {
            if (invocation.getMethod().getName().equals("now")) {
                return firstOfMonth;
            }
            return invocation.callRealMethod();
        })) {
            scheduler.renewCredits();
        }

        assertThat(sub.getInstallmentsPaid()).isEqualTo(3);
        assertThat(sub.getNextPaymentDate()).isEqualTo(firstOfMonth.plusMonths(1));
        assertThat(sub.getCurrentCreditsPT()).isEqualTo(plan.getMonthlyCreditsPT());
        assertThat(sub.getCurrentCreditsNutri()).isEqualTo(plan.getMonthlyCreditsNutri());
        assertThat(sub.getLastRenewalDate()).isEqualTo(firstOfMonth);
        verify(subscriptionRepository).save(sub);
    }

    @Test
    @DisplayName("renewCredits: RATE_MENSILI on first of month, nextPaymentDate null → skips (no save)")
    void renewCredits_rateMensili_nullNextPaymentDate_skips() {
        Subscription sub = buildActiveSubscription(PaymentFrequency.RATE_MENSILI);
        sub.setNextPaymentDate(null);
        sub.setInstallmentsPaid(1);
        sub.setTotalInstallments(6);

        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of(sub));

        LocalDate firstOfMonth = LocalDate.of(2025, 6, 1);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class, invocation -> {
            if (invocation.getMethod().getName().equals("now")) {
                return firstOfMonth;
            }
            return invocation.callRealMethod();
        })) {
            scheduler.renewCredits();
        }

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("renewCredits: RATE_MENSILI on first of month, nextPaymentDate in the future → skips (no save)")
    void renewCredits_rateMensili_futureNextPaymentDate_skips() {
        Subscription sub = buildActiveSubscription(PaymentFrequency.RATE_MENSILI);
        LocalDate firstOfMonth = LocalDate.of(2025, 6, 1);
        sub.setNextPaymentDate(firstOfMonth.plusDays(1)); // due tomorrow, not yet
        sub.setInstallmentsPaid(1);
        sub.setTotalInstallments(6);

        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of(sub));

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class, invocation -> {
            if (invocation.getMethod().getName().equals("now")) {
                return firstOfMonth;
            }
            return invocation.callRealMethod();
        })) {
            scheduler.renewCredits();
        }

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("renewCredits: RATE_MENSILI on first of month, all installments already paid → skips (no save)")
    void renewCredits_rateMensili_allInstallmentsPaid_skips() {
        Subscription sub = buildActiveSubscription(PaymentFrequency.RATE_MENSILI);
        LocalDate firstOfMonth = LocalDate.of(2025, 6, 1);
        sub.setNextPaymentDate(firstOfMonth);
        sub.setInstallmentsPaid(6); // paid == total
        sub.setTotalInstallments(6);

        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of(sub));

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class, invocation -> {
            if (invocation.getMethod().getName().equals("now")) {
                return firstOfMonth;
            }
            return invocation.callRealMethod();
        })) {
            scheduler.renewCredits();
        }

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("renewCredits: RATE_MENSILI on first of month, nextPaymentDate past due → still processes (date is before or equal)")
    void renewCredits_rateMensili_overduePayment_processes() {
        Subscription sub = buildActiveSubscription(PaymentFrequency.RATE_MENSILI);
        LocalDate firstOfMonth = LocalDate.of(2025, 6, 1);
        sub.setNextPaymentDate(firstOfMonth.minusMonths(1)); // overdue
        sub.setInstallmentsPaid(2);
        sub.setTotalInstallments(6);

        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of(sub));

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class, invocation -> {
            if (invocation.getMethod().getName().equals("now")) {
                return firstOfMonth;
            }
            return invocation.callRealMethod();
        })) {
            scheduler.renewCredits();
        }

        assertThat(sub.getInstallmentsPaid()).isEqualTo(3);
        verify(subscriptionRepository).save(sub);
    }

    // ─── Exception handling ───────────────────────────────────────────────────────

    @Test
    @DisplayName("renewCredits: exception on one subscription → continues with others, no crash")
    void renewCredits_exceptionOnOne_continuesWithOthers() {
        Subscription sub1 = buildActiveSubscription(PaymentFrequency.UNICA_SOLUZIONE);
        sub1.setId(101L);

        Subscription sub2 = buildActiveSubscription(PaymentFrequency.UNICA_SOLUZIONE);
        sub2.setId(102L);

        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of(sub1, sub2));

        LocalDate firstOfMonth = LocalDate.of(2025, 6, 1);
        sub1.setLastRenewalDate(firstOfMonth.minusMonths(1)); // rinnovo dovuto
        sub2.setLastRenewalDate(firstOfMonth.minusMonths(1)); // rinnovo dovuto

        // sub1 will throw when save is called
        doThrow(new RuntimeException("DB error")).when(subscriptionRepository).save(sub1);

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class, invocation -> {
            if (invocation.getMethod().getName().equals("now")) {
                return firstOfMonth;
            }
            return invocation.callRealMethod();
        })) {
            scheduler.renewCredits();
        }

        // sub2 should still be saved
        verify(subscriptionRepository).save(sub2);
        assertThat(sub2.getCurrentCreditsPT()).isEqualTo(plan.getMonthlyCreditsPT());
    }

    // ─── Multiple subscriptions ───────────────────────────────────────────────────

    @Test
    @DisplayName("renewCredits: multiple active subscriptions on first of month → all saved")
    void renewCredits_multipleActiveSubs_allSaved() {
        Subscription sub1 = buildActiveSubscription(PaymentFrequency.UNICA_SOLUZIONE);
        sub1.setId(200L);

        Subscription sub2 = buildActiveSubscription(PaymentFrequency.UNICA_SOLUZIONE);
        sub2.setId(201L);

        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of(sub1, sub2));

        LocalDate firstOfMonth = LocalDate.of(2025, 7, 1);
        sub1.setLastRenewalDate(firstOfMonth.minusMonths(1)); // rinnovo dovuto
        sub2.setLastRenewalDate(firstOfMonth.minusMonths(1)); // rinnovo dovuto

        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class, invocation -> {
            if (invocation.getMethod().getName().equals("now")) {
                return firstOfMonth;
            }
            return invocation.callRealMethod();
        })) {
            scheduler.renewCredits();
        }

        verify(subscriptionRepository).save(sub1);
        verify(subscriptionRepository).save(sub2);
        assertThat(sub1.getCurrentCreditsPT()).isEqualTo(plan.getMonthlyCreditsPT());
        assertThat(sub2.getCurrentCreditsPT()).isEqualTo(plan.getMonthlyCreditsPT());
    }
}
