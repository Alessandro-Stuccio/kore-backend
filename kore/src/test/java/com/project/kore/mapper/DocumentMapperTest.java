package com.project.kore.mapper;

import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.enums.DocumentType;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    private DocumentMapper documentMapper;

    @BeforeEach
    void setUp() {
        documentMapper = new DocumentMapper();
    }

    // ---- helpers ----

    private User buildUploader(Long id, String firstName, String lastName) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        return u;
    }

    private Document buildDocument(Long id, String fileName, DocumentType type, User uploadedBy, String notes) {
        Document doc = new Document();
        doc.setId(id);
        doc.setFileName(fileName);
        doc.setContentType("application/pdf");
        doc.setType(type);
        doc.setUploadedBy(uploadedBy);
        doc.setUploadDate(LocalDateTime.of(2025, 3, 10, 8, 0));
        doc.setNotes(notes);
        return doc;
    }

    // ---- toResponse: null guard ----

    @Test
    @DisplayName("toResponse: returns null for null document")
    void toResponse_nullDocument_returnsNull() {
        assertThat(documentMapper.toResponse(null)).isNull();
    }

    // ---- toResponse: field mapping ----

    @Test
    @DisplayName("toResponse: maps all fields from document")
    void toResponse_validDocument_mapsAllFields() {
        User uploader = buildUploader(5L, "Marco", "Rossi");
        Document doc = buildDocument(1L, "report.pdf", DocumentType.DIET_PLAN, uploader, "Note importanti");

        DocumentResponse response = documentMapper.toResponse(doc);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFileName()).isEqualTo("report.pdf");
        assertThat(response.getContentType()).isEqualTo("application/pdf");
        assertThat(response.getType()).isEqualTo("DIET_PLAN");
        assertThat(response.getUploadDate()).isEqualTo("2025-03-10T08:00");
        assertThat(response.getNotes()).isEqualTo("Note importanti");
        assertThat(response.getUploadedByName()).isEqualTo("Marco Rossi");
    }

    @Test
    @DisplayName("toResponse: uploadedByName is null when uploadedBy is null")
    void toResponse_nullUploadedBy_uploadedByNameIsNull() {
        Document doc = buildDocument(2L, "scheda.pdf", DocumentType.WORKOUT_PLAN, null, null);

        DocumentResponse response = documentMapper.toResponse(doc);

        assertThat(response.getUploadedByName()).isNull();
    }

    @Test
    @DisplayName("toResponse: type is the enum name as string")
    void toResponse_insurancePolice_typeStringIsEnumName() {
        User uploader = buildUploader(6L, "Sara", "Neri");
        Document doc = buildDocument(3L, "polizza.pdf", DocumentType.INSURANCE_POLICE, uploader, null);

        DocumentResponse response = documentMapper.toResponse(doc);

        assertThat(response.getType()).isEqualTo("INSURANCE_POLICE");
    }

    @Test
    @DisplayName("toResponse: notes can be null")
    void toResponse_nullNotes_notesIsNull() {
        User uploader = buildUploader(7L, "Giulia", "Verdi");
        Document doc = buildDocument(4L, "workout.pdf", DocumentType.WORKOUT_PLAN, uploader, null);

        DocumentResponse response = documentMapper.toResponse(doc);

        assertThat(response.getNotes()).isNull();
    }

    // ---- toResponseList ----

    @Test
    @DisplayName("toResponseList: maps all documents in list")
    void toResponseList_mapsAllDocuments() {
        User uploader = buildUploader(5L, "Marco", "Rossi");
        Document doc1 = buildDocument(1L, "doc1.pdf", DocumentType.DIET_PLAN, uploader, null);
        Document doc2 = buildDocument(2L, "doc2.pdf", DocumentType.WORKOUT_PLAN, uploader, null);

        List<DocumentResponse> result = documentMapper.toResponseList(List.of(doc1, doc2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("toResponseList: returns empty list for empty input")
    void toResponseList_emptyInput_returnsEmptyList() {
        assertThat(documentMapper.toResponseList(List.of())).isEmpty();
    }

    // ---- toUpdatedNotesResponse ----

    @Test
    @DisplayName("toUpdatedNotesResponse: maps id and notes from document")
    void toUpdatedNotesResponse_mapsIdAndNotes() {
        User uploader = buildUploader(5L, "Marco", "Rossi");
        Document doc = buildDocument(10L, "report.pdf", DocumentType.DIET_PLAN, uploader, "Note aggiornate");

        UpdatedNotesResponse response = documentMapper.toUpdatedNotesResponse(doc);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getNotes()).isEqualTo("Note aggiornate");
    }

    @Test
    @DisplayName("toUpdatedNotesResponse: notes can be null")
    void toUpdatedNotesResponse_nullNotes_notesIsNull() {
        User uploader = buildUploader(5L, "Marco", "Rossi");
        Document doc = buildDocument(11L, "report.pdf", DocumentType.DIET_PLAN, uploader, null);

        UpdatedNotesResponse response = documentMapper.toUpdatedNotesResponse(doc);

        assertThat(response.getNotes()).isNull();
    }
}
