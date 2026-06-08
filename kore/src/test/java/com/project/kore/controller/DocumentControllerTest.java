package com.project.kore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.kore.dto.request.UpdateNotesRequest;
import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.DocumentUploadResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.facade.DocumentFacade;
import com.project.kore.model.Document;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    DocumentFacade documentFacade;

    @InjectMocks
    DocumentController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RequestPostProcessor withPtUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User pt = new User();
        pt.setId(3L);
        pt.setEmail("pt1@test.com");
        pt.setRole(Role.PERSONAL_TRAINER);
        pt.setFirstName("Marco");
        pt.setLastName("PT");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                pt, null, List.of(new SimpleGrantedAuthority("ROLE_PERSONAL_TRAINER")));

        withPtUser = (MockHttpServletRequest request) -> {
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return request;
        };
    }

    // ------------------------------------------------------------------ POST /api/documents/upload

    @Test
    @DisplayName("POST /api/documents/upload — 200 con metadati documento caricato")
    void uploadFile_returns200() throws Exception {
        DocumentUploadResponse uploadResponse = DocumentUploadResponse.builder()
                .id(7L).fileName("referto.pdf").type("MEDICAL_REPORT").uploadDate("2026-05-29")
                .build();
        when(documentFacade.uploadDocumentWithValidation(any(), anyLong(), anyLong(), anyString()))
                .thenReturn(uploadResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file", "referto.pdf", "application/pdf", "PDF content".getBytes());

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("clientId", "10")
                        .param("type", "MEDICAL_REPORT")
                        .with(withPtUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.fileName").value("referto.pdf"));
    }

    // ------------------------------------------------------------------ GET /api/documents/download/{id}

    @Test
    @DisplayName("GET /api/documents/download/{id} — 200 con byte del file")
    void downloadFile_returns200() throws Exception {
        byte[] fileBytes = "PDF content".getBytes();
        Document doc = new Document();
        doc.setId(7L);
        doc.setFileName("referto.pdf");
        doc.setContentType("application/pdf");

        when(documentFacade.getDocumentById(anyLong())).thenReturn(doc);
        when(documentFacade.downloadDocumentSecure(anyLong(), anyLong())).thenReturn(fileBytes);

        mockMvc.perform(get("/api/documents/download/7").with(withPtUser))
                .andExpect(status().isOk())
                .andExpect(content().bytes(fileBytes));
    }

    @Test
    @DisplayName("GET /api/documents/download/{id} — 404 quando documento non trovato")
    void downloadFile_notFound_returns404() throws Exception {
        when(documentFacade.getDocumentById(anyLong()))
                .thenThrow(new CustomResourceNotFoundException("Documento", 99L));

        mockMvc.perform(get("/api/documents/download/99").with(withPtUser))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ GET /api/documents/user/{userId}

    @Test
    @DisplayName("GET /api/documents/user/{userId} — 200 con lista documenti")
    void getUserDocuments_returns200() throws Exception {
        DocumentResponse doc = DocumentResponse.builder()
                .id(7L).fileName("referto.pdf").type("MEDICAL_REPORT").uploadDate("2026-05-29")
                .build();
        when(documentFacade.getUserDocumentsDtoSecure(anyLong(), anyLong()))
                .thenReturn(List.of(doc));

        mockMvc.perform(get("/api/documents/user/10").with(withPtUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].type").value("MEDICAL_REPORT"));
    }

    // ------------------------------------------------------------------ GET /api/documents/user/{userId}/type/{type}

    @Test
    @DisplayName("GET /api/documents/user/{userId}/type/{type} — 200 con documenti filtrati per tipo")
    void getUserDocumentsByType_returns200() throws Exception {
        DocumentResponse doc = DocumentResponse.builder()
                .id(8L).fileName("piano.pdf").type("TRAINING_PLAN").uploadDate("2026-05-29")
                .build();
        when(documentFacade.getUserDocumentsByTypeDtoSecure(anyLong(), anyString(), anyLong()))
                .thenReturn(List.of(doc));

        mockMvc.perform(get("/api/documents/user/10/type/TRAINING_PLAN").with(withPtUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TRAINING_PLAN"));
    }

    // ------------------------------------------------------------------ DELETE /api/documents/{id}

    @Test
    @DisplayName("DELETE /api/documents/{id} — 204 dopo eliminazione con successo")
    void deleteDocument_returns204() throws Exception {
        doNothing().when(documentFacade).deleteDocument(anyLong(), anyLong());

        mockMvc.perform(delete("/api/documents/7").with(withPtUser))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/documents/{id} — 404 quando documento non trovato")
    void deleteDocument_notFound_returns404() throws Exception {
        doThrow(new CustomResourceNotFoundException("Documento", 99L))
                .when(documentFacade).deleteDocument(anyLong(), anyLong());

        mockMvc.perform(delete("/api/documents/99").with(withPtUser))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ PUT /api/documents/{id}/notes

    @Test
    @DisplayName("PUT /api/documents/{id}/notes — 200 con note aggiornate")
    void updateNotes_returns200() throws Exception {
        UpdatedNotesResponse updated = UpdatedNotesResponse.builder()
                .id(7L).notes("Note aggiornate")
                .build();
        when(documentFacade.updateNotes(anyLong(), anyString(), anyLong())).thenReturn(updated);

        UpdateNotesRequest req = new UpdateNotesRequest("Note aggiornate");

        mockMvc.perform(put("/api/documents/7/notes")
                        .with(withPtUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Note aggiornate"));
    }

    @Test
    @DisplayName("GET /api/documents/download/{id} — 200 usa application/octet-stream quando contentType è null")
    void downloadFile_nullContentType_usesOctetStream() throws Exception {
        byte[] fileBytes = "binary content".getBytes();
        Document doc = new Document();
        doc.setId(8L);
        doc.setFileName("document.bin");
        doc.setContentType(null);

        when(documentFacade.getDocumentById(anyLong())).thenReturn(doc);
        when(documentFacade.downloadDocumentSecure(anyLong(), anyLong())).thenReturn(fileBytes);

        mockMvc.perform(get("/api/documents/download/8").with(withPtUser))
                .andExpect(status().isOk());
    }
}
