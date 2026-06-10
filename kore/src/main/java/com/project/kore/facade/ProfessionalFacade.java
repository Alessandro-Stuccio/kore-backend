package com.project.kore.facade;

import com.project.kore.dto.response.BookingResponse;
import com.project.kore.dto.response.SlotResponse;
import com.project.kore.dto.response.stats.ProfessionalStatsResponse;
import com.project.kore.model.User;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;

/**
 * Operazioni del professionista: slot, prenotazioni e statistiche.
 */
public interface ProfessionalFacade {

    /**
     * Gli slot ancora disponibili del professionista.
     *
     * @param professionalId id del professionista
     * @return gli slot liberi
     */
    List<SlotResponse> getAvailableSlots(Long professionalId);

    /**
     * Crea i nuovi slot indicati per il professionista.
     *
     * @param professionalId id del professionista
     * @param slots          gli slot da creare
     * @return gli slot creati
     * @throws AccessDeniedException se l'utente indicato non è un professionista
     */
    List<SlotResponse> createSlots(Long professionalId, List<SlotResponse> slots);

    /**
     * Elimina lo slot solo se il richiedente ne è il proprietario.
     *
     * @param slotId      id dello slot da eliminare
     * @param requesterId id di chi richiede l'eliminazione
     * @throws AccessDeniedException se il richiedente non è il proprietario dello slot
     * @throws IllegalStateException se lo slot è già prenotato
     */
    void deleteSlot(Long slotId, Long requesterId);

    /**
     * Genera gli slot dell'intervallo a partire dal calendario settimanale del professionista.
     *
     * @param professional il professionista
     * @param startDate    data di inizio dell'intervallo (inclusa)
     * @param endDate      data di fine dell'intervallo (inclusa)
     * @throws AccessDeniedException se l'utente indicato non è un professionista
     */
    void generateSlotsFromSchedule(User professional, LocalDate startDate, LocalDate endDate);

    /**
     * Le prossime prenotazioni del professionista.
     *
     * @param professionalId id del professionista
     * @return le prenotazioni future
     * @throws AccessDeniedException se l'utente indicato non è un professionista
     */
    List<BookingResponse> getUpcomingBookings(Long professionalId);

    /**
     * Statistiche del professionista: prenotazioni, recensioni, crediti e simili.
     *
     * @param professionalId id del professionista
     * @return le statistiche aggregate
     * @throws AccessDeniedException se l'utente indicato non è un professionista
     */
    ProfessionalStatsResponse getProfessionalStats(Long professionalId);
}
