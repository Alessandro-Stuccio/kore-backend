package com.project.kore.controller;

import com.project.kore.dto.response.ActivityFeedItemResponse;
import com.project.kore.facade.ActivityFeedFacade;
import com.project.kore.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Feed delle attività recenti dell'utente. Espone GET /api/activity/feed. */
@RestController
@RequestMapping("/api/activity")
public class ActivityFeedController {

    private final ActivityFeedFacade activityFeedFacade;

    public ActivityFeedController(ActivityFeedFacade activityFeedFacade) {
        this.activityFeedFacade = activityFeedFacade;
    }

    /**
     * Prenotazioni e documenti degli ultimi {@code days} giorni, dal più recente.
     *
     * @param user utente autenticato
     * @param days ampiezza della finestra temporale in giorni (default 14)
     * @param size numero massimo di elementi da restituire (default 15)
     * @return 200 con le attività recenti dell'utente
     */
    @GetMapping("/feed")
    public ResponseEntity<List<ActivityFeedItemResponse>> getActivityFeed(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "14") int days,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(activityFeedFacade.getActivityFeed(user.getId(), days, size));
    }
}
