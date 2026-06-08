package com.project.kore.exception;

import com.project.kore.exception.common.CustomResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import org.springframework.security.access.AccessDeniedException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MethodArgumentNotValidException manve;

    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private ConstraintViolation<?> constraintViolation;

    @Mock
    private Path constraintPath;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("GET", "/api/test");
    }

    @Test
    @DisplayName("handleBaseException — ResourceNotFoundException (404) restituisce 404")
    void handleBaseException_resourceNotFoundException_returns404() {
        CustomResourceNotFoundException ex = new CustomResourceNotFoundException("Risorsa non trovata");
        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("handleBaseException — messaggio nell'ErrorResponse corrisponde")
    void handleBaseException_errorResponseContainsMessage() {
        CustomResourceNotFoundException ex = new CustomResourceNotFoundException("Utente non trovato");
        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex, request);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Utente non trovato");
    }

    @Test
    @DisplayName("handleBadCredentials — restituisce 401")
    void handleBadCredentials_returns401() {
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(request);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("handleAccessDenied — restituisce 403")
    void handleAccessDenied_returns403() {
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(new AccessDeniedException("Accesso negato"), request);
        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    @DisplayName("handleValidationExceptions — restituisce 400 con errori campo")
    void handleValidationExceptions_returns400WithFieldErrors() {
        FieldError fieldError = new FieldError("dto", "email", "non deve essere vuoto");
        when(manve.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(manve, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).containsKey("email");
    }

    @Test
    @DisplayName("handleIllegalArgument — restituisce 400")
    void handleIllegalArgument_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("argomento non valido");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("handleIllegalState — restituisce 409")
    void handleIllegalState_returns409() {
        IllegalStateException ex = new IllegalStateException("stato non valido");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex, request);
        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @DisplayName("handleMaxUploadSize — restituisce 413")
    void handleMaxUploadSize_returns413() {
        ResponseEntity<ErrorResponse> response = handler.handleMaxUploadSize(request);
        assertThat(response.getStatusCode().value()).isEqualTo(413);
    }

    @Test
    @DisplayName("handleNoResourceFound — restituisce 404")
    void handleNoResourceFound_returns404() {
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(request);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("handleGlobalException — restituisce 500")
    void handleGlobalException_returns500() throws Exception {
        Exception ex = new Exception("errore imprevisto");
        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, request);
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    @DisplayName("handleConstraintViolation — restituisce 400 con errori di violazione")
    void handleConstraintViolation_returns400() {
        when(constraintPath.toString()).thenReturn("campo");
        when(constraintViolation.getPropertyPath()).thenReturn(constraintPath);
        when(constraintViolation.getMessage()).thenReturn("non deve essere nullo");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(constraintViolation);
        when(constraintViolationException.getConstraintViolations()).thenReturn(violations);

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(constraintViolationException, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).containsKey("campo");
    }

    @Test
    @DisplayName("handleOptimisticLockingFailure — restituisce 409")
    void handleOptimisticLockingFailure_returns409() {
        ObjectOptimisticLockingFailureException ex =
                new ObjectOptimisticLockingFailureException(Object.class, 1L);

        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLockingFailure(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @DisplayName("handleBadCredentials — ErrorResponse contiene path corretto")
    void handleBadCredentials_errorResponseContainsPath() {
        request.setRequestURI("/api/auth/login");
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(request);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/auth/login");
    }
}
