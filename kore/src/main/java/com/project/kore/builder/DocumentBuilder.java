package com.project.kore.builder;

import com.project.kore.enums.DocumentType;
import java.time.LocalDateTime;
import com.project.kore.model.*;


/**
 * Costruisce un Document, ovvero i metadati di un file caricato, con interfaccia fluente.
 */
public interface DocumentBuilder {

    /**
     * Imposta l'id del documento.
     *
     * @param id identificativo del documento
     * @return questo builder, per concatenare le chiamate
     */
    DocumentBuilder id(Long id);

    /**
     * Imposta il nome originale del file.
     *
     * @param fileName nome del file
     * @return questo builder, per concatenare le chiamate
     */
    DocumentBuilder fileName(String fileName);

    /**
     * Imposta il percorso del file su disco.
     *
     * @param filePath percorso di archiviazione
     * @return questo builder, per concatenare le chiamate
     */
    DocumentBuilder filePath(String filePath);

    /**
     * Imposta il MIME type del file.
     *
     * @param contentType tipo di contenuto
     * @return questo builder, per concatenare le chiamate
     */
    DocumentBuilder contentType(String contentType);

    /**
     * Imposta il tipo di documento (es. scheda, piano alimentare, polizza).
     *
     * @param type tipo di documento
     * @return questo builder, per concatenare le chiamate
     */
    DocumentBuilder type(DocumentType type);

    /**
     * Imposta il proprietario del documento.
     *
     * @param owner l'utente proprietario
     * @return questo builder, per concatenare le chiamate
     */
    DocumentBuilder owner(User owner);

    /**
     * Imposta chi ha caricato il documento.
     *
     * @param uploadedBy l'utente che ha effettuato l'upload
     * @return questo builder, per concatenare le chiamate
     */
    DocumentBuilder uploadedBy(User uploadedBy);

    /**
     * Imposta la data di caricamento.
     *
     * @param uploadDate data/ora di upload
     * @return questo builder, per concatenare le chiamate
     */
    DocumentBuilder uploadDate(LocalDateTime uploadDate);

    /**
     * Imposta le note testuali associate al documento.
     *
     * @param notes note del documento
     * @return questo builder, per concatenare le chiamate
     */
    DocumentBuilder notes(String notes);

    /**
     * Costruisce il Document con i valori impostati.
     *
     * @return il documento costruito
     */
    Document build();
}
