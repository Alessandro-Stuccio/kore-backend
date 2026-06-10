package com.project.kore.facade.impl;

import com.project.kore.dto.response.PlanResponse;
import com.project.kore.facade.PlanFacade;
import com.project.kore.mapper.PlanMapper;
import com.project.kore.service.PlanService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestione dei piani di abbonamento.
 */
@Component
public class PlanFacadeImpl implements PlanFacade {

    private final PlanService planService;
    private final PlanMapper planMapper;

    public PlanFacadeImpl(PlanService planService, PlanMapper planMapper) {
        this.planService = planService;
        this.planMapper = planMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> getAllPlans() {
        return planMapper.toResponseList(planService.getActivePlans());
    }
}
