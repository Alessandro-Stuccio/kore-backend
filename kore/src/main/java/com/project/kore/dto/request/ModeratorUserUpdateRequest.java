package com.project.kore.dto.request;

import com.project.kore.util.BusinessConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Modifica di un utente fatta dal moderatore. Ogni campo è opzionale: valgono solo quelli valorizzati.
 *
 * @param email     nuova email (opzionale)
 * @param firstName nuovo nome (opzionale)
 * @param lastName  nuovo cognome (opzionale)
 * @param password  nuova password (opzionale)
 */
public record ModeratorUserUpdateRequest(

        @Email(message = "Il formato dell'email non è valido")
        String email,

        @Size(min = 1, max = 100, message = "Il nome deve essere tra 1 e 100 caratteri")
        String firstName,

        @Size(min = 1, max = 100, message = "Il cognome deve essere tra 1 e 100 caratteri")
        String lastName,

        @Size(min = BusinessConstants.MIN_PASSWORD_LENGTH, max = BusinessConstants.MAX_PASSWORD_LENGTH,
                message = "La password deve avere tra {min} e {max} caratteri")
        String password
) {}
