package com.project.kore.exception.common;

import org.springframework.http.HttpStatus;

/**
 * La si lancia quando un'entità cercata per ID o per un altro campo non esiste nel
 * database. Risponde con 404.
 */
public class CustomResourceNotFoundException extends BaseException {

    public CustomResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    // Compone da sé il messaggio a partire da tipo di risorsa e ID mancante.
    public CustomResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " con ID " + id + " non trovato.", HttpStatus.NOT_FOUND);
    }

    // Compone da sé il messaggio a partire da tipo di risorsa, campo e valore cercato.
    public CustomResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(resourceName + " con " + fieldName + " '" + fieldValue + "' non trovato.", HttpStatus.NOT_FOUND);
    }
}

