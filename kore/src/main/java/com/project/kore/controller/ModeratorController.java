package com.project.kore.controller;

import com.project.kore.dto.request.ModeratorUserUpdateRequest;
import com.project.kore.dto.request.UpdateCreditsRequest;
import com.project.kore.dto.request.UserCreateRequestDTO;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.facade.ModeratorFacade;
import com.project.kore.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Operazioni del moderatore. /api/moderator (e /api/admin), richiede ruolo MODERATOR. */
@RestController
@RequestMapping(value = {"/api/moderator", "/api/admin"})
public class ModeratorController {

    private final ModeratorFacade moderatorFacade;

    public ModeratorController(ModeratorFacade moderatorFacade) {
        this.moderatorFacade = moderatorFacade;
    }

    /**
     * Utenti che il moderatore autenticato può gestire.
     *
     * @param user moderatore autenticato
     * @return 200 con gli utenti gestibili in base al suo ruolo
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getManageableUsers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(moderatorFacade.getManageableUsers(user));
    }

    /**
     * Tutti gli abbonamenti del sistema.
     *
     * @return 200 con l'elenco completo degli abbonamenti
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(moderatorFacade.getAllSubscriptions());
    }

    /**
     * Contatti con cui il moderatore può aprire una chat.
     *
     * @return 200 con i contatti disponibili per la chat
     */
    @GetMapping("/chat-contacts")
    public ResponseEntity<List<UserResponse>> getChatContacts() {
        return ResponseEntity.ok(moderatorFacade.getChatContacts());
    }

    /**
     * Crea un nuovo utente.
     *
     * @param body dati del nuovo utente
     * @param user moderatore autenticato che effettua la creazione
     * @return 200 con i dati dell'utente creato
     */
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequestDTO body, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(moderatorFacade.createUser(body, user));
    }

    /**
     * Aggiorna i dati di un utente esistente.
     *
     * @param id   id dell'utente da aggiornare
     * @param body dati aggiornati
     * @param user moderatore autenticato che effettua l'aggiornamento
     * @return 200 con i dati dell'utente aggiornato
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody ModeratorUserUpdateRequest body,@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(moderatorFacade.updateUser(id, body,user));
    }

    /**
     * Soft delete: marca l'utente come deleted. Non potrà più autenticarsi ma i dati restano in DB.
     *
     * @param id   id dell'utente da eliminare
     * @param user moderatore autenticato che effettua l'eliminazione
     * @return 200 con un messaggio di conferma
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User user) {
        moderatorFacade.deleteUser(id, user);
        return ResponseEntity.ok(Map.of("message", "Utente disabilitato"));
    }

    /**
     * Ritocca a mano i crediti di un abbonamento (PT e nutrizionista).
     *
     * @param id   id dell'abbonamento
     * @param body nuovi valori di crediti PT e nutrizionista
     * @return 200 con i dati dell'abbonamento aggiornato
     */
    @PutMapping("/subscriptions/{id}/credits")
    public ResponseEntity<SubscriptionResponse> updateSubscriptionCredits(@PathVariable Long id,
            @Valid @RequestBody UpdateCreditsRequest body) {
        return ResponseEntity.ok(moderatorFacade.updateSubscriptionCredits(
                id, body.creditsPT(), body.creditsNutri()));
    }

}
