package com.project.kore.facade;

import com.project.kore.dto.request.ModeratorUserUpdateRequest;
import com.project.kore.dto.request.UserCreateRequest;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import com.project.kore.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Operazioni del Moderatore: gestione utenti, abbonamenti e chat.
 */
@Validated
public interface ModeratorFacade {

    /**
     * Gli utenti che questo moderatore ha il diritto di gestire.
     *
     * @param user il moderatore
     * @return gli utenti gestibili in base al suo ruolo
     */
    List<UserResponse> getManageableUsers(User user);

    /**
     * Tutti gli abbonamenti del sistema.
     *
     * @return l'elenco completo degli abbonamenti
     */
    List<SubscriptionResponse> getAllSubscriptions();

    /**
     * Utenti che il moderatore può contattare in chat.
     *
     * @return i contatti disponibili per la chat
     */
    List<UserResponse> getChatContacts();

    /**
     * Crea un utente per conto del moderatore.
     *
     * @param request dati del nuovo utente
     * @param user    il moderatore che effettua la creazione
     * @return i dati dell'utente creato
     * @throws AccessDeniedException          se il moderatore non può gestire quel ruolo
     * @throws ResourceAlreadyExistsException se l'email è già in uso
     * @throws IllegalArgumentException       se un dato della richiesta non è valido (es. frequenza di pagamento)
     */
    UserResponse createUser(@Valid UserCreateRequest request, User user);

    /**
     * Aggiorna l'utente indicato, verificando i permessi del moderatore.
     *
     * @param id      id dell'utente da aggiornare
     * @param request dati aggiornati
     * @param user    il moderatore che effettua l'aggiornamento
     * @return i dati dell'utente aggiornato
     * @throws AccessDeniedException          se il moderatore non può gestire quell'utente o l'assegnazione di ruolo non è coerente
     * @throws ResourceAlreadyExistsException se la nuova email è già in uso
     * @throws IllegalArgumentException       se un dato della richiesta non è valido
     */
    UserResponse updateUser(Long id, @Valid ModeratorUserUpdateRequest request, User user);

    /**
     * Elimina l'utente indicato, verificando i permessi del moderatore.
     *
     * @param id   id dell'utente da eliminare
     * @param user il moderatore che effettua l'eliminazione
     * @throws AccessDeniedException se il moderatore non può gestire quell'utente
     */
    void deleteUser(Long id, User user);

    /**
     * Imposta i crediti mensili dell'abbonamento per personal trainer e nutrizionista.
     *
     * @param id    id dell'abbonamento
     * @param pt    crediti per il personal trainer
     * @param nutri crediti per il nutrizionista
     * @return i dati dell'abbonamento aggiornato
     * @throws IllegalArgumentException se uno dei valori di crediti è negativo
     */
    SubscriptionResponse updateSubscriptionCredits(Long id, @Min(0) int pt, @Min(0) int nutri);
}
