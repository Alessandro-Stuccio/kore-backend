package com.project.kore.controller;

import com.project.kore.dto.response.ProfessionalSummaryResponse;
import com.project.kore.dto.response.SlotResponse;
import com.project.kore.enums.Role;
import com.project.kore.facade.ProfessionalFacade;
import com.project.kore.facade.UserFacade;
import com.project.kore.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Ricerca dei professionisti e gestione dei loro slot. /api/professionals. */
@RestController
@RequestMapping("/api/professionals")
public class ProfessionalController {

    private final UserFacade userFacade;
    private final ProfessionalFacade professionalFacade;

    public ProfessionalController(UserFacade userFacade, ProfessionalFacade professionalFacade) {
        this.userFacade = userFacade;
        this.professionalFacade = professionalFacade;
    }

    /**
     * Professionisti disponibili filtrati per ruolo (PERSONAL_TRAINER o NUTRITIONIST).
     *
     * @param role ruolo dei professionisti da cercare
     * @return 200 con i professionisti disponibili
     */
    @GetMapping
    public ResponseEntity<List<ProfessionalSummaryResponse>> getProfessionals(@RequestParam Role role) {
        return ResponseEntity.ok(userFacade.findAvailableProfessionals(role));
    }

    /**
     * Slot liberi di un professionista, per il calendario di prenotazione.
     *
     * @param id id del professionista
     * @return 200 con i suoi slot liberi
     */
    @GetMapping("/{id}/slots")
    public ResponseEntity<List<SlotResponse>> getProfessionalSlots(@PathVariable Long id) {
        return ResponseEntity.ok(professionalFacade.getAvailableSlots(id));
    }

    /**
     * Aggiunge slot al calendario del professionista autenticato.
     *
     * @param user  professionista autenticato
     * @param slots gli slot da creare
     * @return 200 con gli slot creati
     */
    @PostMapping("/slots")
    public ResponseEntity<List<SlotResponse>> createSlots(@AuthenticationPrincipal User user,
                                                          @RequestBody List<SlotResponse> slots) {
        return ResponseEntity.ok(professionalFacade.createSlots(user.getId(), slots));
    }

    /**
     * Rimuove uno slot del professionista autenticato (deve esserne il proprietario).
     *
     * @param slotId id dello slot da rimuovere
     * @param user   professionista autenticato
     * @return 204 senza corpo
     */
    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId,
                                            @AuthenticationPrincipal User user) {
        professionalFacade.deleteSlot(slotId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
