package com.project.kore.service.impl;

import com.project.kore.model.Review;
import com.project.kore.model.User;
import com.project.kore.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User client;
    private User professional;
    private Review review;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(1L);
        client.setEmail("client@test.com");

        professional = new User();
        professional.setId(2L);
        professional.setEmail("pt@test.com");

        review = new Review();
        review.setId(10L);
        review.setClient(client);
        review.setProfessional(professional);
        review.setRating(5);
        review.setComment("Excellent trainer!");
    }

    // ---- save ----

    @Test
    @DisplayName("save: persists review and returns the saved entity")
    void save_persistsAndReturnsReview() {
        when(reviewRepository.save(review)).thenReturn(review);

        Review result = reviewService.save(review);

        assertThat(result).isSameAs(review);
        verify(reviewRepository).save(review);
    }

    // ---- existsByClientAndProfessional ----

    @Test
    @DisplayName("existsByClientAndProfessional: returns true when review already exists for the pair")
    void existsByClientAndProfessional_reviewExists_returnsTrue() {
        when(reviewRepository.existsByClientIdAndProfessionalId(1L, 2L)).thenReturn(true);

        assertThat(reviewService.existsByClientAndProfessional(1L, 2L)).isTrue();
        verify(reviewRepository).existsByClientIdAndProfessionalId(1L, 2L);
    }

    @Test
    @DisplayName("existsByClientAndProfessional: returns false when no review exists for the pair")
    void existsByClientAndProfessional_noReview_returnsFalse() {
        when(reviewRepository.existsByClientIdAndProfessionalId(1L, 99L)).thenReturn(false);

        assertThat(reviewService.existsByClientAndProfessional(1L, 99L)).isFalse();
    }

    // ---- findByProfessional ----

    @Test
    @DisplayName("findByProfessional: returns all reviews for the given professional")
    void findByProfessional_returnsReviews() {
        when(reviewRepository.findByProfessional(professional)).thenReturn(List.of(review));

        List<Review> result = reviewService.findByProfessional(professional);

        assertThat(result).containsExactly(review);
        verify(reviewRepository).findByProfessional(professional);
    }

    @Test
    @DisplayName("findByProfessional: returns empty list when professional has no reviews")
    void findByProfessional_noReviews_returnsEmpty() {
        when(reviewRepository.findByProfessional(professional)).thenReturn(List.of());

        assertThat(reviewService.findByProfessional(professional)).isEmpty();
    }

    // ---- getAverageRating ----

    @Test
    @DisplayName("getAverageRating: returns average rating from repository when reviews exist")
    void getAverageRating_reviewsExist_returnsAverage() {
        when(reviewRepository.getAverageRating(2L)).thenReturn(4.5);

        double result = reviewService.getAverageRating(2L);

        assertThat(result).isEqualTo(4.5);
        verify(reviewRepository).getAverageRating(2L);
    }

    @Test
    @DisplayName("getAverageRating: returns 0.0 when repository returns null (no reviews yet)")
    void getAverageRating_noReviews_returnsZero() {
        when(reviewRepository.getAverageRating(2L)).thenReturn(null);

        double result = reviewService.getAverageRating(2L);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getAverageRating: returns exact value without rounding for non-integer averages")
    void getAverageRating_nonIntegerAverage_returnsExactValue() {
        when(reviewRepository.getAverageRating(2L)).thenReturn(3.75);

        assertThat(reviewService.getAverageRating(2L)).isEqualTo(3.75);
    }
}
