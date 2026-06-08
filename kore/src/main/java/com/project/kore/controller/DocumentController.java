package com.project.kore.controller;

import com.project.kore.dto.request.UpdateNotesRequest;
import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.DocumentUploadResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.facade.DocumentFacade;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Gestione documenti. L'upload viaggia come form-data con il file binario in MultipartFile. */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentFacade documentFacade;

    public DocumentController(DocumentFacade documentFacade) {
        this.documentFacade = documentFacade;
    }

    /**
     * Carica un documento validando il ruolo dell'uploader rispetto al tipo di file.
     *
     * @param file     file binario da caricare
     * @param clientId id del cliente proprietario del documento
     * @param type     tipo di documento richiesto
     * @param uploader utente autenticato che effettua il caricamento
     * @return 200 con i dati del documento caricato
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clientId") Long clientId,
            @RequestParam("type") String type,
            @AuthenticationPrincipal User uploader) {
        return ResponseEntity.ok(documentFacade.uploadDocumentWithValidation(file, clientId, uploader.getId(), type));
    }

    /**
     * Scarica il contenuto binario di un documento per la visualizzazione inline nel browser.
     *
     * @param id     id del documento
     * @param caller utente autenticato che richiede il download
     * @return 200 con il contenuto del file e l'header Content-Disposition inline
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id,
                                               @AuthenticationPrincipal User caller) {
        Document doc = documentFacade.getDocumentById(id);
        byte[] data = documentFacade.downloadDocumentSecure(id, caller.getId());
        String contentType = doc.getContentType() != null ? doc.getContentType() : "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    /**
     * Restituisce tutti i documenti di un utente (qualsiasi tipo).
     *
     * @param userId id dell'utente di cui leggere i documenti
     * @param caller utente autenticato che effettua la richiesta
     * @return 200 con i documenti visibili al chiamante
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentResponse>> getUserDocuments(@PathVariable Long userId,
                                                                    @AuthenticationPrincipal User caller) {
        return ResponseEntity.ok(documentFacade.getUserDocumentsDtoSecure(userId, caller.getId()));
    }

    /**
     * Restituisce i documenti di un utente filtrati per tipologia.
     *
     * @param userId id dell'utente di cui leggere i documenti
     * @param type   tipo di documento da filtrare
     * @param caller utente autenticato che effettua la richiesta
     * @return 200 con i documenti del tipo indicato visibili al chiamante
     */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<DocumentResponse>> getUserDocumentsByType(
            @PathVariable Long userId, @PathVariable String type,
            @AuthenticationPrincipal User caller) {
        return ResponseEntity.ok(documentFacade.getUserDocumentsByTypeDtoSecure(userId, type, caller.getId()));
    }

    /**
     * Elimina un documento dal database e dal filesystem.
     *
     * @param id     id del documento
     * @param caller utente autenticato che richiede l'eliminazione
     * @return 204 senza corpo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id,
                                               @AuthenticationPrincipal User caller) {
        documentFacade.deleteDocument(id, caller.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Aggiorna le note testuali associate a un documento. Solo proprietario, uploader o admin/moderatore.
     *
     * @param id     id del documento
     * @param body   richiesta con il nuovo testo delle note
     * @param caller utente autenticato che effettua la modifica
     * @return 200 con i dati aggiornati delle note
     */
    @PutMapping("/{id}/notes")
    public ResponseEntity<UpdatedNotesResponse> updateNotes(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotesRequest body,
            @AuthenticationPrincipal User caller) {
        return ResponseEntity.ok(documentFacade.updateNotes(id, body.notes(), caller.getId()));
    }
}
