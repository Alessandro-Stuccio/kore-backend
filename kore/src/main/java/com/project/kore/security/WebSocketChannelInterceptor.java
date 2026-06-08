package com.project.kore.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Valida il JWT sul frame STOMP CONNECT prima di aprire il WebSocket e mette
 * l'utente autenticato come Principal della sessione, così è disponibile nei
 * metodi {@code @MessageMapping}.
 */
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChannelInterceptor.class);
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public WebSocketChannelInterceptor(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // Agisce solo sui frame CONNECT: estrae e valida il JWT dell'header Authorization
    // e imposta l'utente autenticato sulla sessione. Se il token manca o non è valido
    // solleva MessagingException e la connessione viene rifiutata.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String sessionId = accessor.getSessionId();
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[WS] CONNECT rifiutato (sessionId={}): header Authorization mancante o malformato.", sessionId);
            throw new MessagingException("Missing or invalid Authorization header on STOMP CONNECT");
        }

        String jwt = authHeader.substring(7);
        try {
            String email = jwtUtil.extractUsername(jwt);
            UserDetails user = userDetailsService.loadUserByUsername(email);
            if (!jwtUtil.isTokenValid(jwt, user)) {
                log.warn("[WS] CONNECT rifiutato (sessionId={}): JWT non valido per utente {}.", sessionId, email);
                throw new MessagingException("Invalid JWT on STOMP CONNECT");
            }
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            accessor.setUser(auth);
            log.info("[WS] CONNECT accettato (sessionId={}, user={}).", sessionId, email);
        } catch (MessagingException me) {
            throw me;
        } catch (Exception ex) {
            log.warn("[WS] CONNECT rifiutato (sessionId={}): errore validazione JWT — {}", sessionId, ex.getMessage());
            throw new MessagingException("JWT validation failed on STOMP CONNECT", ex);
        }

        return message;
    }
}
