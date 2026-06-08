package com.project.kore.mapper;

import com.project.kore.dto.response.ReviewResponse;
import com.project.kore.model.Review;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewMapperTest {

    private ReviewMapper reviewMapper;

    @BeforeEach
    void setUp() {
        reviewMapper = new ReviewMapper();
    }

    // ---- helpers ----

    private User buildClient(Long id, String firstName, String lastName) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        return u;
    }

    private Review buildReview(Long id, User client, int rating, String comment, LocalDateTime createdAt) {
        Review review = new Review();
        review.setId(id);
        review.setClient(client);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(createdAt);
        return review;
    }

    // ---- toResponse: null guard ----

    @Test
    @DisplayName("toResponse: returns null for null review")
    void toResponse_nullReview_returnsNull() {
        assertThat(reviewMapper.toResponse(null)).isNull();
    }

    // ---- toResponse: field mapping ----

    @Test
    @DisplayName("toResponse: authorName is client firstName")
    void toResponse_authorNameIsClientFirstName() {
        User client = buildClient(1L, "Luca", "Bianchi");
        LocalDateTime now = LocalDateTime.of(2025, 4, 5, 12, 0);
        Review review = buildReview(10L, client, 5, "Ottimo trainer!", now);

        ReviewResponse response = reviewMapper.toResponse(review);

        assertThat(response.getAuthorName()).isEqualTo("Luca");
    }

    @Test
    @DisplayName("toResponse: maps rating correctly")
    void toResponse_mapsRating() {
        User client = buildClient(1L, "Luca", "Bianchi");
        Review review = buildReview(10L, client, 4, "Bravo!", LocalDateTime.now());

        ReviewResponse response = reviewMapper.toResponse(review);

        assertThat(response.getRating()).isEqualTo(4);
    }

    @Test
    @DisplayName("toResponse: maps comment correctly")
    void toResponse_mapsComment() {
        User client = buildClient(2L, "Sara", "Verdi");
        Review review = buildReview(11L, client, 3, "Nella media", LocalDateTime.now());

        ReviewResponse response = reviewMapper.toResponse(review);

        assertThat(response.getComment()).isEqualTo("Nella media");
    }

    @Test
    @DisplayName("toResponse: date is createdAt from review")
    void toResponse_dateIsCreatedAt() {
        User client = buildClient(3L, "Mario", "Neri");
        LocalDateTime createdAt = LocalDateTime.of(2025, 5, 10, 9, 30);
        Review review = buildReview(12L, client, 5, "Eccellente!", createdAt);

        ReviewResponse response = reviewMapper.toResponse(review);

        assertThat(response.getDate()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("toResponse: comment can be null")
    void toResponse_nullComment_commentIsNull() {
        User client = buildClient(4L, "Anna", "Rossi");
        Review review = buildReview(13L, client, 2, null, LocalDateTime.now());

        ReviewResponse response = reviewMapper.toResponse(review);

        assertThat(response.getComment()).isNull();
    }

    @Test
    @DisplayName("toResponse: maps minimum rating of 1")
    void toResponse_minimumRating() {
        User client = buildClient(5L, "Paolo", "Ferrari");
        Review review = buildReview(14L, client, 1, "Pessimo", LocalDateTime.now());

        ReviewResponse response = reviewMapper.toResponse(review);

        assertThat(response.getRating()).isEqualTo(1);
    }

    // ---- toResponseList ----

    @Test
    @DisplayName("toResponseList: maps all reviews in list preserving order")
    void toResponseList_mapsAllReviews() {
        User client1 = buildClient(1L, "Luca", "Bianchi");
        User client2 = buildClient(2L, "Sara", "Verdi");
        Review r1 = buildReview(10L, client1, 5, "Ottimo!", LocalDateTime.now());
        Review r2 = buildReview(11L, client2, 3, "Discreto", LocalDateTime.now());

        List<ReviewResponse> result = reviewMapper.toResponseList(List.of(r1, r2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAuthorName()).isEqualTo("Luca");
        assertThat(result.get(1).getAuthorName()).isEqualTo("Sara");
    }

    @Test
    @DisplayName("toResponseList: returns empty list for empty input")
    void toResponseList_emptyInput_returnsEmptyList() {
        assertThat(reviewMapper.toResponseList(List.of())).isEmpty();
    }

    @Test
    @DisplayName("toResponseList: single-element list maps correctly")
    void toResponseList_singleElement_mapsCorrectly() {
        User client = buildClient(1L, "Luca", "Bianchi");
        Review review = buildReview(10L, client, 5, "Perfetto", LocalDateTime.now());

        List<ReviewResponse> result = reviewMapper.toResponseList(List.of(review));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRating()).isEqualTo(5);
    }
}
