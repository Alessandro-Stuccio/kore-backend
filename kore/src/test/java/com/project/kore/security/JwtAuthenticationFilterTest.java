package com.project.kore.security;

import com.project.kore.enums.Role;
import com.project.kore.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal — nessun header Authorization: chain continua")
    void doFilterInternal_noAuthHeader_chainContinues() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal — header senza prefisso Bearer: chain continua")
    void doFilterInternal_bearerPrefixMissing_chainContinues() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal — token valido: imposta SecurityContext e continua chain")
    void doFilterInternal_validToken_setsSecurityContext() throws Exception {
        String jwt = "valid.jwt.token";
        String email = "user@test.com";

        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setRole(Role.CLIENT);

        request.addHeader("Authorization", "Bearer " + jwt);
        when(jwtUtil.extractUsername(jwt)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(user);
        when(jwtUtil.isTokenValid(jwt, user)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal — token scaduto: restituisce 401 e interrompe chain")
    void doFilterInternal_expiredToken_returns401() throws Exception {
        String jwt = "expired.jwt.token";
        request.addHeader("Authorization", "Bearer " + jwt);
        when(jwtUtil.extractUsername(jwt)).thenThrow(new ExpiredJwtException(null, null, "Token scaduto"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("doFilterInternal — firma JWT non valida: restituisce 401 e interrompe chain")
    void doFilterInternal_invalidSignature_returns401() throws Exception {
        String jwt = "bad.signature.token";
        request.addHeader("Authorization", "Bearer " + jwt);
        when(jwtUtil.extractUsername(jwt)).thenThrow(new SignatureException("Firma non valida"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("doFilterInternal — utente disabilitato (deleted): restituisce 401 e interrompe chain")
    void doFilterInternal_disabledUser_returns401() throws Exception {
        String jwt = "valid.jwt.for.deleted.user";
        String email = "deleted@test.com";

        User disabledUser = new User();
        disabledUser.setId(2L);
        disabledUser.setEmail(email);
        disabledUser.setRole(Role.CLIENT);
        disabledUser.setDeleted(true);

        request.addHeader("Authorization", "Bearer " + jwt);
        when(jwtUtil.extractUsername(jwt)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(disabledUser);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("doFilterInternal — eccezione generica: restituisce 401 e interrompe chain")
    void doFilterInternal_genericException_returns401() throws Exception {
        String jwt = "unparseable.token";
        request.addHeader("Authorization", "Bearer " + jwt);
        when(jwtUtil.extractUsername(jwt)).thenThrow(new RuntimeException("errore inaspettato"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(filterChain, never()).doFilter(any(), any());
    }
}
