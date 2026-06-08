package com.project.kore.exception.booking;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando l'utente prova a prenotare senza un abbonamento attivo (o scaduto).
 * Risponde con 400.
 */
public class SubscriptionExpiredException extends BaseException {
    public SubscriptionExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
