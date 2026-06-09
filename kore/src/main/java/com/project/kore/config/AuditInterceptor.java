package com.project.kore.config;

import com.project.kore.model.AuditLog;
import com.project.kore.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Per ogni richiesta HTTP salva in modo asincrono un record nell'audit log:
 * utente, metodo, path, IP, status e, solo per POST/PUT/PATCH, il body troncato
 * a 2000 caratteri. Il body è leggibile perché {@link RequestBodyCachingFilter}
 * avvolge la request a monte.
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);
    private static final int MAX_BODY_LENGTH = 2000;
    private static final Set<String> BODY_METHODS = Set.of("POST", "PUT", "PATCH");

    private final AuditLogRepository auditLogRepository;

    public AuditInterceptor(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String userIdentity = extractUserIdentity();
        String method       = request.getMethod();
        String path         = request.getRequestURI();
        String ip           = request.getRemoteAddr();
        int    status       = response.getStatus();
        String body         = extractBody(request, method);
        persistAsync(userIdentity, method, path, ip, status, body);
    }

    @Async("emailTaskExecutor")
    protected void persistAsync(String userIdentity, String method, String path,
                                String ip, int httpStatus, String requestBody) {
        try {
            AuditLog entry = new AuditLog();
            entry.setLoggedAt(LocalDateTime.now());
            entry.setUserIdentity(userIdentity);
            entry.setHttpMethod(method);
            entry.setHttpPath(path);
            entry.setIpAddress(ip);
            entry.setHttpStatus(httpStatus);
            entry.setRequestBody(requestBody);
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Audit log fallito per {} {}: {}", method, path, e.getMessage());
        }
    }

    private String extractBody(HttpServletRequest request, String method) {
        if (!BODY_METHODS.contains(method)) {
            return null;
        }
        if (request instanceof ContentCachingRequestWrapper wrapped) {
            byte[] bytes = wrapped.getContentAsByteArray();
            if (bytes.length == 0) {
                return null;
            }
            String raw = new String(bytes, StandardCharsets.UTF_8);
            return raw.length() > MAX_BODY_LENGTH ? raw.substring(0, MAX_BODY_LENGTH) + "…" : raw;
        }
        return null;
    }

    private String extractUserIdentity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "anonymous";
    }
}
