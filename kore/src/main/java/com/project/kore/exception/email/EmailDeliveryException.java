package com.project.kore.exception.email;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando l'invio di un'email tramite il provider esterno fallisce. Risponde
 * con 503.
 */
public class EmailDeliveryException extends BaseException {

    public EmailDeliveryException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // Accoda al messaggio la risposta grezza del provider, se presente.
    public EmailDeliveryException(String message, String providerBody) {
        super(providerBody == null || providerBody.isBlank() ? message : message + " - " + providerBody,
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, cause);
    }
}



