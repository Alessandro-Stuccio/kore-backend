package com.project.kore.exception.review;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando il cliente non può recensire il professionista, di solito perché è
 * iscritto da meno di un mese o ha già lasciato una recensione. Risponde con 422.
 */
public class ReviewNotAllowedException extends BaseException {

    public ReviewNotAllowedException(String message) {
        super(message, HttpStatus.valueOf(422));
    }
}


