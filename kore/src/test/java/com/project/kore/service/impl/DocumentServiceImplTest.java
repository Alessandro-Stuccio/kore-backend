package com.project.kore.service.impl;

import com.project.kore.enums.DocumentType;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import com.project.kore.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private User client;
    private User professional;
    private Document document;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(1L);
        client.setEmail("client@test.com");

        professional = new User();
        professional.setId(2L);
        professional.setEmail("pt@test.com");

        document = new Document();
        document.setId(10L);
        document.setFileName("diet.pdf");
        document.setFilePath("/uploads/diet.pdf");
        document.setContentType("application/pdf");
        document.setType(DocumentType.DIET_PLAN);
        document.setOwner(client);
        document.setUploadedBy(professional);
        document.setUploadDate(LocalDateTime.now().minusHours(1));
    }

    // ---- uploadDocument ----

    @Test
    @DisplayName("uploadDocument: builds Document with correct fields and persists it")
    void uploadDocument_buildsAndPersists() {
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        Document result = documentService.uploadDocument(
                "/uploads/diet.pdf", "diet.pdf", "application/pdf",
                "DIET_PLAN", client, professional);

        assertThat(result).isSameAs(document);
        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());
        Document built = captor.getValue();
        assertThat(built.getFileName()).isEqualTo("diet.pdf");
        assertThat(built.getFilePath()).isEqualTo("/uploads/diet.pdf");
        assertThat(built.getContentType()).isEqualTo("application/pdf");
        assertThat(built.getType()).isEqualTo(DocumentType.DIET_PLAN);
        assertThat(built.getOwner()).isSameAs(client);
        assertThat(built.getUploadedBy()).isSameAs(professional);
        assertThat(built.getUploadDate()).isNotNull();
    }

    @Test
    @DisplayName("uploadDocument: sets uploadDate to current time at point of upload")
    void uploadDocument_setsUploadDateToNow() {
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime before = LocalDateTime.now();
        Document result = documentService.uploadDocument(
                "/uploads/plan.pdf", "plan.pdf", "application/pdf",
                "DIET_PLAN", client, professional);
        LocalDateTime after = LocalDateTime.now();

        assertThat(result.getUploadDate()).isBetween(before, after);
    }

    // ---- findRecentByOwner ----

    @Test
    @DisplayName("findRecentByOwner: delegates to repository with owner and since parameter")
    void findRecentByOwner_delegatesToRepository() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        when(documentRepository.findRecentByOwner(client, since)).thenReturn(List.of(document));

        List<Document> result = documentService.findRecentByOwner(client, since);

        assertThat(result).containsExactly(document);
        verify(documentRepository).findRecentByOwner(client, since);
    }

    @Test
    @DisplayName("findRecentByOwner: returns empty list when owner has no recent documents")
    void findRecentByOwner_noRecentDocuments_returnsEmpty() {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        when(documentRepository.findRecentByOwner(client, since)).thenReturn(List.of());

        assertThat(documentService.findRecentByOwner(client, since)).isEmpty();
    }

    // ---- findRecentByProfessional ----

    @Test
    @DisplayName("findRecentByProfessional: delegates to repository via findRecentByUploader")
    void findRecentByProfessional_delegatesToRepository() {
        LocalDateTime since = LocalDateTime.now().minusDays(3);
        when(documentRepository.findRecentByUploader(professional, since)).thenReturn(List.of(document));

        List<Document> result = documentService.findRecentByProfessional(professional, since);

        assertThat(result).containsExactly(document);
        verify(documentRepository).findRecentByUploader(professional, since);
    }

    // ---- getDocumentById ----

    @Test
    @DisplayName("getDocumentById: returns document when found by id")
    void getDocumentById_found_returnsDocument() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        Document result = documentService.getDocumentById(10L);

        assertThat(result).isSameAs(document);
    }

    @Test
    @DisplayName("getDocumentById: throws CustomResourceNotFoundException when document id does not exist")
    void getDocumentById_notFound_throwsCustomResourceNotFoundException() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDocumentById(999L))
                .isInstanceOf(CustomResourceNotFoundException.class);
    }

    // ---- getUserDocuments ----

    @Test
    @DisplayName("getUserDocuments: returns all documents for owner ordered by upload date desc")
    void getUserDocuments_returnsDocumentsForOwner() {
        when(documentRepository.findByOwnerOrderByUploadDateDesc(client)).thenReturn(List.of(document));

        List<Document> result = documentService.getUserDocuments(client);

        assertThat(result).containsExactly(document);
        verify(documentRepository).findByOwnerOrderByUploadDateDesc(client);
    }

    @Test
    @DisplayName("getUserDocuments: returns empty list when owner has no documents")
    void getUserDocuments_noDocuments_returnsEmpty() {
        when(documentRepository.findByOwnerOrderByUploadDateDesc(client)).thenReturn(List.of());

        assertThat(documentService.getUserDocuments(client)).isEmpty();
    }

    // ---- getUserDocumentsByType ----

    @Test
    @DisplayName("getUserDocumentsByType: returns documents filtered by type for the owner")
    void getUserDocumentsByType_returnsFilteredDocuments() {
        when(documentRepository.findByOwnerAndTypeOrderByUploadDateDesc(client, DocumentType.DIET_PLAN))
                .thenReturn(List.of(document));

        List<Document> result = documentService.getUserDocumentsByType(client, "DIET_PLAN");

        assertThat(result).containsExactly(document);
        verify(documentRepository).findByOwnerAndTypeOrderByUploadDateDesc(client, DocumentType.DIET_PLAN);
    }

    @Test
    @DisplayName("getUserDocumentsByType: returns empty list when owner has no documents of the given type")
    void getUserDocumentsByType_noMatch_returnsEmpty() {
        when(documentRepository.findByOwnerAndTypeOrderByUploadDateDesc(client, DocumentType.DIET_PLAN))
                .thenReturn(List.of());

        assertThat(documentService.getUserDocumentsByType(client, "DIET_PLAN")).isEmpty();
    }

    // ---- deleteDocument ----

    @Test
    @DisplayName("deleteDocument: loads document by id and calls repository delete")
    void deleteDocument_exists_callsRepositoryDelete() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        documentService.deleteDocument(10L);

        verify(documentRepository).delete(document);
    }

    @Test
    @DisplayName("deleteDocument: throws CustomResourceNotFoundException when document id does not exist")
    void deleteDocument_notFound_throwsCustomResourceNotFoundException() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.deleteDocument(999L))
                .isInstanceOf(CustomResourceNotFoundException.class);

        verify(documentRepository, never()).delete(any());
    }

    // ---- updateNotes ----

    @Test
    @DisplayName("updateNotes: sets notes on document and saves, returns updated document")
    void updateNotes_setsNotesAndSaves() {
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        Document result = documentService.updateNotes(10L, "New diet notes");

        assertThat(document.getNotes()).isEqualTo("New diet notes");
        assertThat(result).isSameAs(document);
        verify(documentRepository).save(document);
    }

    @Test
    @DisplayName("updateNotes: throws CustomResourceNotFoundException when document does not exist")
    void updateNotes_documentNotFound_throwsCustomResourceNotFoundException() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.updateNotes(999L, "notes"))
                .isInstanceOf(CustomResourceNotFoundException.class);

        verify(documentRepository, never()).save(any());
    }

    // ---- saveDocument ----

    @Test
    @DisplayName("saveDocument: delegates to repository and returns persisted document")
    void saveDocument_persistsAndReturns() {
        when(documentRepository.save(document)).thenReturn(document);

        Document result = documentService.saveDocument(document);

        assertThat(result).isSameAs(document);
        verify(documentRepository).save(document);
    }

    // ---- findLatestByOwnerAndType ----

    @Test
    @DisplayName("findLatestByOwnerAndType: delegates to repository and returns latest document")
    void findLatestByOwnerAndType_returnsLatestDocument() {
        when(documentRepository.findLatestByOwnerAndType(client, DocumentType.DIET_PLAN)).thenReturn(document);

        Document result = documentService.findLatestByOwnerAndType(client, DocumentType.DIET_PLAN);

        assertThat(result).isSameAs(document);
        verify(documentRepository).findLatestByOwnerAndType(client, DocumentType.DIET_PLAN);
    }

    @Test
    @DisplayName("findLatestByOwnerAndType: returns null when no document of that type exists for owner")
    void findLatestByOwnerAndType_noMatch_returnsNull() {
        when(documentRepository.findLatestByOwnerAndType(client, DocumentType.DIET_PLAN)).thenReturn(null);

        assertThat(documentService.findLatestByOwnerAndType(client, DocumentType.DIET_PLAN)).isNull();
    }

    // ---- countUploadedSince ----

    @Test
    @DisplayName("countUploadedSince: delegates to repository and returns upload count for professional")
    void countUploadedSince_returnsCountFromRepository() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        when(documentRepository.countByUploaderSince(professional, since)).thenReturn(4);

        int result = documentService.countUploadedSince(professional, since);

        assertThat(result).isEqualTo(4);
        verify(documentRepository).countByUploaderSince(professional, since);
    }

    @Test
    @DisplayName("countUploadedSince: returns zero when professional has uploaded no documents in the period")
    void countUploadedSince_noUploads_returnsZero() {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        when(documentRepository.countByUploaderSince(professional, since)).thenReturn(0);

        assertThat(documentService.countUploadedSince(professional, since)).isZero();
    }
}
