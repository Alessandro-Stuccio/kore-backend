package com.project.kore.model;

import com.project.kore.builder.PlanBuilder;
import com.project.kore.builder.impl.PlanBuilderImpl;
import com.project.kore.enums.PlanDuration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

/**
 * Un piano di abbonamento offerto dalla piattaforma. È semestrale o annuale, pagabile in
 * un'unica soluzione o a rate mensili, e assegna ogni mese un certo numero di crediti distinti
 * per PT e nutrizionista (es. Basic 1+1, Premium 2+2). Il nome è univoco.
 */
@Entity
@Table(name = "plans", uniqueConstraints = {
        @UniqueConstraint(name = "uq_plan_name", columnNames = {"name"})
})
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome univoco, es. "Basic Semestrale"
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanDuration duration;

    // Prezzo pieno e prezzo della singola rata mensile
    @Column(nullable = false)
    private Double fullPrice;

    @Column(nullable = false)
    private Double monthlyInstallmentPrice;

    // Crediti che il piano regala ogni mese, per tipo di professionista
    private int monthlyCreditsPT;
    private int monthlyCreditsNutri;

    // Se false il piano sparisce dalla vetrina ma resta valido per chi è già abbonato;
    // l'admin può riattivarlo
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;

    public Plan() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PlanDuration getDuration() { return duration; }
    public void setDuration(PlanDuration duration) { this.duration = duration; }

    public Double getFullPrice() { return fullPrice; }
    public void setFullPrice(Double fullPrice) { this.fullPrice = fullPrice; }

    public Double getMonthlyInstallmentPrice() { return monthlyInstallmentPrice; }
    public void setMonthlyInstallmentPrice(Double monthlyInstallmentPrice) { this.monthlyInstallmentPrice = monthlyInstallmentPrice; }

    public int getMonthlyCreditsPT() { return monthlyCreditsPT; }
    public void setMonthlyCreditsPT(int monthlyCreditsPT) { this.monthlyCreditsPT = monthlyCreditsPT; }

    public int getMonthlyCreditsNutri() { return monthlyCreditsNutri; }
    public void setMonthlyCreditsNutri(int monthlyCreditsNutri) { this.monthlyCreditsNutri = monthlyCreditsNutri; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public static PlanBuilder builder() {
        return new PlanBuilderImpl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plan that = (Plan) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Plan{id=" + id + ", name='" + name + "', duration=" + duration + ", fullPrice=" + fullPrice + ", monthlyCreditsPT=" + monthlyCreditsPT + ", monthlyCreditsNutri=" + monthlyCreditsNutri + "}";
    }
}
