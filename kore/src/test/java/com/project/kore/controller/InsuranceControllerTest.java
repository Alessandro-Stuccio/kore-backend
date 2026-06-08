package com.project.kore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.kore.dto.request.UpdateNotesRequest;
import com.project.kore.dto.response.DocumentResponse;
import com.project.kore.dto.response.DocumentUploadResponse;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.dto.response.UpdatedNotesResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.facade.InsuranceFacade;
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
class InsuranceControllerTest {

    @Mock
    InsuranceFacade insuranceFacade;

    @InjectMocks
    InsuranceController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RequestPostProcessor withInsuranceUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User insurance = new User();
        insurance.setId(6L);
        insurance.setEmail("insurance@test.com");
        insurance.setRole(Role.INSURANCE_MANAGER);
        insurance.setFirstName("Insurance");
        insurance.setLastName("Manager");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                insurance, null, List.of(new SimpleGrantedAuthority("ROLE_INSURANCE_MANAGER")));

        withInsuranceUser = (MockHttpServletRequest request) -> {
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return request;
        };
    }

    // ------------------------------------------------------------------ GET /api/insurance/subscriptions

    @Test
    @DisplayName("GET /api/insurance/subscriptions — 200 con lista abbonamenti")
    void getSubscriptions_returns200() throws Exception {
        SubscriptionResponse sub = SubscriptionResponse.builder()
                .id(1L).userId(10L).userName("Luca Rossi").planName("Premium").active(true)
                .build();
        when(insuranceFacade.getAllSubscriptions()).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/insurance/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].planName").value("Premium"));
    }

    // ------------------------------------------------------------------ GET /api/insurance/clients

    @Test
    @DisplayName("GET /api/insurance/clients — 200 con lista clienti")
    void getClients_returns200() throws Exception {
        UserResponse client = UserResponse.builder()
                .id(10L).firstName("Luca").lastName("Rossi").email("luca@test.com").role(Role.CLIENT)
                .build();
        when(insuranceFacade.getAllClients()).thenReturn(List.of(client));

        mockMvc.perform(get("/api/insurance/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].email").value("luca@test.com"));
    }

    // ------------------------------------------------------------------ GET /api/insurance/chat-contacts

    @Test
    @DisplayName("GET /api/insurance/chat-contacts — 200 con lista contatti")
    void getChatContacts_returns200() throws Exception {
        UserResponse contact = UserResponse.builder()
                .id(1L).firstName("Admin").lastName("User").email("admin@test.com").role(Role.ADMIN)
                .build();
        when(insuranceFacade.getChatContacts()).thenReturn(List.of(contact));

        mockMvc.perform(get("/api/insurance/chat-contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    // ------------------------------------------------------------------ POST /api/insurance/clients/{clientId}/policy

    @Test
    @DisplayName("POST /api/insurance/clients/{clientId}/policy — 200 dopo caricamento polizza")
    void uploadPolicy_returns200() throws Exception {
        DocumentUploadResponse uploadResponse = DocumentUploadResponse.builder()
                .id(9L).fileName("polizza.pdf").type("INSURANCE_POLICY").uploadDate("2026-05-29")
                .build();
        when(insuranceFacade.uploadPolicy(any(), anyLong(), anyLong())).thenReturn(uploadResponse);

        MockMultipartFile file = new MockMultipartFile(
                "file", "polizza.pdf", "application/pdf", "PDF content".getBytes());

        mockMvc.perform(multipart("/api/insurance/clients/10/policy")
                        .file(file)
                        .with(withInsuranceUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.fileName").value("polizza.pdf"));
    }

    // ------------------------------------------------------------------ GET /api/insurance/clients/{clientId}/policies

    @Test
    @DisplayName("GET /api/insurance/clients/{clientId}/policies — 200 con lista polizze")
    void getClientPolicies_returns200() throws Exception {
        DocumentResponse policy = DocumentResponse.builder()
                .id(9L).fileName("polizza.pdf").type("INSURANCE_POLICY").uploadDate("2026-05-29")
                .build();
        when(insuranceFacade.getClientPolicies(anyLong())).thenReturn(List.of(policy));

        mockMvc.perform(get("/api/insurance/clients/10/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].type").value("INSURANCE_POLICY"));
    }

    // ------------------------------------------------------------------ GET /api/insurance/policies/{id}/download

    @Test
    @DisplayName("GET /api/insurance/policies/{id}/download — 200 con byte del file")
    void downloadPolicy_returns200() throws Exception {
        byte[] pdfBytes = "PDF content".getBytes();
        Document doc = new Document();
        doc.setId(9L);
        doc.setFileName("polizza.pdf");
        doc.setContentType("application/pdf");

        when(insuranceFacade.getDocumentById(anyLong())).thenReturn(doc);
        when(insuranceFacade.downloadPolicy(anyLong())).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/insurance/policies/9/download"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    @DisplayName("GET /api/insurance/policies/{id}/download — 404 quando polizza non trovata")
    void downloadPolicy_notFound_returns404() throws Exception {
        when(insuranceFacade.getDocumentById(anyLong()))
                .thenThrow(new CustomResourceNotFoundException("Documento", 99L));

        mockMvc.perform(get("/api/insurance/policies/99/download"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ DELETE /api/insurance/policies/{id}

    @Test
    @DisplayName("DELETE /api/insurance/policies/{id} — 204 dopo eliminazione")
    void deletePolicy_returns204() throws Exception {
        doNothing().when(insuranceFacade).deletePolicy(anyLong());

        mockMvc.perform(delete("/api/insurance/policies/9"))
                .andExpect(status().isNoContent());
    }

    // ------------------------------------------------------------------ PUT /api/insurance/policies/{id}/notes

    @Test
    @DisplayName("PUT /api/insurance/policies/{id}/notes — 200 con note aggiornate")
    void updatePolicyNotes_returns200() throws Exception {
        UpdatedNotesResponse updated = UpdatedNotesResponse.builder()
                .id(9L).notes("Cliente VIP")
                .build();
        when(insuranceFacade.updatePolicyNotes(anyLong(), anyString())).thenReturn(updated);

        UpdateNotesRequest req = new UpdateNotesRequest("Cliente VIP");

        mockMvc.perform(put("/api/insurance/policies/9/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Cliente VIP"));
    }

    @Test
    @DisplayName("GET /api/insurance/policies/{id}/download — 200 usa application/octet-stream quando contentType è null")
    void downloadPolicy_nullContentType_usesOctetStream() throws Exception {
        Document doc = new Document();
        doc.setId(10L);
        doc.setFileName("polizza.bin");
        doc.setContentType(null);

        when(insuranceFacade.getDocumentById(anyLong())).thenReturn(doc);
        when(insuranceFacade.downloadPolicy(anyLong())).thenReturn("content".getBytes());

        mockMvc.perform(get("/api/insurance/policies/10/download"))
                .andExpect(status().isOk());
    }
}
