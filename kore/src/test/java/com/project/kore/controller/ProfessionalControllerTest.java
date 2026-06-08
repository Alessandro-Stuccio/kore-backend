package com.project.kore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.kore.enums.Role;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.facade.ProfessionalFacade;
import com.project.kore.facade.UserFacade;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfessionalControllerTest {

    @Mock
    UserFacade userFacade;

    @Mock
    ProfessionalFacade professionalFacade;

    @InjectMocks
    ProfessionalController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
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
    @DisplayName("GET /api/professionals — 200 lista professionisti per ruolo")
    void getProfessionals_returns200() throws Exception {
        when(userFacade.findAvailableProfessionals(any(Role.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/professionals").param("role", "PERSONAL_TRAINER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/professionals/{id}/slots — 200 slot disponibili professionista")
    void getProfessionalSlots_returns200() throws Exception {
        when(professionalFacade.getAvailableSlots(anyLong()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/professionals/1/slots"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/professionals/slots — 200 crea slot per professionista autenticato")
    void createSlots_returns200() throws Exception {
        when(professionalFacade.createSlots(anyLong(), anyList()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/professionals/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of()))
                        .with(withPtUser))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/professionals/slots/{slotId} — 204 elimina slot")
    void deleteSlot_returns204() throws Exception {
        doNothing().when(professionalFacade).deleteSlot(anyLong(), anyLong());

        mockMvc.perform(delete("/api/professionals/slots/1").with(withPtUser))
                .andExpect(status().isNoContent());
    }
}
