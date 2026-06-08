package com.project.kore.controller;

import com.project.kore.dto.request.JobApplicationRequest;
import com.project.kore.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Endpoint REST per le candidature lavorative. Riceve il CV in PDF e delega l'invio via email.
 */
@RestController
@RequestMapping("/api/job-applications")
public class JobApplicationController {

    private final EmailService emailService;

    public JobApplicationController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Riceve una candidatura con CV allegato (PDF opzionale) e la inoltra via email.
     *
     * @param request dati della candidatura (parte "data" del multipart)
     * @param cv      CV in PDF (parte "cv" del multipart, opzionale)
     * @return 200 con un messaggio di conferma invio
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> submitApplication(
            @RequestPart("data") @Valid JobApplicationRequest request,
            @RequestPart(value = "cv", required = false) MultipartFile cv) {

        emailService.sendJobApplication(request, cv);

        return ResponseEntity.ok(Map.of("message", "Candidatura inviata con successo! Verrai contattato a breve."));
    }
}
