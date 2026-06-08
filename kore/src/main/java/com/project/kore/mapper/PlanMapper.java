package com.project.kore.mapper;

import com.project.kore.dto.request.PlanCreateRequestDTO;
import com.project.kore.dto.response.PlanResponseDTO;
import com.project.kore.enums.PlanDuration;
import com.project.kore.model.Plan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte i piani tra entità e DTO di richiesta/risposta.
 */
@Component
public class PlanMapper {

    /**
     * Converte un piano nel suo DTO di risposta.
     *
     * @param p il piano da convertire
     * @return il DTO del piano, oppure {@code null} se l'input è {@code null}
     */
    public PlanResponseDTO toResponse(Plan p) {
        if (p == null) return null;
        return PlanResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .duration(p.getDuration() != null ? p.getDuration().name() : null)
                .fullPrice(p.getFullPrice())
                .monthlyInstallmentPrice(p.getMonthlyInstallmentPrice())
                .monthlyCreditsPT(p.getMonthlyCreditsPT())
                .monthlyCreditsNutri(p.getMonthlyCreditsNutri())
                .active(p.isActive())
                .build();
    }

    /**
     * Converte una lista di piani nei rispettivi DTO.
     *
     * @param plans i piani da convertire
     * @return i DTO dei piani
     */
    public List<PlanResponseDTO> toResponseList(List<Plan> plans) {
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Costruisce un piano a partire dalla richiesta di creazione.
     *
     * @param request dati del piano da creare
     * @return il nuovo piano
     */
    public Plan toPlan(PlanCreateRequestDTO request) {
        PlanDuration duration = PlanDuration.valueOf(request.duration());
        return Plan.builder()
                .name(request.name())
                .duration(duration)
                .fullPrice(request.fullPrice())
                .monthlyInstallmentPrice(request.monthlyInstallmentPrice())
                .monthlyCreditsPT(request.monthlyCreditsPT() != null ? request.monthlyCreditsPT() : 0)
                .monthlyCreditsNutri(request.monthlyCreditsNutri() != null ? request.monthlyCreditsNutri() : 0)
                .build();
    }

    /**
     * Aggiornamento parziale: sovrascrive solo i campi valorizzati nel DTO, lasciando intatti
     * quelli null o blank.
     *
     * @param request  dati aggiornati (i campi nulli/blank vengono ignorati)
     * @param existing il piano da aggiornare in place
     */
    public void updatePlanFromRequest(PlanCreateRequestDTO request, Plan existing) {
        if (request.name() != null && !request.name().isBlank())
            existing.setName(request.name());
        if (request.duration() != null && !request.duration().isBlank())
            existing.setDuration(PlanDuration.valueOf(request.duration()));
        if (request.fullPrice() != null)
            existing.setFullPrice(request.fullPrice());
        if (request.monthlyInstallmentPrice() != null)
            existing.setMonthlyInstallmentPrice(request.monthlyInstallmentPrice());
        if (request.monthlyCreditsPT() != null)
            existing.setMonthlyCreditsPT(request.monthlyCreditsPT());
        if (request.monthlyCreditsNutri() != null)
            existing.setMonthlyCreditsNutri(request.monthlyCreditsNutri());
    }
}
