package com.project.kore.service.impl;

import com.project.kore.model.Slot;
import com.project.kore.model.User;
import com.project.kore.service.VideoConferenceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Genera link Jitsi Meet pubblici (nessuna autenticazione lato Jitsi) per gli incontri. */
@Service
public class JitsiVideoConferenceServiceImpl implements VideoConferenceService {

    @Value("${jitsi.base-url:https://meet.jit.si/Kore_Consulto_}")
    private String jitsiBaseUrl;

    // Nome stanza: prefisso configurato + id cliente + id professionista + UUID breve per unicità.
    @Override
    public String generateMeetingLink(User user, User professional, Slot slot) {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s%d_%d_%s", jitsiBaseUrl, user.getId(), professional.getId(), uniqueId);
    }
}
