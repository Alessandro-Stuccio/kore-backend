package com.project.kore.exception.booking;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando un professionista ha già raggiunto il numero massimo di clienti
 * assegnati. Risponde con 422.
 */
public class ProfessionalSoldOutException extends BaseException {

    public ProfessionalSoldOutException(String professionalName) {
        super("Il professionista " + professionalName + " è attualmente Sold Out.", HttpStatus.valueOf(422));
    }
}


