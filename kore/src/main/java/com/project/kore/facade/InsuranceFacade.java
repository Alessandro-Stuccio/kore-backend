package com.project.kore.facade;

import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.DocumentUploadResponse;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.exception.document.InvalidFileException;
import com.project.kore.model.Document;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Operazioni dell'Insurance Manager: gestione polizze e visibilità sui clienti.
 */
@Validated
public interface InsuranceFacade {

    /**
     * Tutti i clienti registrati.
     *
     * @return l'elenco dei clienti
     */
    List<UserResponse> getAllClients();

    /**
     * Tutti gli abbonamenti, attivi e storici.
     *
     * @return l'elenco completo degli abbonamenti
     */
    List<SubscriptionResponse> getAllSubscriptions();

    /**
     * Utenti che l'Insurance Manager può contattare in chat.
     *
     * @return i contatti disponibili per la chat
     */
    List<UserResponse> getChatContacts();

    /**
     * Recupera un documento dal suo id.
     *
     * @param documentId id del documento
     * @return il documento trovato
     */
    Document getDocumentById(Long documentId);

    /**
     * Carica una polizza per un cliente per conto dell'Insurance Manager. Il tipo viene forzato a
     * polizza assicurativa; se la scrittura del record fallisce il file su disco viene rimosso.
     *
     * @param file     file della polizza
     * @param clientId id del cliente destinatario
     * @param callerId id dell'Insurance Manager che carica
     * @return i dati del documento caricato
     * @throws InvalidFileException se l'utente destinatario non è un cliente
     */
    DocumentUploadResponse uploadPolicy(MultipartFile file, Long clientId, Long callerId);

    /**
     * Scarica il contenuto di una polizza.
     *
     * @param documentId id della polizza
     * @return il contenuto binario del file
     * @throws AccessDeniedException se il documento non è una polizza assicurativa
     */
    byte[] downloadPolicy(Long documentId);

    /**
     * Elimina una polizza, rimuovendo sia il record sia il file su disco.
     *
     * @param documentId id della polizza
     * @throws AccessDeniedException se il documento non è una polizza assicurativa
     */
    void deletePolicy(Long documentId);

    /**
     * Le polizze associate a un cliente.
     *
     * @param clientId id del cliente
     * @return le polizze del cliente
     * @throws InvalidFileException se l'utente indicato non è un cliente
     */
    List<DocumentResponse> getClientPolicies(Long clientId);

    /**
     * Aggiorna le note di una polizza.
     *
     * @param documentId id della polizza
     * @param notes      nuovo testo delle note
     * @return i dati aggiornati delle note
     * @throws AccessDeniedException se il documento non è una polizza assicurativa
     */
    UpdatedNotesResponse updatePolicyNotes(Long documentId, @Size(max = 1000) String notes);
}
