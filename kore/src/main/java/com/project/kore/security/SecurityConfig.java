package com.project.kore.security;

import com.project.kore.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * Configurazione di Spring Security per l'intera API. La strategia è stateless: nessuna sessione
 * lato server, l'identità viaggia a ogni richiesta dentro il JWT che JwtAuthenticationFilter valida
 * prima di tutto. Le autorizzazioni sono decise per rotta in base al ruolo dell'utente.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) {
        try {
            http
                    // CORS: lasciamo passare solo le origini elencate in cors.allowed-origins (il frontend),
                    // con i metodi usati dall'API e i cookie/credenziali abilitati.
                    .cors(cors -> cors.configurationSource(request -> {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(allowedOrigins);
                        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        config.setAllowedHeaders(List.of("*"));
                        config.setAllowCredentials(true);
                        return config;
                    }))
                    // Niente CSRF: non avendo sessioni né cookie di autenticazione, il token nell'header basta.
                    .csrf(AbstractHttpConfigurer::disable)
                    // Vietiamo di mostrare le pagine in un iframe (clickjacking).
                    .headers(headers -> headers
                            .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                            .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable)
                    )
                    // Mappa delle autorizzazioni. Le regole si valutano in ordine: prima le rotte pubbliche,
                    // poi quelle vincolate a un ruolo, infine il default che pretende un utente autenticato.
                    .authorizeHttpRequests(auth -> auth
                            // Aperte a tutti: login/registrazione, handshake WebSocket, listino piani,
                            // vetrina dei professionisti, recensioni in lettura e candidature di lavoro.
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/ws/**").permitAll()
                            .requestMatchers("/api/plans/**").permitAll()
                            // Gli slot di un professionista li vede solo chi è loggato e può prenotare o gestirli.
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/professionals/*/slots").hasAnyRole(Role.CLIENT.name(), Role.PERSONAL_TRAINER.name(), Role.NUTRITIONIST.name())
                            .requestMatchers("/api/professionals/**").permitAll()
                            .requestMatchers("/api/reviews/professional/**").permitAll()
                            .requestMatchers("/api/job-applications/**").permitAll()
                            // Azioni riservate a un ruolo preciso: chiusura chat ai moderatori, recensioni e
                            // prenotazioni ai clienti, e le aree admin/moderator/insurance ai rispettivi ruoli.
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/chat/*/close").hasAnyRole(Role.MODERATOR.name(), Role.ADMIN.name())
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reviews").hasRole(Role.CLIENT.name())
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/subscriptions/activate").hasRole(Role.CLIENT.name())
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/dashboard").hasRole(Role.CLIENT.name())
                            .requestMatchers("/api/bookings/**").hasRole(Role.CLIENT.name())
                            .requestMatchers( "/api/admin/**").hasRole(Role.ADMIN.name())
                            .requestMatchers("/api/moderator/**").hasAnyRole(Role.MODERATOR.name(),Role.ADMIN.name())
                            .requestMatchers("/api/insurance/**").hasRole(Role.INSURANCE_MANAGER.name())
                            // Tutto il resto richiede comunque un utente autenticato.
                            .anyRequest().authenticated())
                    // Nessuno stato lato server: ogni richiesta si autentica da sola col token.
                    .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    // Il filtro JWT gira prima di quello standard a username/password, così quando si arriva
                    // alle regole qui sopra il SecurityContext è già popolato.
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Manager usato dall'AuthController per autenticare email e password al login.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        try {
            return config.getAuthenticationManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Le password si salvano e si confrontano con BCrypt (hash con salt incorporato).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
