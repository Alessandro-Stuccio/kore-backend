package com.project.kore.facade.impl;

import com.project.kore.dto.request.PlanRequest;
import com.project.kore.dto.request.ProfileUpdateRequest;
import com.project.kore.dto.response.*;
import com.project.kore.enums.Role;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.project.kore.facade.SubscriptionFacade;
import com.project.kore.facade.UserFacade;
import com.project.kore.util.BusinessConstants;
import com.project.kore.mapper.BookingMapper;
import com.project.kore.mapper.SubscriptionMapper;
import com.project.kore.mapper.UserMapper;
import com.project.kore.model.*;
import com.project.kore.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mette insieme i dati di più servizi per la dashboard del cliente e gestisce profilo e abbonamenti.
 */
@Component
public class UserFacadeImpl implements UserFacade {

    private static final Logger log = LoggerFactory.getLogger(UserFacadeImpl.class);

    private final UserService userService;
    private final PlanService planService;
    private final SlotService slotService;
    private final ReviewService reviewService;
    private final SubscriptionService subscriptionService;
    private final UserMapper userMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final BookingMapper bookingMapper;
    private final EmailService emailService;
    private final SubscriptionFacade subscriptionFacade;

    public UserFacadeImpl(UserService userService,
                          PlanService planService,
                          SlotService slotService,
                          ReviewService reviewService,
                          SubscriptionService subscriptionService,
                          UserMapper userMapper,
                          SubscriptionMapper subscriptionMapper,
                          BookingMapper bookingMapper,
                          EmailService emailService,
                          SubscriptionFacade subscriptionFacade) {
        this.userService = userService;
        this.planService = planService;
        this.slotService = slotService;
        this.reviewService = reviewService;
        this.subscriptionService = subscriptionService;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.bookingMapper = bookingMapper;
        this.emailService = emailService;
        this.subscriptionFacade = subscriptionFacade;
    }

    /**
     * Aggiorna solo i campi valorizzati nella richiesta. Se arriva una nuova password la cifra
     * e notifica l'utente via email (l'invio è best-effort e non blocca l'aggiornamento).
     */
    @Override
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userService.getUserById(userId);

        if (request.firstName() != null && !request.firstName().trim().isEmpty())
            user.setFirstName(request.firstName().trim());
        if (request.lastName() != null && !request.lastName().trim().isEmpty())
            user.setLastName(request.lastName().trim());
        if (request.profilePicture() != null && !request.profilePicture().trim().isEmpty())
            user.setProfilePicture(request.profilePicture().trim());
        if (request.password() != null && !request.password().trim().isEmpty()) {
            user.setPassword(userService.encodePassword(request.password().trim()));
            try {
                emailService.sendPasswordChangeEmail(user.getEmail(), user.getFirstName());
            } catch (Exception e) {
                log.warn("Impossibile inviare email cambio password a {}: {}", user.getEmail(), e.getMessage());
            }
        }

        userService.save(user);
    }

    /**
     * Compone la dashboard del cliente: profilo, abbonamento attivo, professionisti assegnati
     * e prossimi appuntamenti. Riservata agli utenti con ruolo CLIENT.
     */
    @Override
    @Transactional(readOnly = true)
    public ClientDashboardResponse getClientDashboard(Long userId) {
        User user = userService.getUserById(userId);
        if (user.getRole() != Role.CLIENT) {
            throw new AccessDeniedException("La dashboard cliente è accessibile solo ai clienti.");
        }

        List<ProfessionalSummaryResponse> followingProfessionals = new ArrayList<>();
        if (user.getAssignedPT() != null)
            followingProfessionals.add(userMapper.toProfessionalSummary(user.getAssignedPT()));
        if (user.getAssignedNutritionist() != null)
            followingProfessionals.add(userMapper.toProfessionalSummary(user.getAssignedNutritionist()));

        SubscriptionResponse subResponse = null;
        try {
            subResponse = subscriptionMapper.toResponse(subscriptionService.getSubscriptionStatus(user));
        } catch (Exception ignored) {}

        List<BookingResponse> upcomingBookings = slotService.findFutureByUser(user, LocalDateTime.now())
                .stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());

        return ClientDashboardResponse.builder()
                .profile(userMapper.toUserResponse(user))
                .followingProfessionals(followingProfessionals)
                .subscription(subResponse)
                .upcomingBookings(upcomingBookings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionalSummaryResponse> findAvailableProfessionals(Role role) {
        return userService.findByRole(role).stream()
                .map(pro -> {
                    double avg = reviewService.getAverageRating(pro.getId());
                    long activeClients = pro.getRole() == Role.PERSONAL_TRAINER
                            ? userService.countByAssignedPT(pro)
                            : userService.countByAssignedNutritionist(pro);
                    return ProfessionalSummaryResponse.builder()
                            .id(pro.getId())
                            .fullName(pro.getFullName())
                            .role(pro.getRole())
                            .averageRating(avg)
                            .currentActiveClients((int) activeClients)
                            .isSoldOut(activeClients >= BusinessConstants.MAX_CLIENTS_PER_PROFESSIONAL)
                            .build();
                })
                .sorted((p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientBasicInfoResponse> getClientsForProfessional(Long professionalId) {
        User professional = userService.getUserById(professionalId);

        List<User> clients;
        if (professional.getRole() == Role.PERSONAL_TRAINER) {
            clients = userService.findByAssignedPT(professional);
        } else if (professional.getRole() == Role.NUTRITIONIST) {
            clients = userService.findByAssignedNutritionist(professional);
        } else {
            throw new IllegalArgumentException("L'utente non è un professionista");
        }

        return clients.stream()
                .map(userMapper::toBasicInfoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientBasicInfoResponse getAdmin() {
        return userService.findByRole(Role.ADMIN).stream().findFirst()
                .map(userMapper::toBasicInfoResponse)
                .orElseThrow(() -> new CustomResourceNotFoundException("Amministratore non trovato nel sistema."));
    }

    /**
     * Attiva un abbonamento per il cliente. Solo i CLIENT possono farlo e non devono già averne
     * uno attivo; l'attivazione vera e propria è delegata a SubscriptionFacade.
     */
    @Override
    @Transactional
    public SubscriptionResponse activateSubscription(PlanRequest request, Long userId) {
        User user = userService.getUserById(userId);
        if (user.getRole() != Role.CLIENT) {
            throw new AccessDeniedException("Solo i clienti possono attivare un abbonamento.");
        }
        if (subscriptionService.findActiveByUser(user).isPresent()) {
            throw new ResourceAlreadyExistsException("L'utente ha già un abbonamento attivo. Contattare l'amministrazione per cambiare piano.");
        }
        Plan plan = planService.getPlanById(request.planId());
        return subscriptionMapper.toResponse(subscriptionFacade.activateSubscription(user, plan, request.paymentFrequency()));
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionStatus(Long userId) {
        User user = userService.getUserById(userId);
        return subscriptionMapper.toResponse(subscriptionService.getSubscriptionStatus(user));
    }

}
