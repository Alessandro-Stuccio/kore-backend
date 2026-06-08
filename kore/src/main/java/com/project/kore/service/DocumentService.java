package com.project.kore.service;

import com.project.kore.enums.DocumentType;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/** Gestione dei documenti caricati dagli utenti. */
@Validated
public interface DocumentService {

    /**
     * Recupera un documento dal suo id.
     *
     * @param documentId id del documento
     * @return il documento trovato
     * @throws CustomResourceNotFoundException se il documento non esiste
     */
    Document getDocumentById(@NotNull @Min(1) Long documentId);

    /**
     * Elimina un documento (record in DB).
     *
     * @param documentId id del documento
     * @throws CustomResourceNotFoundException se il documento non esiste
     */
    void deleteDocument(@NotNull @Min(1) Long documentId);

    /**
     * Aggiorna le note testuali del documento.
     *
     * @param documentId id del documento
     * @param notes      nuovo testo delle note
     * @return il documento aggiornato
     * @throws CustomResourceNotFoundException se il documento non esiste
     */
    Document updateNotes(@NotNull @Min(1) Long documentId, @NotBlank String notes);

    /**
     * Persiste il documento (creazione o aggiornamento).
     *
     * @param document il documento da salvare
     * @return il documento salvato
     */
    Document saveDocument(@NotNull Document document);

    /**
     * Documenti dell'utente proprietario, dal più recente.
     *
     * @param owner il proprietario dei documenti
     * @return i documenti dell'utente
     */
    List<Document> getUserDocuments(@NotNull User owner);

    /**
     * Documenti dell'utente filtrati per tipo.
     *
     * @param owner   il proprietario dei documenti
     * @param docType tipo di documento (nome dell'enum {@link DocumentType})
     * @return i documenti dell'utente di quel tipo
     */
    List<Document> getUserDocumentsByType(@NotNull User owner, @NotBlank String docType);

    /**
     * Crea il record del documento partendo dai metadati e dal file già archiviato su disco.
     *
     * @param filePath     percorso del file su disco
     * @param originalName nome originale del file
     * @param contentType  MIME type del file (può essere nullo)
     * @param docType      tipo di documento (nome dell'enum {@link DocumentType})
     * @param client       utente proprietario del documento
     * @param uploader     utente che ha effettuato il caricamento
     * @return il documento creato
     */
    Document uploadDocument(@NotBlank String filePath,
                            @NotBlank String originalName,
                            String contentType,
                            @NotBlank String docType,
                            @NotNull User client,
                            @NotNull User uploader);

    /**
     * Documenti di un utente caricati a partire dalla data indicata.
     *
     * @param owner il proprietario dei documenti
     * @param since data/ora minima di caricamento (non futura)
     * @return i documenti caricati non prima di {@code since}
     */
    List<Document> findRecentByOwner(@NotNull User owner, @NotNull @PastOrPresent LocalDateTime since);

    /**
     * Documenti caricati da un professionista a partire dalla data indicata.
     *
     * @param professional il professionista che ha caricato
     * @param since        data/ora minima di caricamento
     * @return i documenti caricati dal professionista non prima di {@code since}
     */
    List<Document> findRecentByProfessional(@NotNull User professional, @NotNull LocalDateTime since);

    /**
     * L'ultimo documento di un certo tipo per quell'utente.
     *
     * @param owner il proprietario dei documenti
     * @param type  tipo di documento
     * @return il documento più recente di quel tipo, oppure {@code null} se non esiste
     */
    Document findLatestByOwnerAndType(@NotNull User owner, @NotNull DocumentType type);

    /**
     * Quanti documenti ha caricato il professionista dalla data indicata.
     *
     * @param professional il professionista che ha caricato
     * @param since        data/ora minima di caricamento
     * @return il numero di documenti caricati non prima di {@code since}
     */
    int countUploadedSince(@NotNull User professional, @NotNull LocalDateTime since);
}
