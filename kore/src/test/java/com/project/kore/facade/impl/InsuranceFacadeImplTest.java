package com.project.kore.facade.impl;

import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.DocumentUploadResponse;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.DocumentType;
import com.project.kore.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import com.project.kore.exception.document.InvalidFileException;
import com.project.kore.mapper.DocumentMapper;
import com.project.kore.mapper.SubscriptionMapper;
import com.project.kore.mapper.UserMapper;
import com.project.kore.model.Document;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.service.DocumentService;
import com.project.kore.service.FileStorageService;
import com.project.kore.service.SubscriptionService;
import com.project.kore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InsuranceFacadeImpl unit tests")
class InsuranceFacadeImplTest {

    @Mock private UserService userService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private DocumentService documentService;
    @Mock private FileStorageService fileStorageService;
    @Mock private UserMapper userMapper;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private DocumentMapper documentMapper;

    @InjectMocks
    private InsuranceFacadeImpl facade;

    private User clientUser;
    private User insuranceManager;
    private Document insuranceDoc;
    private Document otherDoc;

    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId(1L);
        clientUser.setRole(Role.CLIENT);

        insuranceManager = new User();
        insuranceManager.setId(20L);
        insuranceManager.setRole(Role.INSURANCE_MANAGER);

        insuranceDoc = new Document();
        insuranceDoc.setId(100L);
        insuranceDoc.setFileName("policy.pdf");
        insuranceDoc.setFilePath("/uploads/policy.pdf");
        insuranceDoc.setContentType("application/pdf");
        insuranceDoc.setType(DocumentType.INSURANCE_POLICE);
        insuranceDoc.setUploadDate(LocalDateTime.now());

        otherDoc = new Document();
        otherDoc.setId(200L);
        otherDoc.setType(DocumentType.DIET_PLAN);
    }

    // ─── getAllClients ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllClients: returns mapped list of CLIENT users")
    void getAllClients_returnsMappedClientList() {
        List<User> clients = List.of(clientUser);
        List<UserResponse> expected = List.of(UserResponse.builder().build());

        when(userService.findByRole(Role.CLIENT)).thenReturn(clients);
        when(userMapper.toAdminResponse(clients)).thenReturn(expected);

        List<UserResponse> result = facade.getAllClients();

        assertThat(result).isEqualTo(expected);
        verify(userService).findByRole(Role.CLIENT);
    }

    // ─── getChatContacts ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getChatContacts: filters only ADMIN and MODERATOR roles")
    void getChatContacts_filtersAdminAndModerator() {
        User admin = new User(); admin.setRole(Role.ADMIN);
        User mod = new User(); mod.setRole(Role.MODERATOR);
        User client = new User(); client.setRole(Role.CLIENT);

        when(userService.findAll()).thenReturn(List.of(admin, mod, client));
        when(userMapper.toAdminResponse(any(User.class))).thenReturn(UserResponse.builder().build());

        List<UserResponse> result = facade.getChatContacts();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getChatContacts: INSURANCE_MANAGER itself is excluded")
    void getChatContacts_insuranceManagerExcluded() {
        when(userService.findAll()).thenReturn(List.of(insuranceManager));

        List<UserResponse> result = facade.getChatContacts();

        assertThat(result).isEmpty();
    }

    // ─── getAllSubscriptions ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllSubscriptions: maps all subscriptions and returns them")
    void getAllSubscriptions_returnsMappedList() {
        Subscription sub = new Subscription();
        SubscriptionResponse resp = new SubscriptionResponse();

        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of(sub));
        when(subscriptionMapper.toResponse(sub)).thenReturn(resp);

        List<SubscriptionResponse> result = facade.getAllSubscriptions();

        assertThat(result).hasSize(1).containsExactly(resp);
    }

    // ─── getDocumentById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getDocumentById: returns document when type is INSURANCE_POLICE")
    void getDocumentById_insurancePolice_returnsDoc() {
        when(documentService.getDocumentById(100L)).thenReturn(insuranceDoc);

        Document result = facade.getDocumentById(100L);

        assertThat(result).isEqualTo(insuranceDoc);
    }

    @Test
    @DisplayName("getDocumentById: throws AccessDeniedException when document is not an insurance policy")
    void getDocumentById_notInsurancePolice_throwsUnauthorized() {
        when(documentService.getDocumentById(200L)).thenReturn(otherDoc);

        assertThatThrownBy(() -> facade.getDocumentById(200L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("polizza");
    }

    // ─── uploadPolicy ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("uploadPolicy: stores file and creates document record for CLIENT")
    void uploadPolicy_validClient_returnsUploadResponse() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("policy.pdf");
        when(file.getContentType()).thenReturn("application/pdf");

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.getUserById(20L)).thenReturn(insuranceManager);
        when(fileStorageService.store(file)).thenReturn("/uploads/policy.pdf");
        when(documentService.uploadDocument(anyString(), anyString(), anyString(),
                eq(DocumentType.INSURANCE_POLICE.name()), eq(clientUser), eq(insuranceManager)))
                .thenReturn(insuranceDoc);

        DocumentUploadResponse result = facade.uploadPolicy(file, 1L, 20L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getFileName()).isEqualTo("policy.pdf");
        assertThat(result.getType()).isEqualTo(DocumentType.INSURANCE_POLICE.name());
    }

    @Test
    @DisplayName("uploadPolicy: throws InvalidFileException when target user is not a CLIENT")
    void uploadPolicy_targetNotClient_throwsInvalidFile() {
        User nonClient = new User();
        nonClient.setId(5L);
        nonClient.setRole(Role.MODERATOR);

        when(userService.getUserById(5L)).thenReturn(nonClient);

        MultipartFile file = mock(MultipartFile.class);
        assertThatThrownBy(() -> facade.uploadPolicy(file, 5L, 20L))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("clienti");
    }

    @Test
    @DisplayName("uploadPolicy: deletes stored file when document service throws an exception")
    void uploadPolicy_documentServiceFails_deletesStoredFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("policy.pdf");
        when(file.getContentType()).thenReturn("application/pdf");

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.getUserById(20L)).thenReturn(insuranceManager);
        when(fileStorageService.store(file)).thenReturn("/uploads/policy.pdf");
        when(documentService.uploadDocument(anyString(), anyString(), anyString(),
                anyString(), any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> facade.uploadPolicy(file, 1L, 20L))
                .isInstanceOf(RuntimeException.class);

        verify(fileStorageService).delete("/uploads/policy.pdf");
    }

    // ─── downloadPolicy ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("downloadPolicy: loads file bytes when document is INSURANCE_POLICE")
    void downloadPolicy_insurancePolice_returnsBytes() {
        byte[] bytes = new byte[]{1, 2, 3};
        when(documentService.getDocumentById(100L)).thenReturn(insuranceDoc);
        when(fileStorageService.load("/uploads/policy.pdf")).thenReturn(bytes);

        byte[] result = facade.downloadPolicy(100L);

        assertThat(result).isEqualTo(bytes);
    }

    @Test
    @DisplayName("downloadPolicy: throws AccessDeniedException when document is not a policy")
    void downloadPolicy_wrongType_throwsUnauthorized() {
        when(documentService.getDocumentById(200L)).thenReturn(otherDoc);

        assertThatThrownBy(() -> facade.downloadPolicy(200L))
                .isInstanceOf(AccessDeniedException.class);

        verify(fileStorageService, never()).load(anyString());
    }

    // ─── deletePolicy ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deletePolicy: deletes document record and file from storage")
    void deletePolicy_insurancePolice_deletesDocAndFile() {
        when(documentService.getDocumentById(100L)).thenReturn(insuranceDoc);

        facade.deletePolicy(100L);

        verify(documentService).deleteDocument(100L);
        verify(fileStorageService).delete("/uploads/policy.pdf");
    }

    @Test
    @DisplayName("deletePolicy: throws AccessDeniedException when document is not a policy")
    void deletePolicy_wrongType_throwsUnauthorized() {
        when(documentService.getDocumentById(200L)).thenReturn(otherDoc);

        assertThatThrownBy(() -> facade.deletePolicy(200L))
                .isInstanceOf(AccessDeniedException.class);

        verify(documentService, never()).deleteDocument(anyLong());
        verify(fileStorageService, never()).delete(anyString());
    }

    // ─── getClientPolicies ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getClientPolicies: returns list of mapped policy documents for a CLIENT")
    void getClientPolicies_validClient_returnsPolicies() {
        DocumentResponse docResponse = DocumentResponse.builder().build();
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(documentService.getUserDocumentsByType(clientUser, DocumentType.INSURANCE_POLICE.name()))
                .thenReturn(List.of(insuranceDoc));
        when(documentMapper.toResponseList(List.of(insuranceDoc))).thenReturn(List.of(docResponse));

        List<DocumentResponse> result = facade.getClientPolicies(1L);

        assertThat(result).hasSize(1).containsExactly(docResponse);
    }

    @Test
    @DisplayName("getClientPolicies: throws InvalidFileException when user is not a CLIENT")
    void getClientPolicies_notClient_throwsInvalidFile() {
        User ptUser = new User();
        ptUser.setId(2L);
        ptUser.setRole(Role.PERSONAL_TRAINER);

        when(userService.getUserById(2L)).thenReturn(ptUser);

        assertThatThrownBy(() -> facade.getClientPolicies(2L))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("clienti");
    }

    // ─── updatePolicyNotes ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePolicyNotes: updates notes and returns mapped response")
    void updatePolicyNotes_insurancePolice_returnsUpdatedNotes() {
        UpdatedNotesResponse notesResponse = UpdatedNotesResponse.builder().build();

        when(documentService.getDocumentById(100L)).thenReturn(insuranceDoc);
        when(documentService.updateNotes(100L, "some notes")).thenReturn(insuranceDoc);
        when(documentMapper.toUpdatedNotesResponse(insuranceDoc)).thenReturn(notesResponse);

        UpdatedNotesResponse result = facade.updatePolicyNotes(100L, "some notes");

        assertThat(result).isEqualTo(notesResponse);
    }

    @Test
    @DisplayName("updatePolicyNotes: throws AccessDeniedException when document is not a policy")
    void updatePolicyNotes_wrongType_throwsUnauthorized() {
        when(documentService.getDocumentById(200L)).thenReturn(otherDoc);

        assertThatThrownBy(() -> facade.updatePolicyNotes(200L, "notes"))
                .isInstanceOf(AccessDeniedException.class);

        verify(documentService, never()).updateNotes(anyLong(), anyString());
    }
}
