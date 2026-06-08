package com.project.kore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.kore.dto.request.PlanCreateRequestDTO;
import com.project.kore.dto.response.PlanResponseDTO;
import com.project.kore.dto.response.stats.AdminStatsResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.facade.AdminFacade;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    AdminFacade adminFacade;

    @InjectMocks
    AdminController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RequestPostProcessor withAdminUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ADMIN);
        admin.setFirstName("Admin");
        admin.setLastName("User");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                admin, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        withAdminUser = (MockHttpServletRequest request) -> {
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return request;
        };
    }

    // ------------------------------------------------------------------ GET /api/admin/users

    @Test
    @DisplayName("POST /api/admin/plans — 200 con piano creato")
    void createPlan_returns200() throws Exception {
        PlanResponseDTO plan = PlanResponseDTO.builder()
                .id(5L).name("Gold").duration("ANNUAL").fullPrice(299.0).monthlyInstallmentPrice(29.9)
                .monthlyCreditsPT(2).monthlyCreditsNutri(2)
                .build();
        when(adminFacade.createPlan(any(PlanCreateRequestDTO.class))).thenReturn(plan);

        PlanCreateRequestDTO req = new PlanCreateRequestDTO("Gold", "ANNUAL", 299.0, 29.9, 2, 2);

        mockMvc.perform(post("/api/admin/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Gold"));
    }

    // ------------------------------------------------------------------ GET /api/admin/plans

    @Test
    @DisplayName("GET /api/admin/plans — 200 con tutti i piani (inclusi disabilitati)")
    void getAllPlans_returns200() throws Exception {
        PlanResponseDTO active = PlanResponseDTO.builder()
                .id(1L).name("Basic").duration("SEMESTRALE").fullPrice(299.0).monthlyInstallmentPrice(59.0)
                .monthlyCreditsPT(1).monthlyCreditsNutri(1).active(true).build();
        PlanResponseDTO disabled = PlanResponseDTO.builder()
                .id(2L).name("Old").duration("ANNUALE").fullPrice(500.0).monthlyInstallmentPrice(45.0)
                .monthlyCreditsPT(1).monthlyCreditsNutri(1).active(false).build();
        when(adminFacade.getAllPlansForAdmin()).thenReturn(List.of(active, disabled));

        mockMvc.perform(get("/api/admin/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].active").value(false));
    }

    // ------------------------------------------------------------------ PATCH /api/admin/plans/{id}/disable|enable

    @Test
    @DisplayName("PATCH /api/admin/plans/{id}/disable — 200 con piano disabilitato")
    void disablePlan_returns200() throws Exception {
        PlanResponseDTO plan = PlanResponseDTO.builder()
                .id(5L).name("Gold").duration("ANNUALE").fullPrice(299.0).monthlyInstallmentPrice(29.9)
                .monthlyCreditsPT(2).monthlyCreditsNutri(2).active(false).build();
        when(adminFacade.setPlanStatus(5L, false)).thenReturn(plan);

        mockMvc.perform(patch("/api/admin/plans/5/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("PATCH /api/admin/plans/{id}/enable — 200 con piano riabilitato")
    void enablePlan_returns200() throws Exception {
        PlanResponseDTO plan = PlanResponseDTO.builder()
                .id(5L).name("Gold").duration("ANNUALE").fullPrice(299.0).monthlyInstallmentPrice(29.9)
                .monthlyCreditsPT(2).monthlyCreditsNutri(2).active(true).build();
        when(adminFacade.setPlanStatus(5L, true)).thenReturn(plan);

        mockMvc.perform(patch("/api/admin/plans/5/enable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    // ------------------------------------------------------------------ GET /api/admin/stats

    @Test
    @DisplayName("GET /api/admin/stats — 200 con statistiche")
    void getStats_returns200() throws Exception {
        AdminStatsResponse stats = AdminStatsResponse.builder()
                .totalUsers(50).totalActiveSubscriptions(30).bookingsTotal(200)
                .build();
        when(adminFacade.getAdminStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(50));
    }
}
