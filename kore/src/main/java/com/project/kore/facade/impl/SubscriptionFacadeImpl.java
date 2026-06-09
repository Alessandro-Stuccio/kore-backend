package com.project.kore.facade.impl;

import com.project.kore.enums.PaymentFrequency;
import com.project.kore.enums.PlanDuration;
import com.project.kore.facade.SubscriptionFacade;
import com.project.kore.model.Plan;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.service.SubscriptionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Attiva gli abbonamenti calcolando date, crediti e rate in base al piano e alla frequenza scelti.
 */
@Component
public class SubscriptionFacadeImpl implements SubscriptionFacade {

    private final SubscriptionService subscriptionService;

    public SubscriptionFacadeImpl(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Disattiva l'eventuale abbonamento attivo e ne crea uno nuovo: la scadenza dipende dalla
     * durata del piano (annuale o semestrale) e i crediti iniziali vengono presi dal piano.
     * In soluzione unica non c'è prossima scadenza di pagamento; a rate, le rate sono pari ai mesi
     * di durata e la prima scadenza è fra un mese.
     */
    @Override
    @Transactional
    public Subscription activateSubscription(User user, Plan plan, PaymentFrequency paymentFrequency) {
        subscriptionService.findActiveByUser(user).ifPresent(existing -> {
            existing.setActive(false);
            subscriptionService.save(existing);
        });

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = plan.getDuration() == PlanDuration.ANNUALE
                ? startDate.plusYears(1)
                : startDate.plusMonths(6);

        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setPlan(plan);
        sub.setPaymentFrequency(paymentFrequency);
        sub.setStartDate(startDate);
        sub.setEndDate(endDate);
        sub.setActive(true);
        sub.setCurrentCreditsPT(plan.getMonthlyCreditsPT());
        sub.setCurrentCreditsNutri(plan.getMonthlyCreditsNutri());
        sub.setLastRenewalDate(startDate);

        if (paymentFrequency == PaymentFrequency.UNICA_SOLUZIONE) {
            sub.setInstallmentsPaid(1);
            sub.setTotalInstallments(1);
            sub.setNextPaymentDate(null);
        } else {
            sub.setInstallmentsPaid(1);
            sub.setTotalInstallments(plan.getDuration().getMonths());
            sub.setNextPaymentDate(startDate.plusMonths(1));
        }

        validateInvariants(sub);
        return subscriptionService.save(sub);
    }

    // Invarianti relazionali/di stato ereditate dal vecchio SubscriptionBuilder.build(), applicate
    // qui all'unico chokepoint di CREATE (la save del service è condivisa con update/deattivazione).
    private static void validateInvariants(Subscription sub) {
        if (sub.getStartDate() != null && sub.getEndDate() != null
                && sub.getStartDate().isAfter(sub.getEndDate())) {
            throw new IllegalArgumentException("startDate non può essere successiva a endDate");
        }
        if (sub.getInstallmentsPaid() > sub.getTotalInstallments()) {
            throw new IllegalStateException("installmentsPaid (" + sub.getInstallmentsPaid()
                    + ") non può superare totalInstallments (" + sub.getTotalInstallments() + ")");
        }
        if (sub.getCurrentCreditsPT() < 0) {
            throw new IllegalArgumentException("currentCreditsPT non può essere negativo");
        }
        if (sub.getCurrentCreditsNutri() < 0) {
            throw new IllegalArgumentException("currentCreditsNutri non può essere negativo");
        }
    }

}
