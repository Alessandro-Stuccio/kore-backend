package com.project.kore.repository;

import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

/**
 * Abbonamenti degli utenti, con focus sul recupero di quelli attivi.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Abbonamento attivo dell'utente: ne esiste al massimo uno con active = true.
    Optional<Subscription> findByUserAndActiveTrue(User user);

    // Come sopra ma con lock PESSIMISTIC_WRITE sulla riga: serve durante la deduzione dei crediti per evitare race condition.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subscription s WHERE s.user = :user AND s.active = true")
    Optional<Subscription> findByUserAndActiveTrueWithLock(@Param("user") User user);

    Optional<Subscription> findByUserIdAndActiveTrue(Long userId);

    // Tutti gli abbonamenti attivi: li usa lo scheduler mensile per rinnovare i crediti.
    List<Subscription> findByActiveTrue();

    // Vero se un piano ha abbonamenti collegati (attivi o storici): da controllare prima di eliminarlo.
    boolean existsByPlanId(Long planId);
}