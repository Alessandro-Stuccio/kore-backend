package com.project.kore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.kore.dto.request.JobApplicationRequest;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobApplicationControllerTest {

    @Mock
    EmailService emailService;

    @InjectMocks
    JobApplicationController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/job-applications — 200 candidatura inviata con successo")
    void submitApplication_withCv_returns200() throws Exception {
        doNothing().when(emailService).sendJobApplication(any(), any());

        JobApplicationRequest jobRequest = new JobApplicationRequest(
                "Mario", "Rossi", "mario.rossi@email.com",
                "Personal Trainer", "Sono un professionista con 5 anni di esperienza");

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(jobRequest));

        MockMultipartFile cvPart = new MockMultipartFile(
                "cv", "cv.pdf", "application/pdf", "PDF content".getBytes());

        mockMvc.perform(multipart("/api/job-applications")
                        .file(dataPart)
                        .file(cvPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Candidatura inviata con successo! Verrai contattato a breve."));
    }

    @Test
    @DisplayName("POST /api/job-applications — 200 candidatura senza CV allegato")
    void submitApplication_withoutCv_returns200() throws Exception {
        doNothing().when(emailService).sendJobApplication(any(), any());

        JobApplicationRequest jobRequest = new JobApplicationRequest(
                "Lucia", "Bianchi", "lucia@email.com",
                "Nutritionist", "Vorrei candidarmi come nutrizionista");

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(jobRequest));

        mockMvc.perform(multipart("/api/job-applications").file(dataPart))
                .andExpect(status().isOk());
    }
}
