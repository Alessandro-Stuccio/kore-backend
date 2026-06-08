package com.project.kore.controller;

import com.project.kore.dto.request.RegisterRequest;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.Role;
import com.project.kore.facade.AuthFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthFacade authFacade;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("register — chiama UserFacade e restituisce 200 con il profilo creato")
    void register() {
        RegisterRequest req = new RegisterRequest(null, null, "mario@test.com", null, null, null, null, null, null);
        UserResponse userResp = UserResponse.builder().id(1L).email("mario@test.com").role(Role.CLIENT).build();
        when(authFacade.registerUser(req)).thenReturn(userResp);

        ResponseEntity<UserResponse> response = authController.register(req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getEmail()).isEqualTo("mario@test.com");
    }

    @Test
    @DisplayName("ping — restituisce messaggio di health check")
    void ping() {
        ResponseEntity<Map<String, String>> response = authController.ping();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }
}
