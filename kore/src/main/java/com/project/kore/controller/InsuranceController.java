package com.project.kore.controller;

import com.project.kore.dto.request.UpdateNotesRequest;
import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.DocumentUploadResponse;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.facade.InsuranceFacade;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Area dell'Insurance Manager: gestione polizze e consultazione abbonamenti. Ruolo INSURANCE_MANAGER. */
@RestController
@RequestMapping("/api/insurance")
public class InsuranceController {

    private final InsuranceFacade insuranceFacade;

    public InsuranceController(InsuranceFacade insuranceFacade) {
        this.insuranceFacade = insuranceFacade;
    }

    /**
     * Tutti gli abbonamenti del sistema, attivi e scaduti.
     *
     * @return 200 con l'elenco completo degli abbonamenti
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions() {
        return ResponseEntity.ok(insuranceFacade.getAllSubscriptions());
    }

    /**
     * Admin e moderatori con cui l'insurance manager può aprire una chat.
     *
     * @return 200 con i contatti disponibili per la chat
     */
    @GetMapping("/chat-contacts")
    public ResponseEntity<List<UserResponse>> getChatContacts() {
        return ResponseEntity.ok(insuranceFacade.getChatContacts());
    }

    /**
     * Tutti i clienti registrati.
     *
     * @return 200 con l'elenco dei clienti
     */
    @GetMapping("/clients")
    public ResponseEntity<List<UserResponse>> getClients() {
        return ResponseEntity.ok(insuranceFacade.getAllClients());
    }

    /**
     * Carica una polizza e la collega al cliente indicato.
     *
     * @param clientId id del cliente destinatario
     * @param file     file della polizza
     * @param caller   insurance manager autenticato
     * @return 200 con i dati del documento caricato
     */
    @PostMapping("/clients/{clientId}/policy")
    public ResponseEntity<DocumentUploadResponse> uploadPolicy(
            @PathVariable Long clientId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User caller) {
        return ResponseEntity.ok(insuranceFacade.uploadPolicy(file, clientId, caller.getId()));
    }

    /**
     * Tutte le polizze di un cliente.
     *
     * @param clientId id del cliente
     * @return 200 con le polizze del cliente
     */
    @GetMapping("/clients/{clientId}/policies")
    public ResponseEntity<List<DocumentResponse>> getClientPolicies(@PathVariable Long clientId) {
        return ResponseEntity.ok(insuranceFacade.getClientPolicies(clientId));
    }

    /**
     * Scarica il file di una polizza, servito inline col content-type corretto.
     *
     * @param id id della polizza
     * @return 200 con il contenuto del file e l'header Content-Disposition inline
     */
    @GetMapping("/policies/{id}/download")
    public ResponseEntity<byte[]> downloadPolicy(@PathVariable Long id) {
        Document doc = insuranceFacade.getDocumentById(id);
        byte[] data = insuranceFacade.downloadPolicy(id);
        String contentType = doc.getContentType() != null ? doc.getContentType() : "application/octet-stream";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    /**
     * Elimina una polizza, sia il record sia il file su disco.
     *
     * @param id id della polizza
     * @return 204 senza corpo
     */
    @DeleteMapping("/policies/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        insuranceFacade.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Aggiorna le note testuali di una polizza.
     *
     * @param id   id della polizza
     * @param body richiesta con il nuovo testo delle note
     * @return 200 con i dati aggiornati delle note
     */
    @PutMapping("/policies/{id}/notes")
    public ResponseEntity<UpdatedNotesResponse> updatePolicyNotes(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotesRequest body) {
        return ResponseEntity.ok(insuranceFacade.updatePolicyNotes(id, body.notes()));
    }
}
