package com.project.kore.controller;

import com.project.kore.dto.request.ReviewRequest;
import com.project.kore.dto.response.ReviewResponse;
import com.project.kore.enums.Role;
import com.project.kore.facade.ReviewFacade;
import com.project.kore.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock private ReviewFacade reviewFacade;

    @InjectMocks
    private ReviewController reviewController;

    @Test
    @DisplayName("addReview — restituisce 200 con la recensione creata")
    void addReview() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("testpass");
        mockUser.setRole(Role.CLIENT);
        ReviewRequest req = new ReviewRequest(2L, 5, null);
        ReviewResponse resp = ReviewResponse.builder().rating(5).authorName("Mario").build();
        when(reviewFacade.addReview(req, 1L)).thenReturn(resp);

        ResponseEntity<ReviewResponse> response = reviewController.addReview(req, mockUser);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("getReviewsForProfessional — restituisce lista recensioni")
    void getReviewsForProfessional() {
        ReviewResponse r = ReviewResponse.builder().rating(4).build();
        when(reviewFacade.getReviewsForProfessional(2L)).thenReturn(List.of(r));

        ResponseEntity<List<ReviewResponse>> response = reviewController.getReviewsForProfessional(2L);

        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("canReview — true quando può recensire e non ha ancora recensito")
    void canReview_canReview() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("testpass");
        mockUser.setRole(Role.CLIENT);
        when(reviewFacade.hasClientReviewed(1L, 2L)).thenReturn(false);
        when(reviewFacade.canClientReview(1L, 2L)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = reviewController.canReview(mockUser, 2L);

        assertThat(response.getBody().get("canReview")).isEqualTo(true);
        assertThat(response.getBody().get("hasReviewed")).isEqualTo(false);
    }

    @Test
    @DisplayName("canReview — false quando ha già recensito")
    void canReview_alreadyReviewed() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("testpass");
        mockUser.setRole(Role.CLIENT);
        when(reviewFacade.hasClientReviewed(1L, 2L)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = reviewController.canReview(mockUser, 2L);

        assertThat(response.getBody().get("canReview")).isEqualTo(false);
        assertThat(response.getBody().get("hasReviewed")).isEqualTo(true);
    }
}
