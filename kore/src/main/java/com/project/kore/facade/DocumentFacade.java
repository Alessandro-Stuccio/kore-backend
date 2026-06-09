package com.project.kore.facade;

import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.DocumentUploadResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.exception.document.InvalidFileException;
import com.project.kore.model.Document;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Upload, download e gestione documenti, con controllo accessi su ogni operazione.
 */
@Validated
public interface DocumentFacade {

    /**
     * Carica un documento per un cliente, dopo aver validato tipo e permessi dell'uploader.
     *
     * @param file       file da caricare
     * @param clientId   id del cliente proprietario del documento
     * @param uploaderId id di chi effettua il caricamento
     * @param type       tipo di documento richiesto
     * @return i dati del documento caricato
     * @throws InvalidFileException se il tipo di documento non è coerente con il ruolo dell'uploader
     */
    DocumentUploadResponse uploadDocumentWithValidation(MultipartFile file, Long clientId, Long uploaderId, String type);

    /**
     * Recupera un documento dal suo id.
     *
     * @param id id del documento
     * @return il documento trovato
     */
    Document getDocumentById(Long id);

    /**
     * Elimina il documento solo se il chiamante ne è il proprietario.
     *
     * @param id       id del documento
     * @param callerId id dell'utente che richiede l'eliminazione
     * @throws AccessDeniedException se il chiamante non è autorizzato a eliminare il documento
     */
    void deleteDocument(Long id, Long callerId);

    /**
     * Scarica il contenuto del documento dopo aver verificato che il chiamante possa accedervi.
     *
     * @param id       id del documento
     * @param callerId id dell'utente che richiede il download
     * @return il contenuto binario del documento
     * @throws AccessDeniedException se il chiamante non è autorizzato a scaricare il documento
     */
    byte[] downloadDocumentSecure(Long id, Long callerId);

    /**
     * Restituisce i documenti dell'utente target visibili al chiamante.
     *
     * @param targetUserId id dell'utente di cui leggere i documenti
     * @param callerId     id dell'utente che effettua la richiesta
     * @return i documenti visibili al chiamante
     * @throws AccessDeniedException se il chiamante non è autorizzato a vedere quei documenti
     */
    List<DocumentResponse> getUserDocumentsDtoSecure(Long targetUserId, Long callerId);

    /**
     * Come getUserDocumentsDtoSecure, ma filtrando per tipo di documento.
     *
     * @param targetUserId id dell'utente di cui leggere i documenti
     * @param type         tipo di documento da filtrare
     * @param callerId     id dell'utente che effettua la richiesta
     * @return i documenti del tipo indicato visibili al chiamante
     * @throws AccessDeniedException se il chiamante non è autorizzato a vedere quei documenti
     */
    List<DocumentResponse> getUserDocumentsByTypeDtoSecure(Long targetUserId, String type, Long callerId);

    /**
     * Aggiorna le note del documento, verificando i permessi del chiamante.
     *
     * @param id       id del documento
     * @param notes    nuovo testo delle note
     * @param callerId id dell'utente che effettua la modifica
     * @return i dati aggiornati delle note
     * @throws AccessDeniedException se il chiamante non è autorizzato a modificare le note
     */
    UpdatedNotesResponse updateNotes(Long id, @Size(max = 1000) String notes, Long callerId);
}
