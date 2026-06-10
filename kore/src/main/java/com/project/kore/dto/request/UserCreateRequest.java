package com.project.kore.dto.request;

import com.project.kore.util.BusinessConstants;
import jakarta.validation.constraints.*;

/**
 * Crea un nuovo utente da pannello moderatore/admin. PT, nutrizionista, piano e frequenza
 * di pagamento servono solo per i clienti.
 *
 * @param email                  email del nuovo utente (fa da username)
 * @param firstName              nome
 * @param lastName               cognome
 * @param password               password in chiaro (8-100 caratteri)
 * @param role                   ruolo da assegnare (nome dell'enum)
 * @param assignedPTId           id del personal trainer assegnato (solo clienti)
 * @param assignedNutritionistId id del nutrizionista assegnato (solo clienti)
 * @param planId                 id del piano (solo clienti)
 * @param paymentFrequency       frequenza di pagamento (solo clienti)
 */
public record UserCreateRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 50) String firstName,
        @NotBlank @Size(min = 2, max = 50) String lastName,
        @NotBlank @Size(min = BusinessConstants.MIN_PASSWORD_LENGTH, max = BusinessConstants.MAX_PASSWORD_LENGTH,
                message = "La password deve avere tra {min} e {max} caratteri") String password,
        @NotBlank String role,
        @Min(1) Long assignedPTId,
        @Min(1) Long assignedNutritionistId,
        @Min(1) Long planId,
        String paymentFrequency
) {}
