package com.project.kore.controller;

import com.project.kore.dto.response.PlanResponseDTO;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.facade.PlanFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlanControllerTest {

    MockMvc mockMvc;

    @Mock
    PlanFacade planFacade;

    @InjectMocks
    PlanController planController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(planController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    // ------------------------------------------------------------------ GET /api/plans

    @Test
    @DisplayName("GET /api/plans — 200 con lista di piani disponibili")
    void getAllPlans_returnsList() throws Exception {
        PlanResponseDTO basic = PlanResponseDTO.builder()
                .id(1L).name("Basic").duration("SEMESTRALE")
                .fullPrice(299.0).monthlyCreditsPT(1).monthlyCreditsNutri(1)
                .build();

        PlanResponseDTO premium = PlanResponseDTO.builder()
                .id(2L).name("Premium").duration("ANNUALE")
                .fullPrice(599.0).monthlyCreditsPT(2).monthlyCreditsNutri(2)
                .build();

        when(planFacade.getAllPlans()).thenReturn(List.of(basic, premium));

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Basic"))
                .andExpect(jsonPath("$[1].name").value("Premium"));
    }

    @Test
    @DisplayName("GET /api/plans — 200 con lista vuota quando non ci sono piani")
    void getAllPlans_emptyList_returns200() throws Exception {
        when(planFacade.getAllPlans()).thenReturn(List.of());

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/plans — 500 quando il facade lancia un'eccezione imprevista")
    void getAllPlans_serviceError_returns500() throws Exception {
        when(planFacade.getAllPlans()).thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isInternalServerError());
    }
}
