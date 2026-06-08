package com.project.kore.exception.document;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando il file caricato non va bene: estensione mancante, formato non
 * supportato o tipo di documento incoerente col ruolo di chi carica (es. un PT che carica
 * un piano alimentare). Risponde con 400.
 */
public class InvalidFileException extends BaseException {

    public InvalidFileException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

