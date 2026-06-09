package com.project.kore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Una riga del registro di audit, scritta dall'AuditInterceptor a ogni richiesta HTTP:
 * tiene traccia di chi ha fatto cosa (utente, metodo, path, IP, status e body).
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    /** Email di chi ha fatto la richiesta, oppure "anonimo" se non autenticato. */
    @Column(name = "user_identity", length = 255)
    private String userIdentity;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "http_path", length = 500)
    private String httpPath;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "http_status")
    private Integer httpStatus;

    /** Body della richiesta, troncato a 4000 caratteri; null per le GET e simili. */
    @Column(name = "request_body", length = 4000)
    private String requestBody;

    public AuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }

    public String getUserIdentity() { return userIdentity; }
    public void setUserIdentity(String userIdentity) { this.userIdentity = userIdentity; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getHttpPath() { return httpPath; }
    public void setHttpPath(String httpPath) { this.httpPath = httpPath; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog that = (AuditLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuditLog{id=" + id + ", loggedAt=" + loggedAt + ", userIdentity='" + userIdentity + "', httpMethod='" + httpMethod + "', httpPath='" + httpPath + "', httpStatus=" + httpStatus + "}";
    }
}
