package com.project.kore.controller;

import com.project.kore.dto.request.ReviewRequest;
import com.project.kore.dto.response.ReviewResponse;
import com.project.kore.facade.ReviewFacade;
import com.project.kore.model.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Recensioni dei professionisti. /api/reviews. */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewFacade reviewFacade;

    public ReviewController(ReviewFacade reviewFacade) {
        this.reviewFacade = reviewFacade;
    }

    /**
     * Recensione (1-5 stelle) di un cliente verso un professionista. Solo chi ha già prenotato
     * con quel professionista e non l'ha ancora recensito può farlo.
     *
     * @param request dati della recensione (professionista, voto, commento)
     * @param user    cliente autenticato che recensisce
     * @return 200 con la recensione registrata
     */
    @PostMapping
    public ResponseEntity<ReviewResponse> addReview(@Valid @RequestBody ReviewRequest request,
                                                     @AuthenticationPrincipal User user) {
        log.info("Aggiunta recensione per professionista {} da utente {}", request.professionalId(), user.getId());
        return ResponseEntity.ok(reviewFacade.addReview(request, user.getId()));
    }

    /**
     * Tutte le recensioni ricevute dal professionista indicato.
     *
     * @param professionalId id del professionista
     * @return 200 con le sue recensioni
     */
    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForProfessional(@PathVariable Long professionalId) {
        return ResponseEntity.ok(reviewFacade.getReviewsForProfessional(professionalId));
    }

    /**
     * Dice al frontend se l'utente può recensire il professionista e se lo ha già fatto.
     * Restituisce i due flag canReview e hasReviewed.
     *
     * @param user           cliente autenticato
     * @param professionalId id del professionista da valutare
     * @return 200 con i flag {@code canReview} e {@code hasReviewed}
     */
    @GetMapping("/can-review")
    public ResponseEntity<Map<String, Object>> canReview(@AuthenticationPrincipal User user,
                                                          @RequestParam Long professionalId) {
        boolean hasReviewed = reviewFacade.hasClientReviewed(user.getId(), professionalId);
        boolean can = !hasReviewed && reviewFacade.canClientReview(user.getId(), professionalId);
        return ResponseEntity.ok(Map.of("canReview", can, "hasReviewed", hasReviewed));
    }
}
