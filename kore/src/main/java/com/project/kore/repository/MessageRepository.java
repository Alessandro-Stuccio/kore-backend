package com.project.kore.repository;

import com.project.kore.enums.MessageStatus;
import com.project.kore.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Gestisce i messaggi delle chat: lettura paginata, conteggio non letti e cambi di stato in bulk.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Messaggi di una chat, paginati e dal più recente.
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timeStamp DESC")
    List<Message> findMessagesByChatId(@Param("chatId") Long chatId, Pageable pageable);

    // Ultimo messaggio della chat (null se vuota).
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timeStamp DESC LIMIT 1")
    Message findLastMessageByChatId(@Param("chatId") Long chatId);

    // Messaggi non letti di un utente in una chat: quelli ricevuti (non inviati da lui) con stato diverso da "letto".
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.status != :readStatus " +
            "AND ((m.chat.user1.id = :userId AND m.sentByUser1 = false) " +
            "OR (m.chat.user2.id = :userId AND m.sentByUser1 = true))")
    int countUnreadMessagesByChatIdAndUserId(@Param("chatId") Long chatId,
                                             @Param("userId") Long userId,
                                             @Param("readStatus") MessageStatus readStatus);

    // Totale messaggi non letti dell'utente su tutte le sue chat.
    @Query("SELECT COUNT(m) FROM Message m WHERE m.status != :readStatus " +
            "AND m.chat.id IN (SELECT c.id FROM Chat c WHERE c.user1.id = :userId OR c.user2.id = :userId) " +
            "AND ((m.chat.user1.id = :userId AND m.sentByUser1 = false) " +
            "OR (m.chat.user2.id = :userId AND m.sentByUser1 = true))")
    int countTotalUnreadMessagesByUserId(@Param("userId") Long userId,
                                         @Param("readStatus") MessageStatus readStatus);

    // Update in bulk: porta da SENT a DELIVERED i soli messaggi ricevuti dall'utente nella chat.
    @Modifying
    @Query("UPDATE Message m SET m.status = :delivered " +
           "WHERE m.chat.id = :chatId AND m.status = :sent " +
           "AND ((m.chat.user1.id = :userId AND m.sentByUser1 = false) " +
           "OR (m.chat.user2.id = :userId AND m.sentByUser1 = true))")
    void markMessagesAsDelivered(@Param("chatId") Long chatId,
                                  @Param("userId") Long userId,
                                  @Param("sent") MessageStatus sent,
                                  @Param("delivered") MessageStatus delivered);

    // Update in bulk: segna come READ tutti i messaggi ricevuti dall'utente nella chat non ancora letti.
    @Modifying
    @Query("UPDATE Message m SET m.status = :read " +
           "WHERE m.chat.id = :chatId AND m.status != :read " +
           "AND ((m.chat.user1.id = :userId AND m.sentByUser1 = false) " +
           "OR (m.chat.user2.id = :userId AND m.sentByUser1 = true))")
    void markMessagesAsRead(@Param("chatId") Long chatId,
                             @Param("userId") Long userId,
                             @Param("read") MessageStatus read);
}
