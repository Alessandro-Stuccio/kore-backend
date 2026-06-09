package com.project.kore.facade.impl;

import com.project.kore.dto.request.ReviewRequest;
import com.project.kore.dto.response.ReviewResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import com.project.kore.exception.review.ReviewNotAllowedException;
import com.project.kore.facade.ReviewFacade;
import com.project.kore.mapper.ReviewMapper;
import com.project.kore.model.Review;
import com.project.kore.model.User;
import com.project.kore.service.ReviewService;
import com.project.kore.service.SlotService;
import com.project.kore.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestisce le recensioni dei professionisti, controllando i prerequisiti prima di salvarle.
 */
@Component
public class ReviewFacadeImpl implements ReviewFacade {

    private final UserService userService;
    private final ReviewService reviewService;
    private final SlotService slotService;
    private final ReviewMapper reviewMapper;

    public ReviewFacadeImpl(UserService userService, ReviewService reviewService,
                            SlotService slotService, ReviewMapper reviewMapper) {
        this.userService = userService;
        this.reviewService = reviewService;
        this.slotService = slotService;
        this.reviewMapper = reviewMapper;
    }

    /**
     * Salva una recensione, ma solo se il cliente non ne ha già lasciata una per quel
     * professionista e ha avuto con lui un rapporto formale (una prenotazione o un'assegnazione attiva).
     */
    @Override
    @Transactional
    public ReviewResponse addReview(ReviewRequest request, Long userId) {
        User user = userService.getUserById(userId);
        User professional = userService.getUserById(request.professionalId());

        if (reviewService.existsByClientAndProfessional(user.getId(), professional.getId())) {
            throw new ResourceAlreadyExistsException("Hai già lasciato una recensione per questo professionista.");
        }
        if (!this.canClientReview(user.getId(), professional.getId())) {
            throw new ReviewNotAllowedException(
                    "Puoi recensire solo professionisti con cui hai avuto un rapporto formale.");
        }

        Review review = new Review();
        review.setClient(user);
        review.setProfessional(professional);
        review.setRating(request.rating());
        review.setComment(request.comment());

        return reviewMapper.toResponse(reviewService.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForProfessional(Long professionalId) {
        User professional = userService.getUserById(professionalId);
        return reviewMapper.toResponseList(reviewService.findByProfessional(professional));
    }

    // Niente recensione se ne esiste già una; altrimenti serve almeno una prenotazione passata
    // o un'assegnazione attiva con quel professionista.
    @Override
    @Transactional(readOnly = true)
    public boolean canClientReview(Long clientId, Long professionalId) {
        if (reviewService.existsByClientAndProfessional(clientId, professionalId)) return false;
        if (slotService.hasBookingBetween(clientId, professionalId)) return true;
        User client = userService.getUserById(clientId);
        User professional = userService.getUserById(professionalId);
        return isCurrentlyAssigned(client, professional);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasClientReviewed(Long clientId, Long professionalId) {
        return reviewService.existsByClientAndProfessional(clientId, professionalId);
    }

    private boolean isCurrentlyAssigned(User client, User professional) {
        if (professional.getRole() == Role.PERSONAL_TRAINER)
            return professional.equals(client.getAssignedPT());
        if (professional.getRole() == Role.NUTRITIONIST)
            return professional.equals(client.getAssignedNutritionist());
        return false;
    }
}
