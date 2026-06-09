package com.project.kore.facade;

import com.project.kore.dto.request.BookingRequest;
import com.project.kore.dto.response.BookingResponse;
import com.project.kore.exception.booking.BookingCancellationException;
import com.project.kore.exception.booking.SlotAlreadyBookedException;
import com.project.kore.exception.booking.SubscriptionExpiredException;
import com.project.kore.exception.common.BusinessLogicException;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

/**
 * Prenotazione e cancellazione di slot.
 */
@Validated
public interface BookingFacade {

    /**
     * Prenota uno slot disponibile per conto dell'utente: scala un credito dall'abbonamento attivo
     * e genera il link alla videoconferenza, proteggendosi dall'overbooking concorrente.
     *
     * @param request dati della prenotazione (id dello slot)
     * @param userId  id dell'utente che prenota
     * @return i dati della prenotazione confermata
     * @throws BusinessLogicException       se l'utente tenta di prenotare con se stesso
     * @throws SlotAlreadyBookedException    se lo slot è stato occupato nel frattempo
     * @throws SubscriptionExpiredException  se l'abbonamento è scaduto o privo di crediti
     * @throws IllegalStateException         se l'aggiornamento dei crediti fallisce per conflitto concorrente
     */
    BookingResponse createBooking(@Valid BookingRequest request, Long userId);

    /**
     * Annulla una prenotazione esistente dell'utente. Il credito viene riaccreditato solo se si
     * cancella con più di 24 ore di anticipo.
     *
     * @param bookingId id della prenotazione (slot) da annullare
     * @param userId    id dell'utente proprietario della prenotazione
     * @throws BookingCancellationException se la prenotazione non appartiene all'utente, non è in uno
     *                                      stato annullabile, o mancano meno di 24 ore all'appuntamento
     */
    void cancelBooking(Long bookingId, Long userId);
}
