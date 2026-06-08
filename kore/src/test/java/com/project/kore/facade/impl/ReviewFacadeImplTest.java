package com.project.kore.facade.impl;

import com.project.kore.dto.request.ReviewRequest;
import com.project.kore.dto.response.ReviewResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import com.project.kore.exception.review.ReviewNotAllowedException;
import com.project.kore.mapper.ReviewMapper;
import com.project.kore.model.Review;
import com.project.kore.model.User;
import com.project.kore.service.ReviewService;
import com.project.kore.service.SlotService;
import com.project.kore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewFacadeImpl unit tests")
class ReviewFacadeImplTest {

    @Mock private UserService userService;
    @Mock private ReviewService reviewService;
    @Mock private SlotService slotService;
    @Mock private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewFacadeImpl reviewFacade;

    private User client;
    private User ptProfessional;
    private User nutriProfessional;
    private Review savedReview;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(1L);
        client.setFirstName("Luca");
        client.setLastName("Bianchi");
        client.setRole(Role.CLIENT);

        ptProfessional = new User();
        ptProfessional.setId(2L);
        ptProfessional.setFirstName("Marco");
        ptProfessional.setLastName("PT");
        ptProfessional.setRole(Role.PERSONAL_TRAINER);

        nutriProfessional = new User();
        nutriProfessional.setId(3L);
        nutriProfessional.setFirstName("Sara");
        nutriProfessional.setLastName("Nutri");
        nutriProfessional.setRole(Role.NUTRITIONIST);

        savedReview = new Review();
        savedReview.setId(100L);
        savedReview.setClient(client);
        savedReview.setProfessional(ptProfessional);
        savedReview.setRating(5);
        savedReview.setComment("ottimo");

        reviewResponse = ReviewResponse.builder()
                .authorName("Luca")
                .rating(5)
                .comment("ottimo")
                .build();
    }

    // ─── addReview ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addReview: review already exists → throws ResourceAlreadyExistsException")
    void addReview_alreadyExists_throwsResourceAlreadyExistsException() {
        ReviewRequest request = new ReviewRequest(2L, 5, "ottimo");

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(ptProfessional);
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> reviewFacade.addReview(request, 1L))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(reviewService, never()).save(any());
    }

    @Test
    @DisplayName("addReview: no prior booking and no assignment → throws ReviewNotAllowedException")
    void addReview_noBookingNoAssignment_throwsReviewNotAllowedException() {
        ReviewRequest request = new ReviewRequest(2L, 5, "ottimo");

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(ptProfessional);
        // existsByClientAndProfessional called first in addReview (duplicate guard)
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        // then called again inside canClientReview
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(false);
        // client has no assigned PT
        client.setAssignedPT(null);

        assertThatThrownBy(() -> reviewFacade.addReview(request, 1L))
                .isInstanceOf(ReviewNotAllowedException.class);

        verify(reviewService, never()).save(any());
    }

    @Test
    @DisplayName("addReview: client has a completed booking → saves and returns review")
    void addReview_hasBooking_savesReview() {
        ReviewRequest request = new ReviewRequest(2L, 5, "ottimo");

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(ptProfessional);
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(true);
        when(reviewService.save(any(Review.class))).thenReturn(savedReview);
        when(reviewMapper.toResponse(savedReview)).thenReturn(reviewResponse);

        ReviewResponse result = reviewFacade.addReview(request, 1L);

        assertThat(result).isEqualTo(reviewResponse);
        verify(reviewService).save(any(Review.class));
    }

    @Test
    @DisplayName("addReview: client is currently assigned to PT → saves and returns review")
    void addReview_assignedToPT_savesReview() {
        ReviewRequest request = new ReviewRequest(2L, 5, "ottimo");

        // Assign the same ptProfessional object so User.equals() works correctly
        client.setAssignedPT(ptProfessional);

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(ptProfessional);
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(false);
        when(reviewService.save(any(Review.class))).thenReturn(savedReview);
        when(reviewMapper.toResponse(savedReview)).thenReturn(reviewResponse);

        ReviewResponse result = reviewFacade.addReview(request, 1L);

        assertThat(result).isEqualTo(reviewResponse);
    }

    @Test
    @DisplayName("addReview: client is currently assigned to Nutritionist → saves and returns review")
    void addReview_assignedToNutri_savesReview() {
        ReviewRequest request = new ReviewRequest(3L, 4, "bravo");

        client.setAssignedNutritionist(nutriProfessional);

        Review nutriReview = new Review();
        nutriReview.setId(101L);
        nutriReview.setClient(client);
        nutriReview.setProfessional(nutriProfessional);
        nutriReview.setRating(4);

        ReviewResponse nutriResponse = ReviewResponse.builder()
                .authorName("Luca").rating(4).comment("bravo").build();

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(3L)).thenReturn(nutriProfessional);
        when(reviewService.existsByClientAndProfessional(1L, 3L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 3L)).thenReturn(false);
        when(reviewService.save(any(Review.class))).thenReturn(nutriReview);
        when(reviewMapper.toResponse(nutriReview)).thenReturn(nutriResponse);

        ReviewResponse result = reviewFacade.addReview(request, 1L);

        assertThat(result).isEqualTo(nutriResponse);
    }

    @Test
    @DisplayName("addReview: saved review preserves rating and comment from request")
    void addReview_reviewBuiltWithCorrectFields() {
        ReviewRequest request = new ReviewRequest(2L, 5, "ottimo");

        client.setAssignedPT(ptProfessional);

        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(ptProfessional);
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(false);
        when(reviewService.save(any(Review.class))).thenReturn(savedReview);
        when(reviewMapper.toResponse(savedReview)).thenReturn(reviewResponse);

        reviewFacade.addReview(request, 1L);

        verify(reviewService).save(argThat(r ->
                r.getRating() == 5
                && "ottimo".equals(r.getComment())
                && r.getClient().equals(client)
                && r.getProfessional().equals(ptProfessional)));
    }

    // ─── getReviewsForProfessional ────────────────────────────────────────────────

    @Test
    @DisplayName("getReviewsForProfessional: returns mapped review list")
    void getReviewsForProfessional_returnsMappedList() {
        List<Review> reviews = List.of(savedReview);
        List<ReviewResponse> responses = List.of(reviewResponse);

        when(userService.getUserById(2L)).thenReturn(ptProfessional);
        when(reviewService.findByProfessional(ptProfessional)).thenReturn(reviews);
        when(reviewMapper.toResponseList(reviews)).thenReturn(responses);

        List<ReviewResponse> result = reviewFacade.getReviewsForProfessional(2L);

        assertThat(result).isEqualTo(responses);
    }

    @Test
    @DisplayName("getReviewsForProfessional: empty list is returned as empty")
    void getReviewsForProfessional_emptyList_returnsEmptyList() {
        when(userService.getUserById(2L)).thenReturn(ptProfessional);
        when(reviewService.findByProfessional(ptProfessional)).thenReturn(List.of());
        when(reviewMapper.toResponseList(List.of())).thenReturn(List.of());

        List<ReviewResponse> result = reviewFacade.getReviewsForProfessional(2L);

        assertThat(result).isEmpty();
    }

    // ─── canClientReview ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("canClientReview: already reviewed → returns false immediately")
    void canClientReview_alreadyReviewed_returnsFalse() {
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(true);

        boolean result = reviewFacade.canClientReview(1L, 2L);

        assertThat(result).isFalse();
        verify(slotService, never()).hasBookingBetween(any(), any());
    }

    @Test
    @DisplayName("canClientReview: not reviewed, has booking → returns true")
    void canClientReview_notReviewedHasBooking_returnsTrue() {
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(true);

        boolean result = reviewFacade.canClientReview(1L, 2L);

        assertThat(result).isTrue();
        verify(userService, never()).getUserById(any());
    }

    @Test
    @DisplayName("canClientReview: not reviewed, no booking, PT assigned → returns true")
    void canClientReview_notReviewedNoBookingPTAssigned_returnsTrue() {
        client.setAssignedPT(ptProfessional);

        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(ptProfessional);

        boolean result = reviewFacade.canClientReview(1L, 2L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canClientReview: not reviewed, no booking, Nutritionist assigned → returns true")
    void canClientReview_notReviewedNoBookingNutriAssigned_returnsTrue() {
        client.setAssignedNutritionist(nutriProfessional);

        when(reviewService.existsByClientAndProfessional(1L, 3L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 3L)).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(3L)).thenReturn(nutriProfessional);

        boolean result = reviewFacade.canClientReview(1L, 3L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canClientReview: not reviewed, no booking, different PT assigned → returns false")
    void canClientReview_notReviewedNoBookingDifferentPTAssigned_returnsFalse() {
        User otherPT = new User();
        otherPT.setId(99L);
        otherPT.setRole(Role.PERSONAL_TRAINER);
        // client is assigned to otherPT, not to ptProfessional (id=2)
        client.setAssignedPT(otherPT);

        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(ptProfessional);

        boolean result = reviewFacade.canClientReview(1L, 2L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canClientReview: not reviewed, no booking, no assignment → returns false")
    void canClientReview_noBookingNoAssignment_returnsFalse() {
        client.setAssignedPT(null);
        client.setAssignedNutritionist(null);

        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 2L)).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(2L)).thenReturn(ptProfessional);

        boolean result = reviewFacade.canClientReview(1L, 2L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canClientReview: professional with role other than PT or NUTRITIONIST → returns false")
    void canClientReview_professionalWithOtherRole_returnsFalse() {
        User admin = new User();
        admin.setId(5L);
        admin.setRole(Role.ADMIN);

        when(reviewService.existsByClientAndProfessional(1L, 5L)).thenReturn(false);
        when(slotService.hasBookingBetween(1L, 5L)).thenReturn(false);
        when(userService.getUserById(1L)).thenReturn(client);
        when(userService.getUserById(5L)).thenReturn(admin);

        boolean result = reviewFacade.canClientReview(1L, 5L);

        assertThat(result).isFalse();
    }

    // ─── hasClientReviewed ────────────────────────────────────────────────────────

    @Test
    @DisplayName("hasClientReviewed: delegates to reviewService and returns true when exists")
    void hasClientReviewed_reviewExists_returnsTrue() {
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(true);

        boolean result = reviewFacade.hasClientReviewed(1L, 2L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("hasClientReviewed: delegates to reviewService and returns false when absent")
    void hasClientReviewed_reviewAbsent_returnsFalse() {
        when(reviewService.existsByClientAndProfessional(1L, 2L)).thenReturn(false);

        boolean result = reviewFacade.hasClientReviewed(1L, 2L);

        assertThat(result).isFalse();
    }
}
