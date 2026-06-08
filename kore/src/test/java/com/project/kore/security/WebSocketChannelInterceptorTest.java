package com.project.kore.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketChannelInterceptor unit tests")
class WebSocketChannelInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private MessageChannel channel;

    private WebSocketChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketChannelInterceptor(jwtUtil, userDetailsService);
    }

    // ─── helper ───────────────────────────────────────────────────────────────────

    /**
     * Builds a STOMP message for the given command, optionally including an Authorization header.
     */
    private Message<byte[]> buildStompMessage(StompCommand command, String authHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId("session-123");
        if (authHeader != null) {
            accessor.addNativeHeader("Authorization", authHeader);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    // ─── non-CONNECT frames ───────────────────────────────────────────────────────

    @Test
    @DisplayName("preSend: SUBSCRIBE frame is not CONNECT → passes through unchanged")
    void preSend_subscribeFrame_passesThroughUnchanged() {
        Message<byte[]> message = buildStompMessage(StompCommand.SUBSCRIBE, null);

        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isSameAs(message);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("preSend: SEND frame is not CONNECT → passes through unchanged")
    void preSend_sendFrame_passesThroughUnchanged() {
        Message<byte[]> message = buildStompMessage(StompCommand.SEND, null);

        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isSameAs(message);
    }

    @Test
    @DisplayName("preSend: DISCONNECT frame is not CONNECT → passes through unchanged")
    void preSend_disconnectFrame_passesThroughUnchanged() {
        Message<byte[]> message = buildStompMessage(StompCommand.DISCONNECT, null);

        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isSameAs(message);
    }

    // ─── CONNECT without Authorization header ────────────────────────────────────

    @Test
    @DisplayName("preSend: CONNECT without Authorization header → throws MessagingException")
    void preSend_connectWithoutAuthHeader_throwsMessagingException() {
        Message<byte[]> message = buildStompMessage(StompCommand.CONNECT, null);

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Authorization");
    }

    @Test
    @DisplayName("preSend: CONNECT with malformed Authorization header (no Bearer prefix) → throws MessagingException")
    void preSend_connectWithMalformedAuthHeader_throwsMessagingException() {
        Message<byte[]> message = buildStompMessage(StompCommand.CONNECT, "Token abc123");

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessagingException.class);
    }

    // ─── CONNECT with invalid JWT ─────────────────────────────────────────────────

    @Test
    @DisplayName("preSend: CONNECT with JWT that fails isTokenValid → throws MessagingException")
    void preSend_connectWithInvalidJwt_throwsMessagingException() {
        Message<byte[]> message = buildStompMessage(StompCommand.CONNECT, "Bearer invalid.jwt.token");

        UserDetails userDetails = new User("mario@test.com", "pass", Collections.emptyList());
        when(jwtUtil.extractUsername("invalid.jwt.token")).thenReturn("mario@test.com");
        when(userDetailsService.loadUserByUsername("mario@test.com")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid("invalid.jwt.token", userDetails)).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("Invalid JWT");
    }

    @Test
    @DisplayName("preSend: CONNECT with JWT that throws on extractUsername → wraps in MessagingException")
    void preSend_connectJwtExtractionThrows_throwsMessagingException() {
        Message<byte[]> message = buildStompMessage(StompCommand.CONNECT, "Bearer bad.token");

        when(jwtUtil.extractUsername("bad.token"))
                .thenThrow(new RuntimeException("JWT parse error"));

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("JWT validation failed");
    }

    // ─── CONNECT with valid JWT ───────────────────────────────────────────────────

    @Test
    @DisplayName("preSend: CONNECT with valid JWT → sets user on accessor and returns the message")
    void preSend_connectWithValidJwt_setsUserAndReturnsMessage() {
        Message<byte[]> message = buildStompMessage(StompCommand.CONNECT, "Bearer valid.jwt.token");

        UserDetails userDetails = new User("mario@test.com", "pass", Collections.emptyList());
        when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("mario@test.com");
        when(userDetailsService.loadUserByUsername("mario@test.com")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid("valid.jwt.token", userDetails)).thenReturn(true);

        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isNotNull();
    }
}
