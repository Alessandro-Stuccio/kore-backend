package com.project.kore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.kore.dto.request.ProfileUpdateRequest;
import com.project.kore.dto.response.ClientBasicInfoResponse;
import com.project.kore.dto.response.ClientDashboardResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.exception.common.CustomResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    UserFacade userFacade;

    @InjectMocks
    UserController userController;

    private RequestPostProcessor withMockUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User mockUser = new User();
        mockUser.setId(10L);
        mockUser.setEmail("luca@test.com");
        mockUser.setRole(Role.CLIENT);
        mockUser.setFirstName("Luca");
        mockUser.setLastName("Rossi");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                mockUser, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        withMockUser = (MockHttpServletRequest request) -> {
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return request;
        };
    }

    // ------------------------------------------------------------------ GET /api/users/dashboard

    @Test
    @DisplayName("GET /api/users/dashboard — 200 con dati aggregati del cliente")
    void getDashboard_returnsClientDashboard() throws Exception {
        ClientDashboardResponse dashboard = new ClientDashboardResponse.Builder()
                .upcomingBookings(List.of())
                .followingProfessionals(List.of())
                .build();

        when(userFacade.getClientDashboard(anyLong())).thenReturn(dashboard);

        mockMvc.perform(get("/api/users/dashboard")
                        .with(withMockUser))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/users/dashboard — 404 quando l'utente non esiste")
    void getDashboard_userNotFound_returns404() throws Exception {
        when(userFacade.getClientDashboard(anyLong()))
                .thenThrow(new CustomResourceNotFoundException("Utente", 10L));

        mockMvc.perform(get("/api/users/dashboard")
                        .with(withMockUser))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ GET /api/users/clients

    @Test
    @DisplayName("GET /api/users/clients — 200 con lista clienti del professionista")
    void getClientsForProfessional_returnsList() throws Exception {
        ClientBasicInfoResponse client = new ClientBasicInfoResponse.Builder()
                .id(1L).firstName("Anna").lastName("Verdi").email("anna@test.com")
                .build();

        when(userFacade.getClientsForProfessional(anyLong())).thenReturn(List.of(client));

        mockMvc.perform(get("/api/users/clients")
                        .with(withMockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("anna@test.com"));
    }

    // ------------------------------------------------------------------ PUT /api/users/profile

    @Test
    @DisplayName("PUT /api/users/profile — 200 quando il profilo viene aggiornato")
    void updateProfile_returns200() throws Exception {
        ProfileUpdateRequest req = new ProfileUpdateRequest("Luca", "Verdi", null, null);

        mockMvc.perform(put("/api/users/profile")
                        .with(withMockUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ GET /api/users/admin

    @Test
    @DisplayName("GET /api/users/admin — 200 con dati contatto admin")
    void getAdmin_returnsAdminInfo() throws Exception {
        ClientBasicInfoResponse adminInfo = new ClientBasicInfoResponse.Builder()
                .id(99L).firstName("Admin").lastName("Sistema")
                .email("admin@test.com").role("ADMIN")
                .build();

        when(userFacade.getAdmin()).thenReturn(adminInfo);

        mockMvc.perform(get("/api/users/admin")
                        .with(withMockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
