package com.project.kore.service;

import com.project.kore.exception.booking.SlotAlreadyBookedException;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Slot;
import com.project.kore.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestione degli slot di disponibilità dei professionisti e delle relative prenotazioni.
 * La prenotazione passa per un lock per-slot (ReentrantLock) per evitare l'overbooking.
 */
@Validated
public interface SlotService {

    /**
     * Persiste in blocco gli slot generati per un professionista.
     *
     * @param slots gli slot da salvare; non nullo e non vuoto
     * @return gli slot salvati, con id valorizzato
     */
    List<Slot> createSlots(@NotNull @NotEmpty List<Slot> slots);

    /**
     * Slot ancora liberi del professionista.
     *
     * @param professional il professionista di cui cercare la disponibilità
     * @return gli slot non ancora prenotati
     */
    List<Slot> getAvailableSlots(@NotNull User professional);

    /**
     * Recupera un singolo slot dal suo id.
     *
     * @param slotId id dello slot
     * @return lo slot trovato
     * @throws CustomResourceNotFoundException se lo slot non esiste
     */
    Slot getSlot(@NotNull @Min(1) Long slotId);

    /**
     * Prenota lo slot per l'utente acquisendo il lock per-slot; ritorna lo slot aggiornato.
     *
     * @param slotId      id dello slot da prenotare
     * @param user        utente che prenota
     * @param meetingLink link alla videoconferenza da associare allo slot
     * @return lo slot ora prenotato (status CONFIRMED, con link e timestamp)
     * @throws CustomResourceNotFoundException se lo slot non esiste
     * @throws SlotAlreadyBookedException       se nel frattempo lo slot è stato occupato
     */
    Slot saveBooking(@NotNull @Min(1) Long slotId, @NotNull User user, @NotBlank String meetingLink);

    /**
     * Elimina definitivamente lo slot.
     *
     * @param slotId id dello slot da eliminare
     * @throws CustomResourceNotFoundException se lo slot non esiste
     */
    void deleteSlot(@NotNull @Min(1) Long slotId);

    /**
     * Annulla la prenotazione dell'utente su quello slot.
     *
     * @param slotId id dello slot prenotato
     * @param userId id dell'utente che aveva prenotato
     * @throws CustomResourceNotFoundException se la prenotazione non esiste
     */
    void cancelBooking(@NotNull @Min(1) Long slotId, @NotNull @Min(1) Long userId);

    /**
     * Dice se il professionista ha già uno slot a quell'orario (per evitare duplicati).
     *
     * @param professional il professionista
     * @param startTime    l'orario di inizio da verificare
     * @return {@code true} se esiste già uno slot a quell'orario
     */
    boolean slotExists(@NotNull User professional, @NotNull LocalDateTime startTime);

    /**
     * Slot prenotati dall'utente a partire dalla data indicata.
     *
     * @param user  utente che ha prenotato
     * @param since data/ora minima delle prenotazioni da considerare
     * @return gli slot prenotati dall'utente non antecedenti a {@code since}
     */
    List<Slot> findRecentByUser(@NotNull User user, @NotNull LocalDateTime since);

    /**
     * Slot prenotati presso il professionista a partire dalla data indicata.
     *
     * @param professional il professionista
     * @param since        data/ora minima da considerare
     * @return gli slot prenotati presso il professionista non antecedenti a {@code since}
     */
    List<Slot> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);

    /**
     * Tutte le prenotazioni del professionista.
     *
     * @param professional il professionista
     * @return gli slot del professionista che risultano prenotati
     */
    List<Slot> findBookingsByProfessional(@NotNull User professional);

    /**
     * Prenotazioni future dell'utente da una certa data/ora in poi.
     *
     * @param user utente che ha prenotato
     * @param from data/ora a partire dalla quale considerare le prenotazioni
     * @return gli slot futuri prenotati dall'utente
     */
    List<Slot> findFutureByUser(@NotNull User user, @NotNull LocalDateTime from);

    /**
     * Tutti gli slot risultanti prenotati nel sistema.
     *
     * @return l'elenco completo degli slot prenotati
     */
    List<Slot> getAllBookedSlots();

    /**
     * Scrive sull'audit log la creazione di una prenotazione.
     *
     * @param slot lo slot appena prenotato
     */
    void logBookingCreated(@NotNull Slot slot);

    /**
     * Slot prenotati del professionista nella giornata delimitata da dayStart/dayEnd.
     *
     * @param professional il professionista
     * @param dayStart     inizio della giornata (incluso)
     * @param dayEnd       fine della giornata (escluso)
     * @return gli slot prenotati del professionista in quella giornata
     */
    List<Slot> findTodayByProfessional(@NotNull User professional,
                                       @NotNull LocalDateTime dayStart,
                                       @NotNull LocalDateTime dayEnd);

    /**
     * Dice se cliente e professionista hanno almeno una prenotazione in comune (gate per le recensioni).
     *
     * @param clientId       id del cliente
     * @param professionalId id del professionista
     * @return {@code true} se esiste almeno una prenotazione tra i due
     */
    boolean hasBookingBetween(@NotNull @Min(1) Long clientId, @NotNull @Min(1) Long professionalId);
}
