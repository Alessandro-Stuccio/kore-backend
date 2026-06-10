package com.project.kore.dto.request;

import jakarta.validation.constraints.*;

/**
 * Crea o aggiorna un piano di abbonamento: nome, durata, prezzi (pieno e a rata mensile) e crediti mensili per PT e nutrizionista.
 *
 * @param name                     nome del piano (2-100 caratteri)
 * @param duration                 durata del piano (nome dell'enum, es. SEMI_ANNUAL/ANNUAL)
 * @param fullPrice                prezzo in un'unica soluzione
 * @param monthlyInstallmentPrice  importo della rata mensile
 * @param monthlyCreditsPT         crediti PT erogati ogni mese
 * @param monthlyCreditsNutri      crediti nutrizionista erogati ogni mese
 */
public record PlanCreateRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank String duration,
        @NotNull @Positive Double fullPrice,
        @NotNull @Positive Double monthlyInstallmentPrice,
        @Min(0) Integer monthlyCreditsPT,
        @Min(0) Integer monthlyCreditsNutri
) {}
