package com.project.kore.controller;

import com.project.kore.dto.request.ProfileUpdateRequest;
import com.project.kore.dto.response.ClientBasicInfoResponse;
import com.project.kore.dto.response.ClientDashboardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.project.kore.facade.UserFacade;
import com.project.kore.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint REST per il profilo utente. Gestisce i dati anagrafici e il recupero della dashboard cliente.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserFacade userFacade;

    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    /**
     * Dashboard del cliente: profilo, abbonamento, professionisti assegnati e prossimi appuntamenti.
     *
     * @param user cliente autenticato
     * @return 200 con i dati della dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ClientDashboardResponse> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userFacade.getClientDashboard(user.getId()));
    }

    /**
     * Clienti assegnati al professionista autenticato.
     *
     * @param user professionista autenticato
     * @return 200 con i clienti a lui assegnati
     */
    @GetMapping("/clients")
    public ResponseEntity<List<ClientBasicInfoResponse>> getClientsForProfessional(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userFacade.getClientsForProfessional(user.getId()));
    }

    /**
     * Aggiorna dati anagrafici o password dell'utente autenticato.
     *
     * @param user    utente autenticato
     * @param request dati di profilo aggiornati
     * @return 200 senza corpo
     */
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal User user, @Valid @RequestBody ProfileUpdateRequest request) {
        userFacade.updateProfile(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    /**
     * Contatti dell'amministratore, usati dal frontend per la sezione "Contatta supporto".
     *
     * @return 200 con i dati di base dell'admin
     */
    @GetMapping("/admin")
    public ResponseEntity<ClientBasicInfoResponse> getAdmin() {
        return ResponseEntity.ok(userFacade.getAdmin());
    }

}
