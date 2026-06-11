package com.project.kore.facade.impl;

import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.DocumentUploadResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.enums.DocumentType;
import com.project.kore.enums.Role;
import com.project.kore.exception.document.InvalidFileException;
import com.project.kore.mapper.DocumentMapper;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import com.project.kore.service.DocumentService;
import com.project.kore.service.FileStorageService;
import com.project.kore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentFacadeImpl unit tests")
class DocumentFacadeImplTest {

    @Mock private DocumentService documentService;
    @Mock private FileStorageService fileStorageService;
    @Mock private UserService userService;
    @Mock private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentFacadeImpl documentFacade;

    private User pt;
    private User nutri;
    private User client;
    private User admin;
    private Document document;

    @BeforeEach
    void setUp() {
        pt = new User();
        pt.setId(2L);
        pt.setRole(Role.PERSONAL_TRAINER);

        nutri = new User();
        nutri.setId(3L);
        nutri.setRole(Role.NUTRITIONIST);

        client = new User();
        client.setId(1L);
        client.setRole(Role.CLIENT);

        admin = new User();
        admin.setId(9L);
        admin.setRole(Role.ADMIN);

        document = new Document();
        document.setId(10L);
        document.setFileName("scheda.pdf");
        document.setFilePath("/uploads/scheda.pdf");
        document.setType(DocumentType.WORKOUT_PLAN);
        document.setUploadDate(LocalDateTime.now());
        document.setOwner(client);
        document.setUploadedBy(pt);
    }

    // ─── uploadDocumentWithValidation ─────────────────────────────────────────────

    @Test
    @DisplayName("upload: PT con tipo diverso da WORKOUT_PLAN → InvalidFileException")
    void upload_ptWrongType_throws() {
        when(userService.getUserById(2L)).thenReturn(pt);

        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        assertThatThrownBy(() -> documentFacade.uploadDocumentWithValidation(file, 1L, 2L, "DIET_PLAN"))
                .isInstanceOf(InvalidFileException.class);

        verify(fileStorageService, never()).store(any());
    }

    @Test
    @DisplayName("upload: NUTRITIONIST con tipo diverso da DIET_PLAN → InvalidFileException")
    void upload_nutriWrongType_throws() {
        when(userService.getUserById(3L)).thenReturn(nutri);

        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        assertThatThrownBy(() -> documentFacade.uploadDocumentWithValidation(file, 1L, 3L, "WORKOUT_PLAN"))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    @DisplayName("upload: INSURANCE_MANAGER con tipo diverso da INSURANCE_POLICE → InvalidFileException")
    void upload_insuranceWrongType_throws() {
        User insurance = new User();
        insurance.setId(4L);
        insurance.setRole(Role.INSURANCE_MANAGER);
        when(userService.getUserById(4L)).thenReturn(insurance);

        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        assertThatThrownBy(() -> documentFacade.uploadDocumentWithValidation(file, 1L, 4L, "DIET_PLAN"))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    @DisplayName("upload: happy path → salva il documento e ritorna la response valorizzata")
    void upload_happyPath_returnsResponse() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(userService.getUserById(2L)).thenReturn(pt);
        when(userService.getUserById(1L)).thenReturn(client);
        when(fileStorageService.store(file)).thenReturn("/uploads/scheda.pdf");
        when(documentService.uploadDocument(any(), any(), any(), eq("WORKOUT_PLAN"), eq(client), eq(pt)))
                .thenReturn(document);

        DocumentUploadResponse response =
                documentFacade.uploadDocumentWithValidation(file, 1L, 2L, "WORKOUT_PLAN");

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getFileName()).isEqualTo("scheda.pdf");
        assertThat(response.getType()).isEqualTo("WORKOUT_PLAN");
        assertThat(response.getUploadDate()).isEqualTo(document.getUploadDate().toString());
    }

    @Test
    @DisplayName("upload: se la scrittura del record fallisce → cancella il file e rilancia")
    void upload_persistFails_deletesFileAndRethrows() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(userService.getUserById(2L)).thenReturn(pt);
        when(userService.getUserById(1L)).thenReturn(client);
        when(fileStorageService.store(file)).thenReturn("/uploads/scheda.pdf");
        when(documentService.uploadDocument(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> documentFacade.uploadDocumentWithValidation(file, 1L, 2L, "WORKOUT_PLAN"))
                .isInstanceOf(RuntimeException.class);

        verify(fileStorageService).delete("/uploads/scheda.pdf");
    }

    // ─── getDocumentById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getDocumentById: delega al service")
    void getDocumentById_delegates() {
        when(documentService.getDocumentById(10L)).thenReturn(document);

        assertThat(documentFacade.getDocumentById(10L)).isSameAs(document);
    }

    // ─── deleteDocument ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteDocument: chi l'ha caricato può eliminarlo")
    void deleteDocument_uploader_ok() {
        when(documentService.getDocumentById(10L)).thenReturn(document);
        when(userService.getUserById(2L)).thenReturn(pt);

        documentFacade.deleteDocument(10L, 2L);

        verify(documentService).deleteDocument(10L);
        verify(fileStorageService).delete("/uploads/scheda.pdf");
    }

    @Test
    @DisplayName("deleteDocument: admin/moderatore può eliminarlo")
    void deleteDocument_privileged_ok() {
        when(documentService.getDocumentById(10L)).thenReturn(document);
        when(userService.getUserById(9L)).thenReturn(admin);

        documentFacade.deleteDocument(10L, 9L);

        verify(documentService).deleteDocument(10L);
    }

    @Test
    @DisplayName("deleteDocument: utente non autorizzato → AccessDeniedException")
    void deleteDocument_unauthorized_throws() {
        when(documentService.getDocumentById(10L)).thenReturn(document);
        when(userService.getUserById(1L)).thenReturn(client);

        assertThatThrownBy(() -> documentFacade.deleteDocument(10L, 1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(documentService, never()).deleteDocument(any());
    }

    // ─── downloadDocumentSecure ───────────────────────────────────────────────────

    @Test
    @DisplayName("download: il proprietario può scaricare")
    void download_owner_ok() {
        when(documentService.getDocumentById(10L)).thenReturn(document);
        when(userService.getUserById(1L)).thenReturn(client);
        when(fileStorageService.load("/uploads/scheda.pdf")).thenReturn(new byte[]{1, 2, 3});

        assertThat(documentFacade.downloadDocumentSecure(10L, 1L)).isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    @DisplayName("download: il PT assegnato al proprietario può scaricare")
    void download_assignedPt_ok() {
        client.setAssignedPT(pt);
        when(documentService.getDocumentById(10L)).thenReturn(document);
        when(userService.getUserById(2L)).thenReturn(pt);
        when(fileStorageService.load("/uploads/scheda.pdf")).thenReturn(new byte[]{9});

        assertThat(documentFacade.downloadDocumentSecure(10L, 2L)).isEqualTo(new byte[]{9});
    }

    @Test
    @DisplayName("download: il nutrizionista assegnato al proprietario può scaricare")
    void download_assignedNutri_ok() {
        client.setAssignedNutritionist(nutri);
        when(documentService.getDocumentById(10L)).thenReturn(document);
        when(userService.getUserById(3L)).thenReturn(nutri);
        when(fileStorageService.load("/uploads/scheda.pdf")).thenReturn(new byte[]{7});

        assertThat(documentFacade.downloadDocumentSecure(10L, 3L)).isEqualTo(new byte[]{7});
    }

    @Test
    @DisplayName("download: utente non collegato al documento → AccessDeniedException")
    void download_unauthorized_throws() {
        User stranger = new User();
        stranger.setId(99L);
        stranger.setRole(Role.CLIENT);
        when(documentService.getDocumentById(10L)).thenReturn(document);
        when(userService.getUserById(99L)).thenReturn(stranger);

        assertThatThrownBy(() -> documentFacade.downloadDocumentSecure(10L, 99L))
                .isInstanceOf(AccessDeniedException.class);

        verify(fileStorageService, never()).load(any());
    }

    // ─── getUserDocumentsDtoSecure ────────────────────────────────────────────────

    @Test
    @DisplayName("getUserDocuments: l'utente stesso vede i propri documenti")
    void getUserDocuments_self_ok() {
        List<Document> docs = List.of(document);
        List<DocumentResponse> expected = List.of(org.mockito.Mockito.mock(DocumentResponse.class));
        when(userService.getUserById(1L)).thenReturn(client);
        when(documentService.getUserDocuments(client)).thenReturn(docs);
        when(documentMapper.toResponseList(docs)).thenReturn(expected);

        assertThat(documentFacade.getUserDocumentsDtoSecure(1L, 1L)).isEqualTo(expected);
    }

    @Test
    @DisplayName("getUserDocuments: il professionista assegnato vede i documenti del cliente")
    void getUserDocuments_assignedPt_ok() {
        client.setAssignedPT(pt);
        List<Document> docs = List.of(document);
        List<DocumentResponse> expected = List.of(org.mockito.Mockito.mock(DocumentResponse.class));
        when(userService.getUserById(2L)).thenReturn(pt);
        when(userService.getUserById(1L)).thenReturn(client);
        when(documentService.getUserDocuments(client)).thenReturn(docs);
        when(documentMapper.toResponseList(docs)).thenReturn(expected);

        assertThat(documentFacade.getUserDocumentsDtoSecure(1L, 2L)).isEqualTo(expected);
    }

    @Test
    @DisplayName("getUserDocuments: il nutrizionista assegnato vede i documenti del cliente")
    void getUserDocuments_assignedNutri_ok() {
        client.setAssignedNutritionist(nutri);
        List<Document> docs = List.of(document);
        List<DocumentResponse> expected = List.of(org.mockito.Mockito.mock(DocumentResponse.class));
        when(userService.getUserById(3L)).thenReturn(nutri);
        when(userService.getUserById(1L)).thenReturn(client);
        when(documentService.getUserDocuments(client)).thenReturn(docs);
        when(documentMapper.toResponseList(docs)).thenReturn(expected);

        assertThat(documentFacade.getUserDocumentsDtoSecure(1L, 3L)).isEqualTo(expected);
    }

    @Test
    @DisplayName("getUserDocuments: estraneo → AccessDeniedException")
    void getUserDocuments_unauthorized_throws() {
        User other = new User();
        other.setId(50L);
        other.setRole(Role.CLIENT);
        when(userService.getUserById(50L)).thenReturn(other);
        when(userService.getUserById(1L)).thenReturn(client);

        assertThatThrownBy(() -> documentFacade.getUserDocumentsDtoSecure(1L, 50L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ─── getUserDocumentsByTypeDtoSecure ──────────────────────────────────────────

    @Test
    @DisplayName("getUserDocumentsByType: privilegiato (admin) ottiene la lista filtrata")
    void getUserDocumentsByType_privileged_ok() {
        List<Document> docs = List.of(document);
        List<DocumentResponse> expected = List.of(org.mockito.Mockito.mock(DocumentResponse.class));
        when(userService.getUserById(9L)).thenReturn(admin);
        when(userService.getUserById(1L)).thenReturn(client);
        when(documentService.getUserDocumentsByType(client, "WORKOUT_PLAN")).thenReturn(docs);
        when(documentMapper.toResponseList(docs)).thenReturn(expected);

        assertThat(documentFacade.getUserDocumentsByTypeDtoSecure(1L, "WORKOUT_PLAN", 9L))
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("getUserDocumentsByType: il PT assegnato ottiene la lista filtrata")
    void getUserDocumentsByType_assignedPt_ok() {
        client.setAssignedPT(pt);
        List<Document> docs = List.of(document);
        List<DocumentResponse> expected = List.of(org.mockito.Mockito.mock(DocumentResponse.class));
        when(userService.getUserById(2L)).thenReturn(pt);
        when(userService.getUserById(1L)).thenReturn(client);
        when(documentService.getUserDocumentsByType(client, "WORKOUT_PLAN")).thenReturn(docs);
        when(documentMapper.toResponseList(docs)).thenReturn(expected);

        assertThat(documentFacade.getUserDocumentsByTypeDtoSecure(1L, "WORKOUT_PLAN", 2L))
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("getUserDocumentsByType: il nutrizionista assegnato ottiene la lista filtrata")
    void getUserDocumentsByType_assignedNutri_ok() {
        client.setAssignedNutritionist(nutri);
        List<Document> docs = List.of(document);
        List<DocumentResponse> expected = List.of(org.mockito.Mockito.mock(DocumentResponse.class));
        when(userService.getUserById(3L)).thenReturn(nutri);
        when(userService.getUserById(1L)).thenReturn(client);
        when(documentService.getUserDocumentsByType(client, "DIET_PLAN")).thenReturn(docs);
        when(documentMapper.toResponseList(docs)).thenReturn(expected);

        assertThat(documentFacade.getUserDocumentsByTypeDtoSecure(1L, "DIET_PLAN", 3L))
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("getUserDocumentsByType: estraneo → AccessDeniedException")
    void getUserDocumentsByType_unauthorized_throws() {
        User other = new User();
        other.setId(50L);
        other.setRole(Role.CLIENT);
        when(userService.getUserById(50L)).thenReturn(other);
        when(userService.getUserById(1L)).thenReturn(client);

        assertThatThrownBy(() -> documentFacade.getUserDocumentsByTypeDtoSecure(1L, "WORKOUT_PLAN", 50L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ─── updateNotes ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateNotes: il proprietario aggiorna le note")
    void updateNotes_owner_ok() {
        UpdatedNotesResponse expected = org.mockito.Mockito.mock(UpdatedNotesResponse.class);
        when(documentService.getDocumentById(10L)).thenReturn(document);
        when(userService.getUserById(1L)).thenReturn(client);
        when(documentService.updateNotes(10L, "nuove note")).thenReturn(document);
        when(documentMapper.toUpdatedNotesResponse(document)).thenReturn(expected);

        assertThat(documentFacade.updateNotes(10L, "nuove note", 1L)).isSameAs(expected);
    }

    @Test
    @DisplayName("updateNotes: utente non autorizzato → AccessDeniedException")
    void updateNotes_unauthorized_throws() {
        User other = new User();
        other.setId(50L);
        other.setRole(Role.CLIENT);
        when(documentService.getDocumentById(10L)).thenReturn(document);
        when(userService.getUserById(50L)).thenReturn(other);

        assertThatThrownBy(() -> documentFacade.updateNotes(10L, "x", 50L))
                .isInstanceOf(AccessDeniedException.class);

        verify(documentService, never()).updateNotes(any(), any());
    }
}
