package com.project.kore.builder.impl;

import com.project.kore.builder.SubscriptionBuilder;
import com.project.kore.enums.PaymentFrequency;
import java.time.LocalDate;
import java.util.Objects;
import com.project.kore.model.*;


/**
 * Builder concreto per Subscription. In build valida date coerenti, rate pagate entro il totale e crediti
 * non negativi.
 */
public class SubscriptionBuilderImpl implements SubscriptionBuilder {
    private Long id;
    private User user;
    private Plan plan;
    private PaymentFrequency paymentFrequency;
    private int installmentsPaid;
    private int totalInstallments;
    private LocalDate nextPaymentDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private int currentCreditsPT;
    private int currentCreditsNutri;
    private LocalDate lastRenewalDate;

    @Override
    public SubscriptionBuilder id(Long id) {
        this.id = id;
        return this;
    }
    @Override
    public SubscriptionBuilder user(User user) {
        this.user = user;
        return this;
    }
    @Override
    public SubscriptionBuilder plan(Plan plan) {
        this.plan = plan;
        return this;
    }
    @Override
    public SubscriptionBuilder paymentFrequency(PaymentFrequency paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
        return this;
    }
    @Override
    public SubscriptionBuilder installmentsPaid(int installmentsPaid) {
        this.installmentsPaid = installmentsPaid;
        return this;
    }
    @Override
    public SubscriptionBuilder totalInstallments(int totalInstallments) {
        this.totalInstallments = totalInstallments;
        return this;
    }
    @Override
    public SubscriptionBuilder nextPaymentDate(LocalDate nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
        return this;
    }
    @Override
    public SubscriptionBuilder startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }
    @Override
    public SubscriptionBuilder endDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }
    @Override
    public SubscriptionBuilder active(boolean active) {
        this.active = active;
        return this;
    }
    @Override
    public SubscriptionBuilder currentCreditsPT(int currentCreditsPT) {
        this.currentCreditsPT = currentCreditsPT;
        return this;
    }
    @Override
    public SubscriptionBuilder currentCreditsNutri(int currentCreditsNutri) {
        this.currentCreditsNutri = currentCreditsNutri;
        return this;
    }
    @Override
    public SubscriptionBuilder lastRenewalDate(LocalDate lastRenewalDate) {
        this.lastRenewalDate = lastRenewalDate;
        return this;
    }

    @Override
    public Subscription build() {
        Objects.requireNonNull(this.user, "user è obbligatorio");
        Objects.requireNonNull(this.plan, "plan è obbligatorio");
        Objects.requireNonNull(this.paymentFrequency, "paymentFrequency è obbligatorio");

        if (this.startDate != null && this.endDate != null && this.startDate.isAfter(this.endDate))
            throw new IllegalArgumentException("startDate non può essere successiva a endDate");
        if (this.installmentsPaid > this.totalInstallments)
            throw new IllegalStateException("installmentsPaid (" + this.installmentsPaid + ") non può superare totalInstallments (" + this.totalInstallments + ")");
        if (this.currentCreditsPT < 0)
            throw new IllegalArgumentException("currentCreditsPT non può essere negativo");
        if (this.currentCreditsNutri < 0)
            throw new IllegalArgumentException("currentCreditsNutri non può essere negativo");

        Subscription obj = new Subscription();
        obj.setId(this.id);
        obj.setUser(this.user);
        obj.setPlan(this.plan);
        obj.setPaymentFrequency(this.paymentFrequency);
        obj.setInstallmentsPaid(this.installmentsPaid);
        obj.setTotalInstallments(this.totalInstallments);
        obj.setNextPaymentDate(this.nextPaymentDate);
        obj.setStartDate(this.startDate);
        obj.setEndDate(this.endDate);
        obj.setActive(this.active);
        obj.setCurrentCreditsPT(this.currentCreditsPT);
        obj.setCurrentCreditsNutri(this.currentCreditsNutri);
        obj.setLastRenewalDate(this.lastRenewalDate);
        return obj;
    }
}
