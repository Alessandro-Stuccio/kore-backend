package com.project.kore.model;

import com.project.kore.enums.PaymentFrequency;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Objects;

/**
 * L'abbonamento di un utente: ogni utente ne ha al massimo uno (vincolo unico su user_id).
 * I crediti residui calano a ogni prenotazione e vengono ripristinati ogni mese dal
 * SubscriptionScheduler.
 */
@Entity
@Table(name = "subscriptions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_subscription_user", columnNames = {"user_id"})
})
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Version: optimistic locking, gestito da JPA
    @Version
    private Integer version;

    @NotNull(message = "user è obbligatorio")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subscription_user_id"))
    private User user;

    // EAGER perché ci serve subito per leggere i crediti del piano
    @NotNull(message = "plan è obbligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subscription_plan_id"))
    private Plan plan;

    @NotNull(message = "paymentFrequency è obbligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentFrequency paymentFrequency;

    // Rate: rilevanti solo col pagamento rateale
    private int installmentsPaid;
    private int totalInstallments;

    private LocalDate nextPaymentDate;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean active;

    // Crediti rimasti nel mese corrente, distinti per tipo di professionista
    private int currentCreditsPT;
    private int currentCreditsNutri;

    // Ultimo rinnovo mensile dei crediti fatto dallo scheduler
    private LocalDate lastRenewalDate;

    public Subscription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public PaymentFrequency getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(PaymentFrequency paymentFrequency) { this.paymentFrequency = paymentFrequency; }

    public int getInstallmentsPaid() { return installmentsPaid; }
    public void setInstallmentsPaid(int installmentsPaid) { this.installmentsPaid = installmentsPaid; }

    public int getTotalInstallments() { return totalInstallments; }
    public void setTotalInstallments(int totalInstallments) { this.totalInstallments = totalInstallments; }

    public LocalDate getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(LocalDate nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getCurrentCreditsPT() { return currentCreditsPT; }
    public void setCurrentCreditsPT(int currentCreditsPT) { this.currentCreditsPT = currentCreditsPT; }

    public int getCurrentCreditsNutri() { return currentCreditsNutri; }
    public void setCurrentCreditsNutri(int currentCreditsNutri) { this.currentCreditsNutri = currentCreditsNutri; }

    public LocalDate getLastRenewalDate() { return lastRenewalDate; }
    public void setLastRenewalDate(LocalDate lastRenewalDate) { this.lastRenewalDate = lastRenewalDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Subscription{id=" + id + ", paymentFrequency=" + paymentFrequency + ", active=" + active + ", currentCreditsPT=" + currentCreditsPT + ", currentCreditsNutri=" + currentCreditsNutri + "}";
    }
}
