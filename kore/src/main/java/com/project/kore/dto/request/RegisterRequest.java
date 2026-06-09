package com.project.kore.dto.request;

import com.project.kore.enums.PaymentFrequency;
import com.project.kore.util.BusinessConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registrazione di un nuovo cliente: dati anagrafici, credenziali e scelta di PT e nutrizionista (obbligatori).
 * Piano e frequenza di pagamento sono facoltativi e possono essere definiti in seguito.
 *
 * @param firstName               nome
 * @param lastName                cognome
 * @param email                   email (fa da username)
 * @param password                password in chiaro (6-100 caratteri)
 * @param selectedPtId            id del personal trainer scelto
 * @param selectedNutritionistId  id del nutrizionista scelto
 * @param profilePicture          URL dell'immagine di profilo (opzionale)
 * @param selectedPlanId          id del piano scelto (opzionale)
 * @param paymentFrequency        frequenza di pagamento scelta (opzionale)
 */
public record RegisterRequest(
        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 100, message = "Il nome non può superare 100 caratteri")
        String firstName,

        @NotBlank(message = "Il cognome è obbligatorio")
        @Size(max = 100, message = "Il cognome non può superare 100 caratteri")
        String lastName,

        @NotBlank(message = "L'email è obbligatoria")
        @Email(message = "Email non valida")
        String email,

        @NotBlank
        @Size(min = BusinessConstants.MIN_PASSWORD_LENGTH, max = BusinessConstants.MAX_PASSWORD_LENGTH,
                message = "La password deve avere tra {min} e {max} caratteri")
        String password,

        @NotNull(message = "È necessario selezionare un Personal Trainer")
        Long selectedPtId,

        @NotNull(message = "È necessario selezionare un Nutrizionista")
        Long selectedNutritionistId,

        @Size(max = 500, message = "URL immagine troppo lungo")
        String profilePicture,

        Long selectedPlanId,

        PaymentFrequency paymentFrequency) {
}
