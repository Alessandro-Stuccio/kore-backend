package com.project.kore.exception.common;

import org.springframework.http.HttpStatus;

/**
 * Radice di tutte le eccezioni custom dell'app. L'idea è che ogni eccezione si porta dietro
 * il proprio {@link HttpStatus}, così il GlobalExceptionHandler sa subito con che codice
 * rispondere senza doverlo dedurre.
 */
public abstract class BaseException extends RuntimeException {

    // Lo status HTTP che il client riceverà (404, 409, 422, ...).
    private final HttpStatus status;

    protected BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    // Variante che conserva la causa originale, utile per il logging.
    protected BaseException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

