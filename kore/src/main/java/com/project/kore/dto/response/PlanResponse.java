package com.project.kore.dto.response;

/**
 * Un piano di abbonamento offerto, con prezzi (intero o rateizzato) e crediti mensili inclusi.
 */
public class PlanResponse {

    private Long id;
    private String name;
    private String duration;
    private Double fullPrice;
    private Double monthlyInstallmentPrice;
    private Integer monthlyCreditsPT;
    private Integer monthlyCreditsNutri;
    private Boolean active;


    private PlanResponse(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.duration = b.duration;
        this.fullPrice = b.fullPrice;
        this.monthlyInstallmentPrice = b.monthlyInstallmentPrice;
        this.monthlyCreditsPT = b.monthlyCreditsPT;
        this.monthlyCreditsNutri = b.monthlyCreditsNutri;
        this.active = b.active;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDuration() { return duration; }
    public Double getFullPrice() { return fullPrice; }
    public Double getMonthlyInstallmentPrice() { return monthlyInstallmentPrice; }
    public Integer getMonthlyCreditsPT() { return monthlyCreditsPT; }
    public Integer getMonthlyCreditsNutri() { return monthlyCreditsNutri; }
    public Boolean getActive() { return active; }

    public static class Builder {
        private Long id;
        private String name;
        private String duration;
        private Double fullPrice;
        private Double monthlyInstallmentPrice;
        private Integer monthlyCreditsPT;
        private Integer monthlyCreditsNutri;
        private Boolean active;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder duration(String duration) { this.duration = duration; return this; }
        public Builder fullPrice(Double fullPrice) { this.fullPrice = fullPrice; return this; }
        public Builder monthlyInstallmentPrice(Double v) { this.monthlyInstallmentPrice = v; return this; }
        public Builder monthlyCreditsPT(Integer v) { this.monthlyCreditsPT = v; return this; }
        public Builder monthlyCreditsNutri(Integer v) { this.monthlyCreditsNutri = v; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }

        public PlanResponse build() { return new PlanResponse(this); }
    }
}
