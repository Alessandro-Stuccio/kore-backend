package com.project.kore.facade;

import com.project.kore.dto.request.LoginRequest;
import com.project.kore.dto.request.RegisterRequest;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.dto.response.AuthResult;
import com.project.kore.exception.booking.ProfessionalSoldOutException;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import com.project.kore.util.BusinessConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;

/**
 * Autenticazione e gestione delle credenziali.
 */
@Validated
public interface AuthFacade {

    /**
     * Registra un nuovo utente.
     *
     * @param request dati di registrazione (anagrafica, credenziali, eventuale professionista assegnato)
     * @return i dati dell'utente appena registrato
     * @throws ResourceAlreadyExistsException se l'email è già in uso
     * @throws IllegalArgumentException       se un id di professionista assegnato non corrisponde al ruolo atteso
     * @throws ProfessionalSoldOutException   se il professionista scelto ha già raggiunto il massimo dei clienti
     */
    UserResponse registerUser(@Valid RegisterRequest request);

    /**
     * Verifica le credenziali e restituisce il token JWT.
     *
     * @param request email e password
     * @return l'esito con il token JWT e i dati utente
     * @throws BadCredentialsException se le credenziali non sono valide
     */
    AuthResult login(@Valid LoginRequest request);

    /**
     * Avvia il recupero password inviando l'email con il link di reset.
     *
     * @param email email dell'account per cui avviare il reset
     */
    void forgotPassword(@NotBlank(message = "L'email è obbligatoria")
                        @Email(message = "Formato email non valido") String email);

    /**
     * Imposta la nuova password verificando il token di reset ricevuto via email.
     *
     * @param token       token di reset ricevuto via email
     * @param newPassword nuova password in chiaro
     */
    void resetPassword(@NotBlank(message = "Il token è obbligatorio") String token,
                       @NotBlank(message = "La nuova password è obbligatoria")
                       @Size(min = BusinessConstants.MIN_PASSWORD_LENGTH, max = BusinessConstants.MAX_PASSWORD_LENGTH,
                               message = "La password deve avere tra {min} e {max} caratteri") String newPassword);
}
