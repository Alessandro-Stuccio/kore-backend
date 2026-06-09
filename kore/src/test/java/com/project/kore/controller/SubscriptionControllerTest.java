package com.project.kore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.kore.dto.request.PlanRequest;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.enums.PaymentFrequency;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    UserFacade userFacade;

    @InjectMocks
    SubscriptionController subscriptionController;

    private RequestPostProcessor withMockUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(subscriptionController)
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

    // ------------------------------------------------------------------ POST /api/subscriptions/activate

    @Test
    @DisplayName("POST /api/subscriptions/activate — 200 quando l'abbonamento viene attivato")
    void activateSubscription_returnsSubscriptionResponse() throws Exception {
        SubscriptionResponse response = SubscriptionResponse.builder()
                .id(1L)
                .userId(10L)
                .planName("Basic")
                .active(true)
                .currentCreditsPT(1)
                .currentCreditsNutri(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .build();

        when(userFacade.activateSubscription(any(), anyLong())).thenReturn(response);

        PlanRequest req = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);

        mockMvc.perform(post("/api/subscriptions/activate")
                        .with(withMockUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planName").value("Basic"))
                .andExpect(jsonPath("$.active").value(true));
    }

    // La validazione del DTO non è più sul controller (niente @Valid) ma sul facade
    // (UserFacade.activateSubscription(@Valid PlanRequest), classe @Validated): un planId/paymentFrequency
    // null produce ConstraintViolationException → 400. Qui copriamo il vincolo che ora fa quel lavoro.
    @Test
    @DisplayName("PlanRequest — planId/paymentFrequency null violano i vincoli (validati ora sul facade → 400)")
    void activateSubscription_missingPlanId_violatesConstraints() {
        try (jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            jakarta.validation.Validator validator = factory.getValidator();
            PlanRequest req = new PlanRequest(null, null);

            var violations = validator.validate(req);

            assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                    .contains("planId", "paymentFrequency");
        }
    }

    @Test
    @DisplayName("POST /api/subscriptions/activate — 404 quando il piano non esiste")
    void activateSubscription_planNotFound_returns404() throws Exception {
        when(userFacade.activateSubscription(any(), anyLong()))
                .thenThrow(new CustomResourceNotFoundException("Piano", 99L));

        PlanRequest req = new PlanRequest(99L, PaymentFrequency.RATE_MENSILI);

        mockMvc.perform(post("/api/subscriptions/activate")
                        .with(withMockUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ GET /api/subscriptions/status

    @Test
    @DisplayName("GET /api/subscriptions/status — 200 con stato abbonamento attivo")
    void getSubscriptionStatus_returnsStatus() throws Exception {
        SubscriptionResponse response = SubscriptionResponse.builder()
                .id(1L)
                .userId(10L)
                .planName("Premium")
                .active(true)
                .currentCreditsPT(2)
                .currentCreditsNutri(2)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();

        when(userFacade.getSubscriptionStatus(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/subscriptions/status")
                        .with(withMockUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planName").value("Premium"))
                .andExpect(jsonPath("$.currentCreditsPT").value(2));
    }

    @Test
    @DisplayName("GET /api/subscriptions/status — 404 quando non c'è abbonamento attivo")
    void getSubscriptionStatus_noSubscription_returns404() throws Exception {
        when(userFacade.getSubscriptionStatus(anyLong()))
                .thenThrow(new CustomResourceNotFoundException("Abbonamento attivo non trovato per utente 10"));

        mockMvc.perform(get("/api/subscriptions/status")
                        .with(withMockUser))
                .andExpect(status().isNotFound());
    }
}
