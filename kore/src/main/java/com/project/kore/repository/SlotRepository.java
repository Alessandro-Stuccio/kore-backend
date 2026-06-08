package com.project.kore.repository;

import com.project.kore.model.Slot;
import com.project.kore.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Slot delle agende dei professionisti, con prenotazioni, disponibilità e promemoria.
 */
public interface SlotRepository extends JpaRepository<Slot, Long> {

    // Evita di generare slot duplicati per lo stesso professionista alla stessa ora.
    boolean existsByProfessionalAndStartTime(User professional, LocalDateTime startTime);

    // Carica lo slot con lock PESSIMISTIC_WRITE sulla riga: blocca la concorrenza durante la prenotazione ed evita il double-booking.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdWithLock(@Param("id") Long id);

    // Slot liberi di un professionista in un intervallo, ordinati per ora di inizio.
    @Query("SELECT s FROM Slot s WHERE s.professional.id = :profId " +
            "AND s.bookedBy IS NULL " +
            "AND s.startTime BETWEEN :start AND :end " +
            "ORDER BY s.startTime ASC")
    List<Slot> findAvailableSlots(@Param("profId") Long profId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Tutti gli slot liberi di un professionista, senza filtro sulla data.
    List<Slot> findByProfessionalAndBookedByIsNull(User professional);

    // Tutti gli slot di un professionista, liberi e prenotati.
    List<Slot> findByProfessional(User professional);

    // ---- Query ex-BookingRepository ----

    List<Slot> findByBookedBy(User bookedBy);

    // Prenotazioni future di un utente, dalla più vicina.
    @Query("SELECT s FROM Slot s WHERE s.bookedBy = :user AND s.startTime > :now ORDER BY s.startTime ASC")
    List<Slot> findFutureByBookedBy(@Param("user") User user, @Param("now") LocalDateTime now);

    // Slot prenotati oggi per un professionista, ordinati per ora di inizio.
    @Query("SELECT s FROM Slot s WHERE s.professional = :professional " +
           "AND s.startTime >= :dayStart AND s.startTime < :dayEnd " +
           "AND s.bookedBy IS NOT NULL " +
           "ORDER BY s.startTime ASC")
    List<Slot> findTodayByProfessional(@Param("professional") User professional,
                                        @Param("dayStart") LocalDateTime dayStart,
                                        @Param("dayEnd") LocalDateTime dayEnd);

    // Prenotazioni recenti di un utente (dalla data indicata), per il feed attività.
    @Query("SELECT s FROM Slot s WHERE s.bookedBy = :user AND s.bookedAt >= :since ORDER BY s.bookedAt DESC")
    List<Slot> findRecentByBookedBy(@Param("user") User user, @Param("since") LocalDateTime since);

    // Prenotazioni recenti gestite da un professionista, per il suo feed attività.
    @Query("SELECT s FROM Slot s WHERE s.professional = :professional AND s.bookedAt >= :since ORDER BY s.bookedAt DESC")
    List<Slot> findRecentByProfessional(@Param("professional") User professional, @Param("since") LocalDateTime since);

    // Vero se il cliente ha almeno una prenotazione col professionista: abilita la recensione.
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Slot s " +
           "WHERE s.bookedBy.id = :userId AND s.professional.id = :professionalId")
    boolean existsByBookedByIdAndProfessionalId(@Param("userId") Long userId,
                                                 @Param("professionalId") Long professionalId);

    // Slot confermati imminenti senza promemoria ancora inviato: li usa il BookingReminderScheduler.
    @Query("SELECT s FROM Slot s " +
           "WHERE s.status = com.project.kore.enums.BookingStatus.CONFIRMED " +
           "AND s.reminderSent = false " +
           "AND s.bookedBy IS NOT NULL " +
           "AND s.startTime >= :from " +
           "AND s.startTime <= :to")
    List<Slot> findUpcomingNeedingReminder(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    // Tutti gli slot effettivamente prenotati (cliente e data valorizzati), per le statistiche admin.
    @Query("SELECT s FROM Slot s WHERE s.bookedBy IS NOT NULL AND s.bookedAt IS NOT NULL")
    List<Slot> findAllBooked();
}
