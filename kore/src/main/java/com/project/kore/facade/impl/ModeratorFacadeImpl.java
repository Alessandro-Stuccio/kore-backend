package com.project.kore.facade.impl;

import com.project.kore.dto.request.ModeratorUserUpdateRequest;
import com.project.kore.dto.request.UserCreateRequest;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.PaymentFrequency;
import com.project.kore.enums.Role;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import org.springframework.security.access.AccessDeniedException;
import com.project.kore.facade.ModeratorFacade;
import com.project.kore.facade.SubscriptionFacade;
import com.project.kore.mapper.SubscriptionMapper;
import com.project.kore.mapper.UserMapper;
import com.project.kore.model.Plan;
import com.project.kore.model.User;
import com.project.kore.service.PlanService;
import com.project.kore.service.SubscriptionService;
import com.project.kore.service.UserService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Operazioni del moderatore su utenti, abbonamenti e chat. Fa anche da base per AdminFacadeImpl,
 * che ne eredita i metodi protected.
 */
@Primary
@Component
public class ModeratorFacadeImpl implements ModeratorFacade {



    protected final SubscriptionMapper subscriptionMapper;
    protected final UserMapper userMapper;
    protected final UserService userService;
    protected final SubscriptionService subscriptionService;
    protected final PlanService planService;
    protected final SubscriptionFacade subscriptionFacade;

    public ModeratorFacadeImpl(UserService userService,
                               SubscriptionService subscriptionService,
                               UserMapper userMapper,
                               SubscriptionMapper subscriptionMapper,
                               PlanService planService,
                               SubscriptionFacade subscriptionFacade) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.userMapper = userMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.planService = planService;
        this.subscriptionFacade = subscriptionFacade;
    }

    /**
     * Utenti gestibili dal chiamante: l'admin li vede tutti, il moderatore solo i ruoli che il suo
     * ruolo gli consente di gestire (vedi Role.getManagebleRoles).
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getManageableUsers(User user) {
        List<User> l=userService.findAll();
        if(user.getRole()== Role.ADMIN)return userMapper.toAdminResponse(l);
        else return l.stream()
                .filter(u -> Role.getManagebleRoles(user.getRole()).contains(u.getRole()))
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getChatContacts() {
        return userService.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.INSURANCE_MANAGER || u.getRole() == Role.MODERATOR)
                .map(userMapper::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionService.getAllSubscriptions().stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    /**
     * Crea un utente solo se il suo ruolo è tra quelli che il chiamante può gestire.
     * La costruzione vera e propria (password, professionisti assegnati, eventuale abbonamento)
     * è in buildAndSaveUser.
     */
    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request, User user) {
        Role targetRole = Role.valueOf(request.role());
        if (!Role.getManagebleRoles(user.getRole()).contains(targetRole)) {
            throw new AccessDeniedException(
                    "Il moderatore non può creare utenti con ruolo " + targetRole + ".");
        }
        return buildAndSaveUser(request, targetRole);
    }

    /**
     * Aggiorna l'utente solo se il chiamante può gestirne il ruolo. Se cambia l'email, ne verifica
     * l'unicità escludendo l'utente stesso.
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long id, ModeratorUserUpdateRequest request, User user) {

        User target = userService.getUserById(id);
        if (!Role.getManagebleRoles(user.getRole()).contains(target.getRole())) {
            throw new AccessDeniedException(
                    "Il moderatore non può modificare utenti con ruolo " + target.getRole() + ".");
        }

        String email = request.email();
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(target.getEmail())) {
            if (userService.existsUserByEmailExcluding(email, id)) {
                throw new ResourceAlreadyExistsException("Utente", "email", email);
            }
            target.setEmail(email);
        }

        applyUserUpdates(target, request);

        return userMapper.toAdminResponse(userService.save(target));
    }

    /**
     * Soft delete di un utente, consentito solo se il chiamante può gestirne il ruolo.
     */
    @Override
    @Transactional
    public void deleteUser(Long id, User user) {

        User target = userService.getUserById(id);
        if (!Role.getManagebleRoles(user.getRole()).contains(target.getRole())) {
            throw new AccessDeniedException(
                    "L'utente " + user.getRole() + " non può eliminare utenti con ruolo " + target.getRole() + ".");
        }

        userService.deleteUser(id);

        // Un utente eliminato non deve più avere un abbonamento attivo: lo disattiviamo così
        // sparisce dalle statistiche (attivi, revenue, popolarità piani, crediti) e dalla home.
        subscriptionService.findActiveByUser(target).ifPresent(sub -> {
            sub.setActive(false);
            subscriptionService.save(sub);
        });
    }

    @Override
    @Transactional
    public SubscriptionResponse updateSubscriptionCredits(Long id, int pt, int nutri) {
        if (pt < 0 || nutri < 0) {
            throw new IllegalArgumentException("I crediti non possono essere negativi.");
        }
        return subscriptionMapper.toResponse(subscriptionService.updateSubscriptionCredits(id, pt, nutri));
    }

    protected UserResponse buildAndSaveUser(UserCreateRequest request, Role targetRole) {
        String email = request.email();
        String firstName = request.firstName();
        String lastName = request.lastName();
        String password = request.password();
        if (email == null || firstName == null || lastName == null || password == null) {
            throw new IllegalArgumentException(
                    "Campi obbligatori mancanti (email, firstName, lastName, password, role).");
        }

        if (userService.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Utente", "email", email);
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(userService.encodePassword(password));
        user.setRole(targetRole);

        if (targetRole == Role.CLIENT) {
            if (request.assignedPTId() != null) {
                User assignedPT = userService.getUserById(request.assignedPTId());
                if (assignedPT.getRole() != Role.PERSONAL_TRAINER) {
                    throw new AccessDeniedException("L'utente assegnato come PT non è un PERSONAL_TRAINER");
                }
                user.setAssignedPT(assignedPT);
            }
            if (request.assignedNutritionistId() != null) {
                User assignedNutri = userService.getUserById(request.assignedNutritionistId());
                if (assignedNutri.getRole() != Role.NUTRITIONIST) {
                    throw new AccessDeniedException("L'utente assegnato come nutrizionista non è un NUTRITIONIST");
                }
                user.setAssignedNutritionist(assignedNutri);
            }
        }

        User savedUser = userService.save(user);

        if (targetRole == Role.CLIENT && request.planId() != null && request.paymentFrequency() != null) {
            PaymentFrequency freq;
            try {
                freq = PaymentFrequency.valueOf(request.paymentFrequency());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Frequenza di pagamento non valida: " + request.paymentFrequency());
            }
            Plan plan = planService.getPlanById(request.planId());
            subscriptionFacade.activateSubscription(savedUser, plan, freq);
        }

        return userMapper.toAdminResponse(savedUser);
    }

    protected void applyUserUpdates(User target, ModeratorUserUpdateRequest request) {
        String firstName = request.firstName();
        if (firstName != null && !firstName.isBlank()) target.setFirstName(firstName);

        String lastName = request.lastName();
        if (lastName != null && !lastName.isBlank()) target.setLastName(lastName);

        String password = request.password();
        if (password != null && !password.isBlank()) target.setPassword(userService.encodePassword(password));

    }
}
