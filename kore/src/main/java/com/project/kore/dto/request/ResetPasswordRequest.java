package com.project.kore.dto.request;

import com.project.kore.util.BusinessConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Imposta la nuova password usando il token ricevuto via email.
 *
 * @param token       token di reset ricevuto via email
 * @param newPassword nuova password in chiaro (almeno 6 caratteri)
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Il token è obbligatorio")
        String token,

        @NotBlank(message = "La nuova password è obbligatoria")
        @Size(min = BusinessConstants.MIN_PASSWORD_LENGTH, max = BusinessConstants.MAX_PASSWORD_LENGTH,
                message = "La password deve avere tra {min} e {max} caratteri")
        String newPassword) {
}
