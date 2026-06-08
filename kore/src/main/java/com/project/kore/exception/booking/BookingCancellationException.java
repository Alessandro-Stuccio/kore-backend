package com.project.kore.exception.booking;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando una prenotazione non può essere annullata, ad esempio se lo stato
 * è sbagliato o mancano meno di 24 ore all'appuntamento. Risponde con 400.
 */
public class BookingCancellationException extends BaseException {
    public BookingCancellationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
