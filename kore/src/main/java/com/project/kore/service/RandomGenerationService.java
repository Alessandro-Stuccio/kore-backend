package com.project.kore.service;

/** Genera valori casuali, ad esempio i token usati per il reset password. */
public interface RandomGenerationService {

    /**
     * Genera una chiave/token casuale.
     *
     * @return il token generato
     */
    String getTokenKey();
}
