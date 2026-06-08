package com.project.kore.facade.impl;

import com.project.kore.dto.request.PlanCreateRequestDTO;
import com.project.kore.dto.response.PlanResponseDTO;
import com.project.kore.dto.response.stats.AdminStatsResponse;
import com.project.kore.enums.PlanDuration;
import com.project.kore.enums.Role;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import com.project.kore.facade.SubscriptionFacade;
import com.project.kore.mapper.PlanMapper;
import com.project.kore.mapper.SubscriptionMapper;
import com.project.kore.mapper.UserMapper;
import com.project.kore.model.Plan;
import com.project.kore.model.Slot;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.service.ChatService;
import com.project.kore.service.PlanService;
import com.project.kore.service.SlotService;
import com.project.kore.service.SubscriptionService;
import com.project.kore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminFacadeImpl unit tests")
class AdminFacadeImplTest {

    @Mock private ChatService chatService;
    @Mock private UserService userService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private UserMapper userMapper;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private PlanService planService;
    @Mock private SlotService slotService;
    @Mock private PlanMapper planMapper;
    @Mock private SubscriptionFacade subscriptionFacade;

    @InjectMocks
    private AdminFacadeImpl facade;

    private Plan plan;
    private PlanResponseDTO planResponseDTO;
    private PlanCreateRequestDTO createRequest;

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

        planResponseDTO = PlanResponseDTO.builder()
                .id(1L).name("Basic").duration("SEMESTRALE")
                .fullPrice(100.0).monthlyInstallmentPrice(20.0)
                .monthlyCreditsPT(1).monthlyCreditsNutri(1)
                .build();

        createRequest = new PlanCreateRequestDTO("Basic", "SEMESTRALE", 100.0, 20.0, 1, 1);
    }

    // ─── createPlan ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createPlan: creates plan successfully when all fields are valid and name is unique")
    void createPlan_validRequest_success() {
        when(planService.existsByName("Basic")).thenReturn(false);
        when(planMapper.toPlan(createRequest)).thenReturn(plan);
        when(planService.createPlan(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponseDTO);

        PlanResponseDTO result = facade.createPlan(createRequest);

        assertThat(result).isEqualTo(planResponseDTO);
        verify(planService).createPlan(plan);
    }

    @Test
    @DisplayName("createPlan: throws ResourceAlreadyExistsException when plan name already exists")
    void createPlan_duplicateName_throwsAlreadyExists() {
        when(planService.existsByName("Basic")).thenReturn(true);

        assertThatThrownBy(() -> facade.createPlan(createRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(planService, never()).createPlan(any());
    }

    @Test
    @DisplayName("createPlan: throws IllegalArgumentException when name is null")
    void createPlan_nullName_throwsIllegalArgument() {
        PlanCreateRequestDTO invalidRequest = new PlanCreateRequestDTO(null, "SEMESTRALE", 100.0, 20.0, 1, 1);

        assertThatThrownBy(() -> facade.createPlan(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Campi obbligatori");
    }

    @Test
    @DisplayName("createPlan: throws IllegalArgumentException when duration is null")
    void createPlan_nullDuration_throwsIllegalArgument() {
        PlanCreateRequestDTO invalidRequest = new PlanCreateRequestDTO("New", null, 100.0, 20.0, 1, 1);

        assertThatThrownBy(() -> facade.createPlan(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Campi obbligatori");
    }

    @Test
    @DisplayName("createPlan: throws IllegalArgumentException when fullPrice is null")
    void createPlan_nullFullPrice_throwsIllegalArgument() {
        PlanCreateRequestDTO invalidRequest = new PlanCreateRequestDTO("New", "SEMESTRALE", null, 20.0, 1, 1);

        assertThatThrownBy(() -> facade.createPlan(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("createPlan: throws IllegalArgumentException when mapper raises IllegalArgumentException (invalid duration)")
    void createPlan_invalidDurationString_throwsIllegalArgument() {
        PlanCreateRequestDTO badDuration = new PlanCreateRequestDTO("NewPlan", "INVALID", 100.0, 20.0, 1, 1);

        when(planService.existsByName("NewPlan")).thenReturn(false);
        when(planMapper.toPlan(badDuration)).thenThrow(new IllegalArgumentException("Bad enum"));

        assertThatThrownBy(() -> facade.createPlan(badDuration))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Durata non valida");
    }

    // ─── updatePlan ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePlan: updates plan when name is changed to a unique value")
    void updatePlan_newUniqueName_success() {
        PlanCreateRequestDTO updateRequest = new PlanCreateRequestDTO("Premium", "ANNUALE", 200.0, 30.0, 2, 2);

        when(planService.getPlanById(1L)).thenReturn(plan);
        when(planService.existsByName("Premium")).thenReturn(false);
        when(planService.createPlan(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponseDTO);

        PlanResponseDTO result = facade.updatePlan(1L, updateRequest);

        assertThat(result).isEqualTo(planResponseDTO);
        verify(planMapper).updatePlanFromRequest(updateRequest, plan);
        verify(planService).createPlan(plan);
    }

    @Test
    @DisplayName("updatePlan: does not check name uniqueness when name is unchanged")
    void updatePlan_sameName_skipsUniquenessCheck() {
        PlanCreateRequestDTO updateRequest = new PlanCreateRequestDTO("Basic", "ANNUALE", 200.0, 30.0, 2, 2);

        when(planService.getPlanById(1L)).thenReturn(plan);
        when(planService.createPlan(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponseDTO);

        facade.updatePlan(1L, updateRequest);

        verify(planService, never()).existsByName(any());
    }

    @Test
    @DisplayName("updatePlan: throws ResourceAlreadyExistsException when new name is already taken")
    void updatePlan_duplicateNewName_throwsAlreadyExists() {
        PlanCreateRequestDTO updateRequest = new PlanCreateRequestDTO("Premium", "ANNUALE", 200.0, 30.0, 2, 2);

        when(planService.getPlanById(1L)).thenReturn(plan);
        when(planService.existsByName("Premium")).thenReturn(true);

        assertThatThrownBy(() -> facade.updatePlan(1L, updateRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(planService, never()).createPlan(any());
    }

    @Test
    @DisplayName("updatePlan: throws IllegalArgumentException when mapper raises IllegalArgumentException (invalid duration)")
    void updatePlan_invalidDuration_throwsIllegalArgument() {
        PlanCreateRequestDTO updateRequest = new PlanCreateRequestDTO("Basic", "INVALID", 100.0, 20.0, 1, 1);

        when(planService.getPlanById(1L)).thenReturn(plan);
        doThrow(new IllegalArgumentException("Bad enum"))
                .when(planMapper).updatePlanFromRequest(updateRequest, plan);

        assertThatThrownBy(() -> facade.updatePlan(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Durata non valida");
    }

    // ─── setPlanStatus / getAllPlansForAdmin ───────────────────────────────────────

    @Test
    @DisplayName("setPlanStatus(disable): disables plan when no subscriptions are linked")
    void setPlanStatus_disable_noSubscriptions_success() {
        when(subscriptionService.hasSubscribersByPlan(1L)).thenReturn(false);
        when(planService.setActive(1L, false)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponseDTO);

        PlanResponseDTO result = facade.setPlanStatus(1L, false);

        assertThat(result).isEqualTo(planResponseDTO);
        verify(planService).setActive(1L, false);
    }

    @Test
    @DisplayName("setPlanStatus(disable): throws IllegalStateException when subscriptions are linked")
    void setPlanStatus_disable_withSubscriptions_throwsIllegalState() {
        when(subscriptionService.hasSubscribersByPlan(1L)).thenReturn(true);

        assertThatThrownBy(() -> facade.setPlanStatus(1L, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("abbonamenti");

        verify(planService, never()).setActive(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("setPlanStatus(enable): re-enables plan without checking subscriptions")
    void setPlanStatus_enable_success() {
        when(planService.setActive(1L, true)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponseDTO);

        PlanResponseDTO result = facade.setPlanStatus(1L, true);

        assertThat(result).isEqualTo(planResponseDTO);
        verify(planService).setActive(1L, true);
        verify(subscriptionService, never()).hasSubscribersByPlan(anyLong());
    }

    @Test
    @DisplayName("getAllPlansForAdmin: returns all plans (incl. disabled) mapped to DTOs")
    void getAllPlansForAdmin_returnsAll() {
        List<Plan> plans = List.of(plan);
        List<PlanResponseDTO> expected = List.of(planResponseDTO);
        when(planService.getAllPlans()).thenReturn(plans);
        when(planMapper.toResponseList(plans)).thenReturn(expected);

        List<PlanResponseDTO> result = facade.getAllPlansForAdmin();

        assertThat(result).isEqualTo(expected);
        verify(planService).getAllPlans();
    }

    // ─── getAdminStats ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAdminStats: returns non-null response with aggregated stats")
    void getAdminStats_returnsNonNullResponse() {
        when(userService.findAll()).thenReturn(List.of());
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of());
        when(planService.getAllPlans()).thenReturn(List.of());
        when(slotService.getAllBookedSlots()).thenReturn(List.of());

        AdminStatsResponse result = facade.getAdminStats();

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getAdminStats: totalUsers reflects user count from service")
    void getAdminStats_totalUsersMatchesUserCount() {
        User u1 = buildUser(Role.CLIENT, null);
        User u2 = buildUser(Role.CLIENT, null);

        when(userService.findAll()).thenReturn(List.of(u1, u2));
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of());
        when(planService.getAllPlans()).thenReturn(List.of());
        when(slotService.getAllBookedSlots()).thenReturn(List.of());

        AdminStatsResponse result = facade.getAdminStats();

        assertThat(result.getTotalUsers()).isEqualTo(2);
    }

    @Test
    @DisplayName("getAdminStats: totalActiveSubscriptions counts only active subscriptions")
    void getAdminStats_countsOnlyActiveSubscriptions() {
        Subscription active = buildSubscription(true);
        Subscription inactive = buildSubscription(false);

        when(userService.findAll()).thenReturn(List.of());
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of(active, inactive));
        when(planService.getAllPlans()).thenReturn(List.of());
        when(slotService.getAllBookedSlots()).thenReturn(List.of());

        AdminStatsResponse result = facade.getAdminStats();

        assertThat(result.getTotalActiveSubscriptions()).isEqualTo(1);
        assertThat(result.getTotalSubscriptions()).isEqualTo(2);
    }

    @Test
    @DisplayName("getAdminStats: bookingsThisMonth counts only slots booked in current month")
    void getAdminStats_bookingsThisMonth_correctCount() {
        Slot slotThisMonth = new Slot();
        slotThisMonth.setBookedAt(LocalDateTime.now());

        Slot slotLastMonth = new Slot();
        slotLastMonth.setBookedAt(LocalDateTime.now().minusMonths(2));

        Slot slotNoBookedAt = new Slot();
        // bookedAt is null — should be excluded

        when(userService.findAll()).thenReturn(List.of());
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of());
        when(planService.getAllPlans()).thenReturn(List.of());
        when(slotService.getAllBookedSlots()).thenReturn(List.of(slotThisMonth, slotLastMonth, slotNoBookedAt));

        AdminStatsResponse result = facade.getAdminStats();

        assertThat(result.getBookingsThisMonth()).isEqualTo(1);
        assertThat(result.getBookingsTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("getAdminStats: usersPerMonth covers the last 6 months")
    void getAdminStats_usersPerMonth_has6Entries() {
        when(userService.findAll()).thenReturn(List.of());
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of());
        when(planService.getAllPlans()).thenReturn(List.of());
        when(slotService.getAllBookedSlots()).thenReturn(List.of());

        AdminStatsResponse result = facade.getAdminStats();

        assertThat(result.getUsersPerMonth()).hasSize(6);
    }

    @Test
    @DisplayName("getAdminStats: professionalWorkload includes PT and NUTRITIONIST users only")
    void getAdminStats_workload_includesOnlyProfessionals() {
        User pt = buildUser(Role.PERSONAL_TRAINER, "Alice", "Smith");
        User client = buildUser(Role.CLIENT, null);

        when(userService.findAll()).thenReturn(List.of(pt, client));
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of());
        when(planService.getAllPlans()).thenReturn(List.of());
        when(slotService.getAllBookedSlots()).thenReturn(List.of());

        AdminStatsResponse result = facade.getAdminStats();

        assertThat(result.getProfessionalWorkload()).hasSize(1);
        assertThat(result.getProfessionalWorkload().get(0).getRole()).isEqualTo(Role.PERSONAL_TRAINER.name());
    }

    @Test
    @DisplayName("getAdminStats: monthlyRevenue is sum of active subscriptions installment prices")
    void getAdminStats_monthlyRevenue_sumOfActiveSubsPrices() {
        Subscription active1 = buildSubscription(true);
        Subscription active2 = buildSubscription(true);

        when(userService.findAll()).thenReturn(List.of());
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of(active1, active2));
        when(planService.getAllPlans()).thenReturn(List.of());
        when(slotService.getAllBookedSlots()).thenReturn(List.of());

        AdminStatsResponse result = facade.getAdminStats();

        // Both active subscriptions use plan with monthlyInstallmentPrice=20.0
        assertThat(result.getMonthlyRevenue()).isEqualTo(40.0);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────────

    private User buildUser(Role role, String firstName, String lastName) {
        User u = new User();
        u.setRole(role);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        return u;
    }

    private User buildUser(Role role, String ignored) {
        return buildUser(role, "First", "Last");
    }

    private Subscription buildSubscription(boolean active) {
        Subscription sub = new Subscription();
        sub.setActive(active);
        sub.setPlan(plan);
        sub.setCurrentCreditsPT(1);
        sub.setCurrentCreditsNutri(1);
        return sub;
    }
}
