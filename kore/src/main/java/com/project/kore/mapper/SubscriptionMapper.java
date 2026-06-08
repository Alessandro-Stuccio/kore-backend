package com.project.kore.mapper;

import com.project.kore.dto.request.RegisterRequest;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.enums.PaymentFrequency;
import com.project.kore.model.Plan;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte gli abbonamenti tra entità e DTO e ne costruisce di nuovi,
 * sia in fase di registrazione che da assegnazione admin.
 */
@Component
public class SubscriptionMapper {

    /**
     * Crea un abbonamento in fase di registrazione, prendendo la frequenza dalla richiesta.
     *
     * @param request dati di registrazione (da cui si legge la frequenza)
     * @param user    titolare dell'abbonamento
     * @param plan    piano scelto
     * @return il nuovo abbonamento, oppure {@code null} se un parametro è {@code null}
     */
    public Subscription toSubscription(RegisterRequest request, User user, Plan plan) {
        if (request == null || user == null || plan == null) return null;
        return buildSubscription(user, plan, request.paymentFrequency());
    }

    /**
     * Come toSubscription, ma la frequenza la passa direttamente l'admin.
     *
     * @param user             titolare dell'abbonamento
     * @param plan             piano scelto
     * @param paymentFrequency frequenza di pagamento
     * @return il nuovo abbonamento
     */
    public Subscription toSubscriptionFromAdmin(User user, Plan plan, PaymentFrequency paymentFrequency) {
        return buildSubscription(user, plan, paymentFrequency);
    }

    /**
     * Converte un abbonamento nel suo DTO di risposta.
     *
     * @param s l'abbonamento da convertire
     * @return il DTO dell'abbonamento, oppure {@code null} se l'input è {@code null}
     */
    public SubscriptionResponse toResponse(Subscription s) {
        if (s == null) return null;
        Plan plan = s.getPlan();
        Double monthlyPrice = plan != null ? plan.getMonthlyInstallmentPrice() : null;
        return SubscriptionResponse.builder()
                .id(s.getId())
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .userName(s.getUser() != null ? s.getUser().getFullName() : null)
                .planName(plan != null ? plan.getName() : null)
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .active(s.isActive())
                .currentCreditsPT(s.getCurrentCreditsPT())
                .currentCreditsNutri(s.getCurrentCreditsNutri())
                .monthlyPrice(monthlyPrice)
                .build();
    }

    /**
     * Converte una lista di abbonamenti nei rispettivi DTO.
     *
     * @param subscriptions gli abbonamenti da convertire
     * @return i DTO degli abbonamenti
     */
    public List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions) {
        return subscriptions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Calcola date, rate e crediti iniziali partendo dalla durata del piano:
    // inizio oggi, fine dopo i mesi del piano, una sola rata per il pagamento unico
    // altrimenti una al mese, e crediti PT/Nutri presi dal piano.
    private Subscription buildSubscription(User user, Plan plan, PaymentFrequency paymentFrequency) {
        LocalDate startDate = LocalDate.now();
        int months = plan.getDuration().getMonths();
        LocalDate endDate = startDate.plusMonths(months);
        int totalInstallments = paymentFrequency == PaymentFrequency.UNICA_SOLUZIONE ? 1 : months;

        return Subscription.builder()
                .user(user)
                .plan(plan)
                .paymentFrequency(paymentFrequency)
                .installmentsPaid(1)
                .totalInstallments(totalInstallments)
                .nextPaymentDate(paymentFrequency == PaymentFrequency.UNICA_SOLUZIONE ? null : startDate.plusMonths(1))
                .startDate(startDate)
                .endDate(endDate)
                .active(true)
                .currentCreditsPT(plan.getMonthlyCreditsPT())
                .currentCreditsNutri(plan.getMonthlyCreditsNutri())
                .lastRenewalDate(startDate)
                .build();
    }
}
