package com.project.kore.controller;

import com.project.kore.dto.response.stats.ProfessionalStatsResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.facade.ProfessionalFacade;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfessionalStatsControllerTest {

    @Mock
    ProfessionalFacade professionalFacade;

    @InjectMocks
    ProfessionalStatsController controller;

    private MockMvc mockMvc;
    private RequestPostProcessor withPtUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User pt = new User();
        pt.setId(1L);
        pt.setEmail("pt@test.com");
        pt.setRole(Role.PERSONAL_TRAINER);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                pt, null, List.of(new SimpleGrantedAuthority("ROLE_PERSONAL_TRAINER")));

        withPtUser = (MockHttpServletRequest request) -> {
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return request;
        };
    }

    @Test
    @DisplayName("GET /api/professional/stats — 200 con statistiche professionista")
    void getStats_returns200() throws Exception {
        when(professionalFacade.getProfessionalStats(anyLong()))
                .thenReturn(ProfessionalStatsResponse.builder().build());

        mockMvc.perform(get("/api/professional/stats").with(withPtUser))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/professional/bookings — 200 con lista appuntamenti futuri")
    void getUpcomingBookings_returns200() throws Exception {
        when(professionalFacade.getUpcomingBookings(anyLong()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/professional/bookings").with(withPtUser))
                .andExpect(status().isOk());
    }
}
