package com.project.kore.facade;

import com.project.kore.dto.request.PlanRequest;
import com.project.kore.dto.request.ProfileUpdateRequest;
import com.project.kore.dto.response.ClientBasicInfoResponse;
import com.project.kore.dto.response.ClientDashboardResponse;
import com.project.kore.dto.response.ProfessionalSummaryResponse;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Profilo utente, dashboard del cliente e attivazione abbonamenti.
 */
@Validated
public interface UserFacade {

    /**
     * Dashboard del cliente: prenotazioni, crediti e abbonamento attivo.
     *
     * @param userId id del cliente
     * @return i dati della dashboard
     * @throws AccessDeniedException se l'utente non è un cliente
     */
    ClientDashboardResponse getClientDashboard(Long userId);

    /**
     * Dati di base dell'amministratore di sistema.
     *
     * @return i dati di base dell'admin
     */
    ClientBasicInfoResponse getAdmin();

    /**
     * Aggiorna il profilo dell'utente.
     *
     * @param userId  id dell'utente
     * @param request dati di profilo aggiornati
     */
    void updateProfile(Long userId, @Valid ProfileUpdateRequest request);

    /**
     * I clienti associati al professionista.
     *
     * @param professionalId id del professionista
     * @return i clienti a lui assegnati
     * @throws IllegalArgumentException se l'utente indicato non è un professionista
     */
    List<ClientBasicInfoResponse> getClientsForProfessional(Long professionalId);

    /**
     * Attiva l'abbonamento scelto dall'utente (piano e frequenza dalla request).
     *
     * @param request piano e frequenza di pagamento scelti
     * @param userId  id dell'utente che sottoscrive
     * @return i dati dell'abbonamento attivato
     * @throws AccessDeniedException          se l'utente non è un cliente
     * @throws ResourceAlreadyExistsException se l'utente ha già un abbonamento attivo
     */
    SubscriptionResponse activateSubscription(@Valid PlanRequest request, Long userId);

    /**
     * Stato dell'abbonamento attivo dell'utente.
     *
     * @param userId id dell'utente
     * @return i dati dell'abbonamento attivo
     */
    SubscriptionResponse getSubscriptionStatus(Long userId);

    /**
     * I professionisti disponibili per il ruolo indicato (es. PERSONAL_TRAINER, NUTRITIONIST).
     *
     * @param role il ruolo dei professionisti da cercare
     * @return i professionisti disponibili
     */
    List<ProfessionalSummaryResponse> findAvailableProfessionals(Role role);
}
