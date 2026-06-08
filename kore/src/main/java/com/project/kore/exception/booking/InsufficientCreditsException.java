package com.project.kore.exception.booking;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando il cliente ha esaurito i crediti del mese per un certo tipo di
 * professionista (es. tutte le sedute PT già usate). Risponde con 422.
 */
public class InsufficientCreditsException extends BaseException {

    public InsufficientCreditsException(String professionalType) {
        super("Crediti " + professionalType + " esauriti. Aggiorna il tuo abbonamento per continuare.", HttpStatus.valueOf(422));
    }
}


