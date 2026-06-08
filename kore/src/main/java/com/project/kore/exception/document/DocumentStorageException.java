package com.project.kore.exception.document;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando qualcosa va storto nell'I/O su filesystem mentre si salva o si scarica
 * un documento. Risponde con 500.
 */
public class DocumentStorageException extends BaseException {

    public DocumentStorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Variante che porta con sé l'eccezione originale (tipicamente una IOException).
    public DocumentStorageException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}

