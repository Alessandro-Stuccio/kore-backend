package com.project.kore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.kore.dto.response.ActivityFeedItemResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.facade.ActivityFeedFacade;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ActivityFeedControllerTest {

    @Mock
    ActivityFeedFacade activityFeedFacade;

    @InjectMocks
    ActivityFeedController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RequestPostProcessor withClientUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User client = new User();
        client.setId(10L);
        client.setEmail("luca@test.com");
        client.setRole(Role.CLIENT);
        client.setFirstName("Luca");
        client.setLastName("Rossi");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                client, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        withClientUser = (MockHttpServletRequest request) -> {
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return request;
        };
    }

    // ------------------------------------------------------------------ GET /api/activity/feed

    @Test
    @DisplayName("GET /api/activity/feed — 200 con feed di attività (default params)")
    void getActivityFeed_defaultParams_returns200() throws Exception {
        ActivityFeedItemResponse item = ActivityFeedItemResponse.builder()
                .type("BOOKING")
                .text("Prenotazione con Mario PT confermata")
                .timestamp(LocalDateTime.now())
                .build();
        when(activityFeedFacade.getActivityFeed(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/activity/feed").with(withClientUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("BOOKING"))
                .andExpect(jsonPath("$[0].text").value("Prenotazione con Mario PT confermata"));
    }

    @Test
    @DisplayName("GET /api/activity/feed — 200 con lista vuota quando non ci sono attività")
    void getActivityFeed_empty_returns200() throws Exception {
        when(activityFeedFacade.getActivityFeed(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/activity/feed").with(withClientUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/activity/feed — 200 con parametri personalizzati days e size")
    void getActivityFeed_customParams_returns200() throws Exception {
        ActivityFeedItemResponse bookingItem = ActivityFeedItemResponse.builder()
                .type("BOOKING").text("Sessione di allenamento")
                .timestamp(LocalDateTime.now()).build();
        ActivityFeedItemResponse docItem = ActivityFeedItemResponse.builder()
                .type("DOCUMENT").text("Referto caricato")
                .timestamp(LocalDateTime.now().minusDays(1)).build();

        when(activityFeedFacade.getActivityFeed(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingItem, docItem));

        mockMvc.perform(get("/api/activity/feed")
                        .param("days", "7")
                        .param("size", "5")
                        .with(withClientUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/activity/feed — 200 con eventi di tipo DOCUMENT")
    void getActivityFeed_documentEvents_returns200() throws Exception {
        ActivityFeedItemResponse docItem = ActivityFeedItemResponse.builder()
                .type("DOCUMENT")
                .text("Piano di allenamento caricato dal tuo PT")
                .timestamp(LocalDateTime.now())
                .build();
        when(activityFeedFacade.getActivityFeed(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(docItem));

        mockMvc.perform(get("/api/activity/feed").with(withClientUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DOCUMENT"));
    }
}
