package com.project.kore.exception.chat;

import com.project.kore.exception.common.BaseException;
import org.springframework.http.HttpStatus;

/**
 * La si lancia quando un utente prova a scrivere a qualcuno con cui non gli è permesso
 * chattare. La chat vale solo tra un cliente e un suo professionista, o con l'Admin per il
 * supporto. Risponde con 403.
 */
public class ChatNotAllowedException extends BaseException {

    public ChatNotAllowedException() {
        super("La chat è permessa solo tra un cliente e un professionista a lui assegnato.", HttpStatus.FORBIDDEN);
    }

    public ChatNotAllowedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}

