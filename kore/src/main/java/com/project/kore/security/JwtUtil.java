package com.project.kore.security;

import com.project.kore.service.RandomGenerationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Genera e valida i token JWT. Gestisce due tipi di token, distinti dal claim
 * {@code purpose}: quello di autenticazione (scadenza configurabile, default 24 h)
 * e quello di reset password (scadenza fissa di 30 minuti).
 */
@Component
public class JwtUtil {



    private static final String PURPOSE_CLAIM = "purpose";
    private static final String PURPOSE_PASSWORD_RESET = "PASSWORD_RESET";
    private static final long PASSWORD_RESET_EXPIRATION_MS = 30 * 60 * 1000L;

    public JwtUtil(RandomGenerationService random) {
        SECRET_KEY =random.getTokenKey();
        LogManager.getLogger(this.getClass()).warn("SECRET_KEY => " + SECRET_KEY);
    }

    private final String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * All'avvio fallisce subito se la chiave di firma non è configurata.
     *
     * @throws IllegalStateException se la chiave segreta è nulla o vuota
     */
    @PostConstruct
    public void validateSecret() {
        if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET non configurata. " +
                "Imposta la variabile d'ambiente JWT_SECRET prima di avviare l'app."
            );
        }
    }

    /**
     * Estrae lo username dal token: il subject è l'email dell'utente.
     *
     * @param token il token JWT
     * @return l'email contenuta nel subject
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Estrae un claim arbitrario dal token applicando il resolver indicato.
     *
     * @param token          il token JWT
     * @param claimsResolver funzione che seleziona il claim dai {@link Claims}
     * @param <T>            tipo del valore estratto
     * @return il valore del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera il token di autenticazione, con scadenza presa da {@code jwt.expiration}.
     *
     * @param userDetails l'utente per cui emettere il token
     * @return il token JWT firmato
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Genera il token per il reset password: claim {@code purpose=PASSWORD_RESET} e validità 30 minuti.
     *
     * @param email email dell'account che richiede il reset
     * @return il token di reset firmato
     */
    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .setClaims(Map.of(PURPOSE_CLAIM, PURPOSE_PASSWORD_RESET))
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + PASSWORD_RESET_EXPIRATION_MS))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Accetta solo i token di reset e ne restituisce l'email.
     *
     * @param token il token da validare
     * @return l'email contenuta nel subject del token di reset
     * @throws IllegalArgumentException se il token non è un token di reset password
     */
    public String validatePasswordResetToken(String token) {
        Claims claims = extractAllClaims(token);
        String purpose = claims.get(PURPOSE_CLAIM, String.class);
        if (!PURPOSE_PASSWORD_RESET.equals(purpose)) {
            throw new IllegalArgumentException("Token non valido per il reset della password.");
        }
        return claims.getSubject();
    }

    /**
     * Verifica che il token appartenga all'utente indicato e non sia scaduto.
     *
     * @param token       il token da validare
     * @param userDetails l'utente atteso
     * @return {@code true} se il token è valido per quell'utente
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
