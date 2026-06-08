package com.project.kore.builder.impl;

import com.project.kore.builder.PlanBuilder;
import com.project.kore.enums.PlanDuration;
import java.util.Objects;
import com.project.kore.model.*;


/**
 * Builder concreto per Plan. In build controlla i campi obbligatori, prezzi positivi e crediti non negativi.
 */
public class PlanBuilderImpl implements PlanBuilder {
    private Long id;
    private String name;
    private PlanDuration duration;
    private Double fullPrice;
    private Double monthlyInstallmentPrice;
    private int monthlyCreditsPT;
    private int monthlyCreditsNutri;
    private boolean active = true;

    @Override
    public PlanBuilder id(Long id) {
        this.id = id;
        return this;
    }
    @Override
    public PlanBuilder name(String name) {
        this.name = name;
        return this;
    }
    @Override
    public PlanBuilder duration(PlanDuration duration) {
        this.duration = duration;
        return this;
    }
    @Override
    public PlanBuilder fullPrice(Double fullPrice) {
        this.fullPrice = fullPrice;
        return this;
    }
    @Override
    public PlanBuilder monthlyInstallmentPrice(Double monthlyInstallmentPrice) {
        this.monthlyInstallmentPrice = monthlyInstallmentPrice;
        return this;
    }
    @Override
    public PlanBuilder monthlyCreditsPT(int monthlyCreditsPT) {
        this.monthlyCreditsPT = monthlyCreditsPT;
        return this;
    }
    @Override
    public PlanBuilder monthlyCreditsNutri(int monthlyCreditsNutri) {
        this.monthlyCreditsNutri = monthlyCreditsNutri;
        return this;
    }
    @Override
    public PlanBuilder active(boolean active) {
        this.active = active;
        return this;
    }
    @Override
    public Plan build() {
        Objects.requireNonNull(this.name, "name è obbligatorio");
        Objects.requireNonNull(this.duration, "duration è obbligatorio");
        Objects.requireNonNull(this.fullPrice, "fullPrice è obbligatorio");
        Objects.requireNonNull(this.monthlyInstallmentPrice, "monthlyInstallmentPrice è obbligatorio");

        if (this.name.isBlank())
            throw new IllegalArgumentException("name non può essere vuoto");
        if (this.fullPrice <= 0)
            throw new IllegalArgumentException("fullPrice deve essere maggiore di zero");
        if (this.monthlyInstallmentPrice <= 0)
            throw new IllegalArgumentException("monthlyInstallmentPrice deve essere maggiore di zero");
        if (this.monthlyCreditsPT < 0)
            throw new IllegalArgumentException("monthlyCreditsPT non può essere negativo");
        if (this.monthlyCreditsNutri < 0)
            throw new IllegalArgumentException("monthlyCreditsNutri non può essere negativo");

        Plan obj = new Plan();
        obj.setId(this.id);
        obj.setName(this.name);
        obj.setDuration(this.duration);
        obj.setFullPrice(this.fullPrice);
        obj.setMonthlyInstallmentPrice(this.monthlyInstallmentPrice);
        obj.setMonthlyCreditsPT(this.monthlyCreditsPT);
        obj.setMonthlyCreditsNutri(this.monthlyCreditsNutri);
        obj.setActive(this.active);
        return obj;
    }
}
