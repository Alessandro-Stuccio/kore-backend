package com.project.kore.service.impl;

import com.project.kore.model.Review;
import com.project.kore.model.User;
import com.project.kore.repository.ReviewRepository;
import com.project.kore.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

/** Persistenza e query delle recensioni. */
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review save(Review review) {
        // Invariante relazionale ereditata dal vecchio ReviewBuilder.build().
        if (review.getClient() != null && review.getProfessional() != null
                && review.getClient().getId() != null && review.getProfessional().getId() != null
                && review.getClient().getId().equals(review.getProfessional().getId())) {
            throw new IllegalStateException("client e professional non possono essere lo stesso utente");
        }
        return reviewRepository.save(review);
    }

    @Override
    public boolean existsByClientAndProfessional(Long clientId, Long professionalId) {
        return reviewRepository.existsByClientIdAndProfessionalId(clientId, professionalId);
    }

    @Override
    public List<Review> findByProfessional(User professional) {
        return reviewRepository.findByProfessional(professional);
    }

    @Override
    public double getAverageRating(Long professionalId) {
        Double avg = reviewRepository.getAverageRating(professionalId);
        return avg != null ? avg : 0.0;
    }

}
