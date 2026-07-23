package pl.salezjanie.most.engagement.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.salezjanie.most.engagement.application.EngagementService;
import pl.salezjanie.most.engagement.application.PointsLedgerView;

import java.util.UUID;

@RestController
@RequestMapping("/api/engagement")
public class EngagementController {

    private final EngagementService engagementService;

    public EngagementController(EngagementService engagementService) {
        this.engagementService = engagementService;
    }

    @GetMapping("/me/points")
    public PointsLedgerView getMyPoints(@AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        return engagementService.getMyPoints(memberId);
    }
}
