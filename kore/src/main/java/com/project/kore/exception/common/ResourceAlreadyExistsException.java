package com.project.kore.exception.common;

import org.springframework.http.HttpStatus;

/**
 * La si lancia quando si cerca di creare un'entità che viola un vincolo di unicità, come
 * un'email già registrata o un piano con nome duplicato. Risponde con 409.
 */
public class ResourceAlreadyExistsException extends BaseException {

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    // Compone da sé il messaggio a partire da tipo di risorsa, campo e valore duplicato.
    public ResourceAlreadyExistsException(String resourceName, String fieldName, String fieldValue) {
        super(resourceName + " con " + fieldName + " '" + fieldValue + "' esiste già.", HttpStatus.CONFLICT);
    }
}

