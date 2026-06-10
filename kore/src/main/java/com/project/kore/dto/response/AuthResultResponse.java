package com.project.kore.dto.response;

import com.project.kore.model.User;

/**
 * Risultato interno dell'autenticazione: tiene insieme il token JWT e l'utente, prima di costruire la risposta esposta.
 */
public class AuthResultResponse {

    private String token;
    private User user;


    private AuthResultResponse(Builder b) {
        this.token = b.token;
        this.user = b.user;
    }

    public static Builder builder() { return new Builder(); }

    public String getToken() { return token; }
    public User getUser() { return user; }

    public static class Builder {
        private String token;
        private User user;

        public Builder token(String token) { this.token = token; return this; }
        public Builder user(User user) { this.user = user; return this; }

        public AuthResultResponse build() { return new AuthResultResponse(this); }
    }
}
