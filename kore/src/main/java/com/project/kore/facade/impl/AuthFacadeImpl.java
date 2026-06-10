package com.project.kore.facade.impl;

import com.project.kore.dto.request.LoginRequest;
import com.project.kore.dto.request.RegisterRequest;
import com.project.kore.dto.response.AuthResultResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.booking.ProfessionalSoldOutException;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import com.project.kore.facade.AuthFacade;
import com.project.kore.util.BusinessConstants;
import com.project.kore.facade.SubscriptionFacade;
import com.project.kore.mapper.UserMapper;
import com.project.kore.model.Plan;
import com.project.kore.model.User;
import com.project.kore.security.JwtUtil;
import com.project.kore.service.EmailService;
import com.project.kore.service.PlanService;
import com.project.kore.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registrazione, login e reset password, coordinando UserService, EmailService e {@link JwtUtil}.
 */
@Component
public class AuthFacadeImpl implements AuthFacade {

    private static final Logger log = LoggerFactory.getLogger(AuthFacadeImpl.class);
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PlanService planService;
    private final SubscriptionFacade subscriptionFacade;

    public AuthFacadeImpl(JwtUtil jwtUtil,
                          UserService userService,
                          EmailService emailService,
                          PasswordEncoder passwordEncoder,
                          UserMapper userMapper,
                          PlanService planService,
                          SubscriptionFacade subscriptionFacade) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.planService = planService;
        this.subscriptionFacade = subscriptionFacade;
    }

    // Crea il cliente, assegna gli eventuali professionisti scelti (con controllo capienza) e,
    // se indicati piano e frequenza, attiva subito l'abbonamento. L'email di benvenuto è best-effort:
    // un errore SMTP non deve far fallire la registrazione.
    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("Utente", "email", request.email());
        }

        User newUser = userMapper.toUser(request);
        newUser.setPassword(userService.encodePassword(request.password()));

        assignProfessional(newUser, request.selectedPtId(), Role.PERSONAL_TRAINER);
        assignProfessional(newUser, request.selectedNutritionistId(), Role.NUTRITIONIST);

        User savedUser = userService.save(newUser);

        if (request.selectedPlanId() != null && request.paymentFrequency() != null) {
            Plan plan = planService.getPlanById(request.selectedPlanId());
            subscriptionFacade.activateSubscription(savedUser, plan, request.paymentFrequency());
        }

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());
        } catch (Exception e) {
            log.warn("Impossibile inviare email di benvenuto a {}: {}", savedUser.getEmail(), e.getMessage());
        }

        return userMapper.toUserResponse(savedUser);
    }

    // Verifica la password e, se combacia, genera il JWT e lo restituisce insieme all'utente.
    @Override
    @Transactional(readOnly = true)
    public AuthResultResponse login(LoginRequest request) {
        User user = userService.getUserByEmail(request.email());
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenziali non valide");
        }
        String jwtToken = jwtUtil.generateToken(user);
        return AuthResultResponse.builder().token(jwtToken).user(user).build();
    }

    // Genera il token di reset (vita breve, 30 min) e lo invia via email. Errori SMTP loggati ma non propagati.
    @Override
    @Transactional(readOnly = true)
    public void forgotPassword(String email) {
        User user = userService.getUserByEmail(email);
        String resetToken = jwtUtil.generatePasswordResetToken(user.getEmail());
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetToken);
        } catch (Exception e) {
            log.error("Errore nell'invio dell'email di reset password a {}: {}", email, e.getMessage());
        }
    }

    // Valida il token di reset (firma e scadenza), salva la nuova password codificata e manda
    // l'email di conferma (best-effort).
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = jwtUtil.validatePasswordResetToken(token);
        User user = userService.getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);
        try {
            emailService.sendPasswordChangeEmail(user.getEmail(), user.getFirstName());
        } catch (Exception e) {
            log.warn("Impossibile inviare email di avvenuto reset password a {}: {}", user.getEmail(), e.getMessage());
        }
        log.info("Password reimpostata con successo per l'utente {}", user.getEmail());
    }

    private void assignProfessional(User user, Long proId, Role expectedRole) {
        if (proId == null) {
            return;
        }
        User professional = userService.getUserById(proId);
        if (professional.getRole() != expectedRole) {
            throw new IllegalArgumentException("L'ID fornito non corrisponde a un " + expectedRole + ".");
        }
        long activeClients = expectedRole == Role.PERSONAL_TRAINER
                ? userService.countByAssignedPT(professional)
                : userService.countByAssignedNutritionist(professional);
        if (activeClients >= BusinessConstants.MAX_CLIENTS_PER_PROFESSIONAL) {
            throw new ProfessionalSoldOutException(professional.getFirstName());
        }
        if (expectedRole == Role.PERSONAL_TRAINER) {
            user.setAssignedPT(professional);
        } else {
            user.setAssignedNutritionist(professional);
        }
    }
}
