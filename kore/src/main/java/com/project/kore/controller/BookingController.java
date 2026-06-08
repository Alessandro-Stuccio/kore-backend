package com.project.kore.controller;

import com.project.kore.dto.request.BookingRequest;
import com.project.kore.dto.response.BookingResponse;
import com.project.kore.facade.BookingFacade;
import com.project.kore.model.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Prenotazioni e cancellazioni degli slot. /api/bookings, richiede autenticazione. */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingFacade bookingFacade;

    public BookingController(BookingFacade bookingFacade) {
        this.bookingFacade = bookingFacade;
    }

    /**
     * Prenota uno slot libero: scala un credito dall'abbonamento attivo e, dietro le quinte,
     * usa un lock sullo slot per evitare che due utenti lo prenotino in contemporanea.
     *
     * @param request dati della prenotazione (id dello slot)
     * @param user    utente autenticato che prenota
     * @return 200 con i dati della prenotazione confermata
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request,
                                                          @AuthenticationPrincipal User user) {
        log.info("Richiesta prenotazione slot {} da utente {}", request.slotId(), user.getId());
        BookingResponse response = bookingFacade.createBooking(request, user.getId());
        log.info("Prenotazione confermata: id={} utente={}", response.getId(), user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Annulla una prenotazione dell'utente. Il credito viene riaccreditato solo se si cancella
     * con più di 24 ore di anticipo rispetto allo slot.
     *
     * @param id   id della prenotazione (slot) da annullare
     * @param user utente autenticato proprietario della prenotazione
     * @return 200 con un messaggio di conferma annullamento
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable Long id,
                                                              @AuthenticationPrincipal User user) {
        log.info("Annullamento prenotazione id={} richiesto da utente {}", id, user.getId());
        bookingFacade.cancelBooking(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Prenotazione annullata con successo. Lo slot è stato liberato e il credito riaccreditato."));
    }

}
