package com.project.kore.security;

import com.project.kore.service.RandomGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil unit tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET_PLAIN = "12345678901234567890123456789012"; // 32 bytes
    private static final String SECRET_BASE64 = Base64.getEncoder().encodeToString(SECRET_PLAIN.getBytes());
    private static final long EXPIRATION_MS = 86_400_000L; // 24h

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        RandomGenerationService random = mock(RandomGenerationService.class);
        when(random.getTokenKey()).thenReturn(SECRET_BASE64);
        jwtUtil = new JwtUtil(random);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", EXPIRATION_MS);

        userDetails = new User(
                "mario@test.com",
                "encoded_pass",
                Collections.emptyList()
        );
    }

    // ─── generateToken ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken: returns a non-null, non-blank JWT string")
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken(userDetails);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("generateToken: generated token contains 3 JWT parts separated by dots")
    void generateToken_hasThreeParts() {
        String token = jwtUtil.generateToken(userDetails);

        assertThat(token.split("\\.")).hasSize(3);
    }

    // ─── extractUsername ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("extractUsername: returns the email embedded as subject in the token")
    void extractUsername_returnsEmailFromToken() {
        String token = jwtUtil.generateToken(userDetails);

        String username = jwtUtil.extractUsername(token);

        assertThat(username).isEqualTo("mario@test.com");
    }

    // ─── isTokenValid ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid: fresh token with matching user → returns true")
    void isTokenValid_freshTokenCorrectUser_returnsTrue() {
        String token = jwtUtil.generateToken(userDetails);

        boolean valid = jwtUtil.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: fresh token but wrong username → returns false")
    void isTokenValid_wrongUsername_returnsFalse() {
        String token = jwtUtil.generateToken(userDetails);

        UserDetails otherUser = new User(
                "other@test.com",
                "encoded_pass",
                Collections.emptyList()
        );

        boolean valid = jwtUtil.isTokenValid(token, otherUser);

        assertThat(valid).isFalse();
    }

    // ─── generatePasswordResetToken ───────────────────────────────────────────────

    @Test
    @DisplayName("generatePasswordResetToken: returns a non-null, non-blank JWT string")
    void generatePasswordResetToken_returnsNonNullToken() {
        String token = jwtUtil.generatePasswordResetToken("mario@test.com");

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("generatePasswordResetToken: token subject is the provided email")
    void generatePasswordResetToken_subjectIsEmail() {
        String token = jwtUtil.generatePasswordResetToken("mario@test.com");

        String subject = jwtUtil.extractUsername(token);

        assertThat(subject).isEqualTo("mario@test.com");
    }

    // ─── validatePasswordResetToken ───────────────────────────────────────────────

    @Test
    @DisplayName("validatePasswordResetToken: valid reset token → returns email")
    void validatePasswordResetToken_validToken_returnsEmail() {
        String token = jwtUtil.generatePasswordResetToken("mario@test.com");

        String email = jwtUtil.validatePasswordResetToken(token);

        assertThat(email).isEqualTo("mario@test.com");
    }

    @Test
    @DisplayName("validatePasswordResetToken: regular auth token passed → throws IllegalArgumentException")
    void validatePasswordResetToken_authTokenPassed_throwsIllegalArgumentException() {
        String authToken = jwtUtil.generateToken(userDetails);

        assertThatThrownBy(() -> jwtUtil.validatePasswordResetToken(authToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token non valido");
    }
}
