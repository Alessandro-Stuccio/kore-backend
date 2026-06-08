package com.project.kore.enums;

/**
 * Stato di una conversazione chat. Una chat chiusa dal moderatore (CLOSED)
 * resta riapribile dal cliente non appena invia un nuovo messaggio.
 */
public enum ChatStatus {
    OPEN,
    CLOSED
}
