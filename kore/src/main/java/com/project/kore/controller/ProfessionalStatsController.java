package com.project.kore.controller;

import com.project.kore.dto.response.BookingResponse;
import com.project.kore.dto.response.stats.ProfessionalStatsResponse;
import com.project.kore.facade.ProfessionalFacade;
import com.project.kore.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint REST per le metriche della dashboard del professionista autenticato.
 */
@RestController
@RequestMapping("/api/professional")
public class ProfessionalStatsController {

    private final ProfessionalFacade professionalFacade;

    public ProfessionalStatsController(ProfessionalFacade professionalFacade) {
        this.professionalFacade = professionalFacade;
    }

    /**
     * Restituisce tutte le statistiche aggregate per la dashboard del professionista autenticato.
     *
     * @param user professionista autenticato
     * @return 200 con le statistiche aggregate
     */
    @GetMapping("/stats")
    public ResponseEntity<ProfessionalStatsResponse> getStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(professionalFacade.getProfessionalStats(user.getId()));
    }

    /**
     * Restituisce gli appuntamenti futuri del professionista autenticato per il calendario.
     *
     * @param user professionista autenticato
     * @return 200 con le sue prenotazioni future
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getUpcomingBookings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(professionalFacade.getUpcomingBookings(user.getId()));
    }
}
