package com.project.kore.repository;

import com.project.kore.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Gestisce le chat tra coppie di utenti.
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    // Cerca la chat tra due utenti senza importare in che ordine sono salvati come user1/user2.
    @Query("SELECT c FROM Chat c WHERE (c.user1.id = :userId1 AND c.user2.id = :userId2) OR (c.user1.id = :userId2 AND c.user2.id = :userId1)")
    Optional<Chat> findChatBetweenUsers(@Param("userId1") long userId1, @Param("userId2") long userId2);

    // Tutte le chat dell'utente, sia come user1 che come user2.
    @Query("SELECT c FROM Chat c WHERE c.user1.id = :userId OR c.user2.id = :userId")
    List<Chat> findAllChatsByUserId(@Param("userId") long userId);

    // Quante chat aperte ha un moderatore: serve a bilanciare il carico tra i moderatori.
    @Query("""
            SELECT COUNT(c) FROM Chat c
            WHERE (c.user1.id = :moderatorId OR c.user2.id = :moderatorId)
            AND c.status = com.project.kore.enums.ChatStatus.OPEN
            """)
    long countOpenChatsByModerator(@Param("moderatorId") long moderatorId);

}

