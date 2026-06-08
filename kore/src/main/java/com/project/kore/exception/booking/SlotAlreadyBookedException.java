package com.project.kore.exception.booking;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando lo slot è stato preso da un altro utente nel frattempo, o non è più
 * disponibile a causa dell'optimistic locking. Risponde con 409.
 */
public class SlotAlreadyBookedException extends BaseException {

    public SlotAlreadyBookedException() {
        super("Lo slot selezionato non è più disponibile.", HttpStatus.CONFLICT);
    }

    public SlotAlreadyBookedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}