package com.project.kore.service;

import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.User;
import com.project.kore.enums.Role;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** Gestione degli utenti della piattaforma. */
@Validated
public interface UserService {

    /**
     * Recupera un utente dal suo id.
     *
     * @param id id dell'utente
     * @return l'utente trovato
     * @throws CustomResourceNotFoundException se nessun utente ha quell'id
     */
    User getUserById(
            @NotNull(message = "l'id deve essere valorizzato")
            @Min(value = 1, message = "non esistono id negativi") Long id);

    /**
     * Cerca l'utente per email, che qui fa anche da username.
     *
     * @param email email dell'utente (esclude i soft-deleted)
     * @return l'utente con quella email
     * @throws CustomResourceNotFoundException se nessun utente attivo ha quella email
     */
    User getUserByEmail(@NotNull String email);

    /**
     * Verifica se esiste un utente attivo con quella email.
     *
     * @param email email da cercare
     * @return {@code true} se l'email è già in uso da un utente non eliminato
     */
    boolean existsByEmail(@NotNull String email);

    /**
     * Persiste l'utente (creazione o aggiornamento).
     *
     * @param user l'utente da salvare
     * @return l'utente salvato
     */
    User save(@NotNull User user);

    /**
     * Utenti attivi con il ruolo indicato.
     *
     * @param role il ruolo da filtrare
     * @return gli utenti non eliminati con quel ruolo
     */
    List<User> findByRole(@NotNull Role role);

    /**
     * Tutti gli utenti attivi.
     *
     * @return gli utenti non eliminati
     */
    List<User> findAll();

    /**
     * Quanti client sono assegnati al personal trainer.
     *
     * @param pt il personal trainer
     * @return il numero di client assegnati
     */
    long countByAssignedPT(@NotNull User pt);

    /**
     * Quanti client sono assegnati al nutrizionista.
     *
     * @param nutritionist il nutrizionista
     * @return il numero di client assegnati
     */
    long countByAssignedNutritionist(@NotNull User nutritionist);

    /**
     * Client assegnati al personal trainer.
     *
     * @param pt il personal trainer
     * @return i client assegnati
     */
    List<User> findByAssignedPT(@NotNull User pt);

    /**
     * Client assegnati al nutrizionista.
     *
     * @param nutritionist il nutrizionista
     * @return i client assegnati
     */
    List<User> findByAssignedNutritionist(@NotNull User nutritionist);

    /**
     * Email già in uso da un altro utente, escludendo se stessi: serve in fase di update profilo.
     *
     * @param email     email da verificare
     * @param excludeId id dell'utente da escludere dal controllo (tipicamente se stesso)
     * @return {@code true} se l'email appartiene a un altro utente
     */
    boolean existsUserByEmailExcluding(String email, Long excludeId);

    /**
     * Soft delete dell'utente: lo marca come eliminato e sgancia i client se era un professionista.
     *
     * @param id id dell'utente da eliminare
     * @throws CustomResourceNotFoundException se nessun utente ha quell'id
     */
    void deleteUser(Long id);

    /**
     * Applica l'hashing configurato a una password in chiaro.
     *
     * @param rawPassword password in chiaro
     * @return la password cifrata (BCrypt)
     */
    String encodePassword(@NotNull String rawPassword);
}
