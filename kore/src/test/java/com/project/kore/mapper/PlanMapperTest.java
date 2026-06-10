package com.project.kore.mapper;

import com.project.kore.dto.request.PlanCreateRequest;
import com.project.kore.dto.response.PlanResponse;
import com.project.kore.enums.PlanDuration;
import com.project.kore.model.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanMapperTest {

    private PlanMapper planMapper;

    @BeforeEach
    void setUp() {
        planMapper = new PlanMapper();
    }

    // ---- helpers ----

    private Plan buildPlan(Long id, String name, PlanDuration duration, double fullPrice,
                           double monthlyPrice, int creditsPT, int creditsNutri) {
        Plan plan = new Plan();
        plan.setId(id);
        plan.setName(name);
        plan.setDuration(duration);
        plan.setFullPrice(fullPrice);
        plan.setMonthlyInstallmentPrice(monthlyPrice);
        plan.setMonthlyCreditsPT(creditsPT);
        plan.setMonthlyCreditsNutri(creditsNutri);
        return plan;
    }

    // ---- toResponse: null guard ----

    @Test
    @DisplayName("toResponse: returns null for null plan")
    void toResponse_nullPlan_returnsNull() {
        assertThat(planMapper.toResponse(null)).isNull();
    }

    // ---- toResponse: field mapping ----

    @Test
    @DisplayName("toResponse: maps all fields correctly for SEMESTRALE plan")
    void toResponse_semestralePlan_mapsAllFields() {
        Plan plan = buildPlan(1L, "Basic Semestrale", PlanDuration.SEMESTRALE, 300.0, 55.0, 1, 1);

        PlanResponse response = planMapper.toResponse(plan);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Basic Semestrale");
        assertThat(response.getDuration()).isEqualTo("SEMESTRALE");
        assertThat(response.getFullPrice()).isEqualTo(300.0);
        assertThat(response.getMonthlyInstallmentPrice()).isEqualTo(55.0);
        assertThat(response.getMonthlyCreditsPT()).isEqualTo(1);
        assertThat(response.getMonthlyCreditsNutri()).isEqualTo(1);
    }

    @Test
    @DisplayName("toResponse: maps duration as ANNUALE for annual plan")
    void toResponse_annualPlan_durationIsANNUALE() {
        Plan plan = buildPlan(2L, "Premium Annuale", PlanDuration.ANNUALE, 600.0, 55.0, 2, 2);

        PlanResponse response = planMapper.toResponse(plan);

        assertThat(response.getDuration()).isEqualTo("ANNUALE");
    }

    @Test
    @DisplayName("toResponse: duration is null when plan has null duration")
    void toResponse_nullDuration_durationIsNull() {
        Plan plan = new Plan();
        plan.setId(3L);
        plan.setName("NoDuration");
        plan.setDuration(null);

        PlanResponse response = planMapper.toResponse(plan);

        assertThat(response.getDuration()).isNull();
    }

    // ---- toResponseList ----

    @Test
    @DisplayName("toResponseList: maps all plans in list")
    void toResponseList_mapsAllPlans() {
        Plan p1 = buildPlan(1L, "Basic", PlanDuration.SEMESTRALE, 300.0, 55.0, 1, 1);
        Plan p2 = buildPlan(2L, "Premium", PlanDuration.ANNUALE, 600.0, 55.0, 2, 2);

        List<PlanResponse> result = planMapper.toResponseList(List.of(p1, p2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("toResponseList: returns empty list for empty input")
    void toResponseList_emptyInput_returnsEmptyList() {
        assertThat(planMapper.toResponseList(List.of())).isEmpty();
    }

    // ---- toPlan ----

    @Test
    @DisplayName("toPlan: maps all fields from request")
    void toPlan_validRequest_mapsAllFields() {
        PlanCreateRequest request = new PlanCreateRequest(
                "Basic Semestrale", "SEMESTRALE", 300.0, 55.0, 1, 1);

        Plan plan = planMapper.toPlan(request);

        assertThat(plan.getName()).isEqualTo("Basic Semestrale");
        assertThat(plan.getDuration()).isEqualTo(PlanDuration.SEMESTRALE);
        assertThat(plan.getFullPrice()).isEqualTo(300.0);
        assertThat(plan.getMonthlyInstallmentPrice()).isEqualTo(55.0);
        assertThat(plan.getMonthlyCreditsPT()).isEqualTo(1);
        assertThat(plan.getMonthlyCreditsNutri()).isEqualTo(1);
    }

    @Test
    @DisplayName("toPlan: maps ANNUALE duration")
    void toPlan_annualeDuration_parsesCorrectly() {
        PlanCreateRequest request = new PlanCreateRequest(
                "Premium Annuale", "ANNUALE", 600.0, 55.0, 2, 2);

        Plan plan = planMapper.toPlan(request);

        assertThat(plan.getDuration()).isEqualTo(PlanDuration.ANNUALE);
    }

    @Test
    @DisplayName("toPlan: null credits default to 0")
    void toPlan_nullCredits_defaultToZero() {
        PlanCreateRequest request = new PlanCreateRequest(
                "Basic", "SEMESTRALE", 300.0, 55.0, null, null);

        Plan plan = planMapper.toPlan(request);

        assertThat(plan.getMonthlyCreditsPT()).isEqualTo(0);
        assertThat(plan.getMonthlyCreditsNutri()).isEqualTo(0);
    }

    @Test
    @DisplayName("toPlan: throws exception for invalid duration string")
    void toPlan_invalidDuration_throwsIllegalArgumentException() {
        PlanCreateRequest request = new PlanCreateRequest(
                "Bad", "INVALID_DURATION", 100.0, 10.0, 1, 1);

        assertThatThrownBy(() -> planMapper.toPlan(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- updatePlanFromRequest ----

    @Test
    @DisplayName("updatePlanFromRequest: updates all non-null fields in existing plan")
    void updatePlanFromRequest_updatesAllFields() {
        Plan existing = buildPlan(1L, "Old Name", PlanDuration.SEMESTRALE, 200.0, 40.0, 1, 1);
        PlanCreateRequest request = new PlanCreateRequest(
                "New Name", "ANNUALE", 400.0, 70.0, 2, 2);

        planMapper.updatePlanFromRequest(request, existing);

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getDuration()).isEqualTo(PlanDuration.ANNUALE);
        assertThat(existing.getFullPrice()).isEqualTo(400.0);
        assertThat(existing.getMonthlyInstallmentPrice()).isEqualTo(70.0);
        assertThat(existing.getMonthlyCreditsPT()).isEqualTo(2);
        assertThat(existing.getMonthlyCreditsNutri()).isEqualTo(2);
    }

    @Test
    @DisplayName("updatePlanFromRequest: does not overwrite name when null in request")
    void updatePlanFromRequest_nullName_doesNotOverwriteName() {
        Plan existing = buildPlan(1L, "Preserved Name", PlanDuration.SEMESTRALE, 200.0, 40.0, 1, 1);
        PlanCreateRequest request = new PlanCreateRequest(
                null, "ANNUALE", 400.0, 70.0, 2, 2);

        planMapper.updatePlanFromRequest(request, existing);

        assertThat(existing.getName()).isEqualTo("Preserved Name");
    }

    @Test
    @DisplayName("updatePlanFromRequest: does not overwrite name when blank in request")
    void updatePlanFromRequest_blankName_doesNotOverwriteName() {
        Plan existing = buildPlan(1L, "Preserved Name", PlanDuration.SEMESTRALE, 200.0, 40.0, 1, 1);
        PlanCreateRequest request = new PlanCreateRequest(
                "   ", "SEMESTRALE", 200.0, 40.0, 1, 1);

        planMapper.updatePlanFromRequest(request, existing);

        assertThat(existing.getName()).isEqualTo("Preserved Name");
    }

    @Test
    @DisplayName("updatePlanFromRequest: does not overwrite price when null in request")
    void updatePlanFromRequest_nullPrice_doesNotOverwritePrice() {
        Plan existing = buildPlan(1L, "Plan", PlanDuration.SEMESTRALE, 200.0, 40.0, 1, 1);
        PlanCreateRequest request = new PlanCreateRequest(
                "Plan", "SEMESTRALE", null, null, 1, 1);

        planMapper.updatePlanFromRequest(request, existing);

        assertThat(existing.getFullPrice()).isEqualTo(200.0);
        assertThat(existing.getMonthlyInstallmentPrice()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("updatePlanFromRequest: does not overwrite credits when null in request")
    void updatePlanFromRequest_nullCredits_doesNotOverwriteCredits() {
        Plan existing = buildPlan(1L, "Plan", PlanDuration.SEMESTRALE, 200.0, 40.0, 3, 3);
        PlanCreateRequest request = new PlanCreateRequest(
                "Plan", "SEMESTRALE", 200.0, 40.0, null, null);

        planMapper.updatePlanFromRequest(request, existing);

        assertThat(existing.getMonthlyCreditsPT()).isEqualTo(3);
        assertThat(existing.getMonthlyCreditsNutri()).isEqualTo(3);
    }
}
