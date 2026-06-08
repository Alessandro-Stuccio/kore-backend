package com.project.kore.repository;

import com.project.kore.model.User;
import com.project.kore.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Accesso agli utenti: tiene conto del soft-delete e gestisce le assegnazioni a PT e nutrizionisti.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Utente attivo per email: lo usa l'autenticazione per caricare lo UserDetails.
    Optional<User> findByEmailAndDeletedFalse(String email);

    // In aggiornamento profilo: cerca un altro utente con la stessa email per controllarne l'unicità senza contare se stesso.
    Optional<User> findByEmailAndIdIsNotAndDeletedFalse(String email, Long id);

    // Tutti gli utenti non soft-deleted.
    List<User> findAllByDeletedFalse();

    // Filtra per ruolo solo tra gli utenti attivi.
    List<User> findByRoleAndDeletedFalse(Role role);

    // Conta i clienti attivi di un PT.
    long countByAssignedPTAndDeletedFalse(User pt);

    // Conta i clienti attivi di un nutrizionista.
    long countByAssignedNutritionistAndDeletedFalse(User nutritionist);

    // Clienti attivi di un PT.
    List<User> findByAssignedPTAndDeletedFalse(User pt);

    // Clienti attivi di un nutrizionista.
    List<User> findByAssignedNutritionistAndDeletedFalse(User nutritionist);

    // Stacca il PT da tutti i suoi clienti: da fare prima del soft-delete del PT per non lasciare riferimenti orfani.
    @Modifying
    @Query("UPDATE User u SET u.assignedPT = null WHERE u.assignedPT.id = :ptId")
    void clearAssignedPT(@Param("ptId") Long ptId);

    // Come clearAssignedPT ma per il nutrizionista.
    @Modifying
    @Query("UPDATE User u SET u.assignedNutritionist = null WHERE u.assignedNutritionist.id = :nutriId")
    void clearAssignedNutritionist(@Param("nutriId") Long nutriId);
}
