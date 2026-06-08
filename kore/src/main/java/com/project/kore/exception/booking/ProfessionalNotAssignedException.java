package com.project.kore.exception.booking;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando un cliente prova a prenotare con un professionista a cui non è
 * assegnato. Risponde con 403.
 */
public class ProfessionalNotAssignedException extends BaseException {

    public ProfessionalNotAssignedException(String professionalType) {
        super("Non sei assegnato a questo " + professionalType + ".", HttpStatus.FORBIDDEN);
    }

    // Il primo parametro è ignorato: resta per compatibilità con i vecchi call site.
    public ProfessionalNotAssignedException(String professionalType, String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}

