package com.project.kore.service;

import org.springframework.validation.annotation.Validated;

import com.project.kore.model.User;
import com.project.kore.model.Slot;

/** Genera i link per le videoconferenze delle prenotazioni. */
@Validated
public interface VideoConferenceService {

    /**
     * Crea la stanza Jitsi per l'incontro tra cliente e professionista su quello slot e ne ritorna l'URL.
     *
     * @param user         il cliente che partecipa
     * @param professional il professionista che partecipa
     * @param slot         lo slot prenotato per cui creare la stanza
     * @return l'URL della videoconferenza
     */
    String generateMeetingLink(User user, User professional, Slot slot);
}

