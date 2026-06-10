package com.project.kore.facade.impl;

import com.project.kore.dto.response.PlanResponse;
import com.project.kore.mapper.PlanMapper;
import com.project.kore.model.Plan;
import com.project.kore.enums.PlanDuration;
import com.project.kore.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanFacadeImpl unit tests")
class PlanFacadeImplTest {

    @Mock private PlanService planService;
    @Mock private PlanMapper planMapper;

    @InjectMocks
    private PlanFacadeImpl planFacade;

    private Plan plan;
    private PlanResponse planResponse;

    @BeforeEach
    void setUp() {
        plan = new Plan();
        plan.setId(1L);
        plan.setName("Basic");
        plan.setDuration(PlanDuration.SEMESTRALE);
        plan.setFullPrice(100.0);
        plan.setMonthlyInstallmentPrice(20.0);
        plan.setMonthlyCreditsPT(1);
        plan.setMonthlyCreditsNutri(1);

        planResponse = PlanResponse.builder()
                .id(1L).name("Basic").duration("SEMESTRALE")
                .fullPrice(100.0).monthlyInstallmentPrice(20.0)
                .monthlyCreditsPT(1).monthlyCreditsNutri(1)
                .build();
    }

    // ─── getAllPlans ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllPlans: delegates to service (active only) and mapper, returns list")
    void getAllPlans_returnsMappedList() {
        List<Plan> plans = List.of(plan);
        List<PlanResponse> expected = List.of(planResponse);
        when(planService.getActivePlans()).thenReturn(plans);
        when(planMapper.toResponseList(plans)).thenReturn(expected);

        List<PlanResponse> result = planFacade.getAllPlans();

        assertThat(result).isEqualTo(expected);
        verify(planService).getActivePlans();
        verify(planMapper).toResponseList(plans);
    }

    @Test
    @DisplayName("getAllPlans: empty service result returns empty list")
    void getAllPlans_emptyList_returnsEmpty() {
        when(planService.getActivePlans()).thenReturn(List.of());
        when(planMapper.toResponseList(List.of())).thenReturn(List.of());

        List<PlanResponse> result = planFacade.getAllPlans();

        assertThat(result).isEmpty();
    }

}
