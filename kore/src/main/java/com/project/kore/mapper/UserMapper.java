package com.project.kore.mapper;

import com.project.kore.dto.request.RegisterRequest;
import com.project.kore.dto.response.ClientBasicInfoResponse;
import com.project.kore.dto.response.ProfessionalSummaryDTO;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.Role;
import com.project.kore.model.User;
import com.project.kore.repository.ReviewRepository;
import com.project.kore.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converte gli utenti tra entità e DTO. Per i professionisti aggiunge alla risposta
 * la media voti e il numero di clienti attivi, leggendoli dai repository.
 */
@Component
public class UserMapper {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public UserMapper(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Risposta completa: ai clienti aggiunge i nomi di PT e nutrizionista assegnati,
     * ai professionisti la media voti e il conteggio dei clienti attivi (letti dai repository).
     *
     * @param user l'utente da convertire
     * @return il DTO completo dell'utente
     */
    public UserResponse toUserResponse(User user) {
        Double avgRating = null;
        Integer clientsCount = null;

        // Solo i professionisti hanno rating e clienti da calcolare.
        if (user.getRole() == Role.PERSONAL_TRAINER || user.getRole() == Role.NUTRITIONIST) {
            avgRating = reviewRepository.getAverageRating(user.getId());
            if (avgRating == null) avgRating = 0.0;
            if (user.getRole() == Role.PERSONAL_TRAINER) {
                clientsCount = (int) userRepository.countByAssignedPTAndDeletedFalse(user);
            } else {
                clientsCount = (int) userRepository.countByAssignedNutritionistAndDeletedFalse(user);
            }
        }

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .profilePictureUrl(user.getProfilePicture())
                .assignedPtName(user.getAssignedPT() != null ?
                        user.getAssignedPT().getFullName() : null)
                .assignedNutritionistName(user.getAssignedNutritionist() != null ?
                        user.getAssignedNutritionist().getFullName() : null)
                .activeClientsCount(clientsCount)
                .averageRating(avgRating)
                .build();
    }

    /**
     * Versione leggera senza accessi al DB: per le viste admin/moderator rating e
     * conteggio clienti non servono.
     *
     * @param user l'utente da convertire
     * @return il DTO dell'utente senza dati calcolati
     */
    public UserResponse toAdminResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .profilePictureUrl(user.getProfilePicture())
                .assignedPtName(user.getAssignedPT() != null ?
                        user.getAssignedPT().getFullName() : null)
                .assignedNutritionistName(user.getAssignedNutritionist() != null ?
                        user.getAssignedNutritionist().getFullName() : null)
                .build();
    }

    /**
     * Variante su lista della risposta leggera.
     *
     * @param user gli utenti da convertire (può essere {@code null})
     * @return i DTO degli utenti, lista vuota se l'input è {@code null}
     */
    public List<UserResponse> toAdminResponse(List<User> user) {
        return user==null?new ArrayList<>():user.stream().map(this::toAdminResponse).toList();
    }

    /**
     * Costruisce l'entità utente da una richiesta di registrazione (ruolo forzato a CLIENT).
     *
     * @param request dati di registrazione
     * @return il nuovo utente, oppure {@code null} se la richiesta è {@code null}
     */
    public User toUser(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setProfilePicture(request.profilePicture());
        user.setRole(Role.CLIENT);
        return user;
    }

    /**
     * Converte l'utente nelle sole informazioni essenziali.
     *
     * @param user l'utente da convertire
     * @return il DTO con i dati di base
     */
    public ClientBasicInfoResponse toBasicInfoResponse(User user) {
        return ClientBasicInfoResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePicture())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }

    /**
     * Converte un professionista nel suo riepilogo per liste e dashboard.
     *
     * @param pro il professionista da convertire
     * @return il DTO di riepilogo del professionista
     */
    public ProfessionalSummaryDTO toProfessionalSummary(User pro) {
        return ProfessionalSummaryDTO.builder()
                .id(pro.getId())
                .fullName(pro.getFullName())
                .role(pro.getRole())
                .build();
    }
}