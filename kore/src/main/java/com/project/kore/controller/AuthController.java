package com.project.kore.controller;

import com.project.kore.dto.request.ForgotPasswordRequest;
import com.project.kore.dto.request.LoginRequest;
import com.project.kore.dto.request.RegisterRequest;
import com.project.kore.dto.request.ResetPasswordRequest;
import com.project.kore.dto.response.AuthResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.facade.AuthFacade;
import com.project.kore.model.User;
import com.project.kore.dto.response.AuthResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Autenticazione e gestione credenziali. Endpoint pubblici sotto /api/auth, senza JWT. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    /**
     * Registra un nuovo utente; nasce sempre con ruolo CLIENT.
     *
     * @param request dati di registrazione validati
     * @return 200 con i dati dell'utente registrato
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registrazione nuovo utente: {}", request.email());
        UserResponse response = authFacade.registerUser(request);
        log.info("Utente registrato con successo: id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Login con email e password; restituisce token JWT e dati del profilo.
     *
     * @param request credenziali di accesso
     * @return 200 con token JWT e dati del profilo
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentativo di login: {}", request.email());
        AuthResult result = authFacade.login(request);
        User u = result.getUser();
        return ResponseEntity.ok(AuthResponse.builder()
                .token(result.getToken())
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .role(u.getRole())
                .profilePicture(u.getProfilePicture())
                .build());
    }

    /**
     * Manda via email il link di reset password. Il link scade dopo 30 minuti.
     *
     * @param request email dell'account per cui avviare il reset
     * @return 200 con un messaggio di conferma invio
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authFacade.forgotPassword(request.email());
        return ResponseEntity.ok(Map.of("message", "Link di reset inviato. Controlla la tua casella di posta."));
    }

    /**
     * Reimposta la password usando il token ricevuto via email.
     *
     * @param request token di reset e nuova password
     * @return 200 con un messaggio di conferma
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authFacade.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password reimpostata con successo."));
    }

    /**
     * Health check: serve al frontend per capire se il backend è raggiungibile.
     *
     * @return 200 con lo stato del backend
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "UP", "message", "Il Backend è online e funziona correttamente"));
    }
}
