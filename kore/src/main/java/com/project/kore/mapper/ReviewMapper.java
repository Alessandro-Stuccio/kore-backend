package com.project.kore.mapper;

import com.project.kore.dto.response.ReviewResponse;
import com.project.kore.model.Review;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte le recensioni nel DTO mostrato al pubblico.
 */
@Component
public class ReviewMapper {

    /**
     * Converte una recensione nel DTO mostrato sul profilo del professionista.
     *
     * @param review la recensione da convertire
     * @return il DTO della recensione, oppure {@code null} se l'input è {@code null}
     */
    public ReviewResponse toResponse(Review review) {
        if (review == null) return null;
        return ReviewResponse.builder()
                .authorName(review.getClient().getFirstName())
                .rating(review.getRating())
                .comment(review.getComment())
                .date(review.getCreatedAt())
                .build();
    }

    /**
     * Converte una lista di recensioni nei rispettivi DTO.
     *
     * @param reviews le recensioni da convertire
     * @return i DTO delle recensioni
     */
    public List<ReviewResponse> toResponseList(List<Review> reviews) {
        return reviews.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
