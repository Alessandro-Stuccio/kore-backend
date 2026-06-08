package com.project.kore.exception.common;

import org.springframework.http.HttpStatus;

/**
 * La si lancia quando una richiesta valida nella forma viola una regola di dominio che non
 * ha un'eccezione dedicata. È il fallback generico per la business logic. Risponde con 422.
 */
public class BusinessLogicException extends BaseException {

    public BusinessLogicException(String message) {
        super(message, HttpStatus.valueOf(422));
    }
}


