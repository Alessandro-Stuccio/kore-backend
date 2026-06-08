package com.project.kore.exception;

import com.project.kore.exception.common.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Punto unico in cui finiscono tutte le eccezioni dell'app, standard e custom. Invece di
 * riempire i controller di try-catch, qui le traduciamo tutte in un ErrorResponse coerente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Le nostre eccezioni di dominio: usano lo status che si portano dietro. I 5xx li
    // logghiamo come error con stack trace, il resto come semplice warn.
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        if (status.is5xxServerError()) {
            log.error("Errore interno: {} — Path: {}", ex.getMessage(), request.getRequestURI(), ex);
        } else {
            log.warn("Eccezione business [{}]: {} — Path: {}", status.value(), ex.getMessage(), request.getRequestURI());
        }
        return buildErrorResponse(ex.getMessage(), status, request);
    }

    // Login con credenziali sbagliate: 401, con messaggio generico per non rivelare dettagli.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(HttpServletRequest request) {
        log.warn("Tentativo di login fallito — Path: {}", request.getRequestURI());
        return buildErrorResponse("Email o password non validi", HttpStatus.UNAUTHORIZED, request);
    }

    // Utente autenticato ma senza i permessi richiesti: 403.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Accesso negato — Path: {}", request.getRequestURI());
        String message = (ex.getMessage() == null || ex.getMessage().isBlank())
                ? "Non hai i permessi per accedere a questa risorsa"
                : ex.getMessage();
        return buildErrorResponse(message, HttpStatus.FORBIDDEN, request);
    }

    // @Valid sui body fallito: 400 con la mappa campo → messaggio per il frontend.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Errore di validazione")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Argomento non valido arrivato fino qui: 400.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Argomento non valido: {} — Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    // Operazione richiesta in uno stato incompatibile: la trattiamo come conflitto, 409.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.warn("Stato non valido: {} — Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    // Upload oltre il limite di dimensione: 413 Payload Too Large.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(413);
        return buildErrorResponse("File troppo grande", status, request);
    }

    // Richiesta verso un endpoint inesistente: 404.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(HttpServletRequest request) {
        return buildErrorResponse("Endpoint non trovato: " + request.getRequestURI(), HttpStatus.NOT_FOUND, request);
    }

    // Rete di sicurezza per tutto ciò che non abbiamo previsto: logghiamo lo stack e
    // rispondiamo 500 con un messaggio generico, senza esporre dettagli interni.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
                log.error("Errore imprevisto — Path: {} — Tipo: {}", request.getRequestURI(), ex.getClass().getSimpleName(), ex);
        return buildErrorResponse(
                "Errore interno",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    // Vincoli violati su parametri/path (validazione a livello di metodo): 400 con il
    // dettaglio dei singoli campi.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String field = cv.getPropertyPath().toString();
            errors.put(field, cv.getMessage());
        });
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Violazione vincoli")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Due richieste hanno toccato lo stesso record insieme (optimistic locking): 409,
    // invitando l'utente a riprovare.
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(org.springframework.orm.ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        log.warn("Conflitto di concorrenza su record (Optimistic Locking): {} — Path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse("Slot non più disponibile, riprova", HttpStatus.CONFLICT, request);
    }

    // Confeziona l'ErrorResponse comune a quasi tutti gli handler (senza errori di campo).
    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(error, status);
    }


}