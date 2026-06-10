package com.project.kore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.kore.dto.request.ModeratorUserUpdateRequest;
import com.project.kore.dto.request.UpdateCreditsRequest;
import com.project.kore.dto.request.UserCreateRequest;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.facade.ModeratorFacade;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ModeratorControllerTest {

    @Mock
    ModeratorFacade moderatorFacade;

    @InjectMocks
    ModeratorController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RequestPostProcessor withModeratorUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User moderator = new User();
        moderator.setId(5L);
        moderator.setEmail("moderator1@test.com");
        moderator.setRole(Role.MODERATOR);
        moderator.setFirstName("Mod");
        moderator.setLastName("Eratore");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                moderator, null, List.of(new SimpleGrantedAuthority("ROLE_MODERATOR")));

        withModeratorUser = (MockHttpServletRequest request) -> {
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return request;
        };
    }

    // ------------------------------------------------------------------ GET /api/moderator/users

    @Test
    @DisplayName("GET /api/moderator/users — 200 con lista utenti gestibili")
    void getManageableUsers_returns200() throws Exception {
        UserResponse user = UserResponse.builder()
                .id(10L).firstName("Luca").lastName("Rossi").email("luca@test.com").role(Role.CLIENT)
                .build();
        when(moderatorFacade.getManageableUsers(any(User.class))).thenReturn(List.of(user));

        mockMvc.perform(get("/api/moderator/users").with(withModeratorUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].role").value("CLIENT"));
    }

    // ------------------------------------------------------------------ POST /api/moderator/users

    @Test
    @DisplayName("POST /api/moderator/users — 200 quando utente creato")
    void createUser_returns200() throws Exception {
        UserResponse created = UserResponse.builder()
                .id(20L).firstName("Anna").lastName("Verdi").email("anna@test.com").role(Role.CLIENT)
                .build();
        when(moderatorFacade.createUser(any(UserCreateRequest.class), any(User.class))).thenReturn(created);

        UserCreateRequest req = new UserCreateRequest(
                "anna@test.com", "Anna", "Verdi", "password123", "CLIENT",
                null, null, null, null);

        mockMvc.perform(post("/api/moderator/users")
                        .with(withModeratorUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.email").value("anna@test.com"));
    }

    // ------------------------------------------------------------------ PUT /api/moderator/users/{id}

    @Test
    @DisplayName("PUT /api/moderator/users/{id} — 200 con utente aggiornato")
    void updateUser_returns200() throws Exception {
        UserResponse updated = UserResponse.builder()
                .id(10L).firstName("Luca").lastName("Blu").email("luca@test.com").role(Role.CLIENT)
                .build();
        when(moderatorFacade.updateUser(anyLong(), any(ModeratorUserUpdateRequest.class), any(User.class)))
                .thenReturn(updated);

        ModeratorUserUpdateRequest req = new ModeratorUserUpdateRequest(
                "luca@test.com", "Luca", "Blu", null);

        mockMvc.perform(put("/api/moderator/users/10")
                        .with(withModeratorUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Blu"));
    }

    // ------------------------------------------------------------------ DELETE /api/moderator/users/{id}

    @Test
    @DisplayName("DELETE /api/moderator/users/{id} — 200 con messaggio di conferma")
    void deleteUser_returns200() throws Exception {
        doNothing().when(moderatorFacade).deleteUser(anyLong(), any(User.class));

        mockMvc.perform(delete("/api/moderator/users/10").with(withModeratorUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utente disabilitato"));
    }

    @Test
    @DisplayName("DELETE /api/moderator/users/{id} — 404 quando utente non trovato")
    void deleteUser_notFound_returns404() throws Exception {
        doThrow(new CustomResourceNotFoundException("Utente", 99L))
                .when(moderatorFacade).deleteUser(anyLong(), any(User.class));

        mockMvc.perform(delete("/api/moderator/users/99").with(withModeratorUser))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ GET /api/moderator/subscriptions

    @Test
    @DisplayName("GET /api/moderator/subscriptions — 200 con lista abbonamenti")
    void getAllSubscriptions_returns200() throws Exception {
        SubscriptionResponse sub = SubscriptionResponse.builder()
                .id(1L).userId(10L).userName("Luca Rossi").planName("Basic").active(true)
                .build();
        when(moderatorFacade.getAllSubscriptions()).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/moderator/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].planName").value("Basic"));
    }

    // ------------------------------------------------------------------ PUT /api/moderator/subscriptions/{id}/credits

    @Test
    @DisplayName("PUT /api/moderator/subscriptions/{id}/credits — 200 con crediti aggiornati")
    void updateSubscriptionCredits_returns200() throws Exception {
        SubscriptionResponse updated = SubscriptionResponse.builder()
                .id(1L).currentCreditsPT(2).currentCreditsNutri(1).active(true)
                .build();
        when(moderatorFacade.updateSubscriptionCredits(anyLong(), any(int.class), any(int.class)))
                .thenReturn(updated);

        UpdateCreditsRequest req = new UpdateCreditsRequest(2, 1);

        mockMvc.perform(put("/api/moderator/subscriptions/1/credits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentCreditsPT").value(2));
    }

    @Test
    @DisplayName("GET /api/moderator/chat-contacts — 200 con lista contatti chat")
    void getChatContacts_returns200() throws Exception {
        when(moderatorFacade.getChatContacts()).thenReturn(List.of());

        mockMvc.perform(get("/api/moderator/chat-contacts"))
                .andExpect(status().isOk());
    }
}
