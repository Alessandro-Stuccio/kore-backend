package com.project.kore.util;

/**
 * Costanti di business condivise tra i vari layer.
 */
public final class BusinessConstants {

    private BusinessConstants() {}

    public static final int MAX_CLIENTS_PER_PROFESSIONAL = 50;
    public static final int MAX_MESSAGE_LENGTH = 2000;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
}
