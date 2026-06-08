package com.project.kore.repository;

import com.project.kore.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Accesso ai log di audit: solo le operazioni CRUD di base, nessuna query custom.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
