package com.project.kore.mapper;

import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.model.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte i documenti nei rispettivi DTO di risposta.
 */
@Component
public class DocumentMapper {

    /**
     * Converte un documento nel suo DTO di risposta.
     *
     * @param doc il documento da convertire
     * @return il DTO del documento, oppure {@code null} se l'input è {@code null}
     */
    public DocumentResponse toResponse(Document doc) {
        if (doc == null) return null;
        return DocumentResponse.builder()
                .id(doc.getId())
                .fileName(doc.getFileName())
                .contentType(doc.getContentType())
                .type(doc.getType().name())
                .uploadDate(doc.getUploadDate().toString())
                .notes(doc.getNotes())
                .uploadedByName(doc.getUploadedBy() != null ? doc.getUploadedBy().getFullName() : null)
                .build();
    }

    /**
     * Converte una lista di documenti nei rispettivi DTO.
     *
     * @param documents i documenti da convertire
     * @return i DTO dei documenti
     */
    public List<DocumentResponse> toResponseList(List<Document> documents) {
        return documents.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Risposta minimale dopo la modifica delle note: solo id e note aggiornate.
     *
     * @param doc il documento aggiornato
     * @return il DTO con id e note
     */
    public UpdatedNotesResponse toUpdatedNotesResponse(Document doc) {
        return UpdatedNotesResponse.builder().id(doc.getId()).notes(doc.getNotes()).build();
    }
}
