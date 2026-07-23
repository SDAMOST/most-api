package pl.salezjanie.most.communication.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.salezjanie.most.communication.application.NotificationService;
import pl.salezjanie.most.communication.application.SubscriptionService;
import pl.salezjanie.most.communication.domain.Notification;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/communication")
public class CommunicationController {

    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;

    public CommunicationController(SubscriptionService subscriptionService, NotificationService notificationService) {
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
    }

    @PostMapping("/subscriptions/{initiativeId}")
    public ResponseEntity<Void> subscribe(@PathVariable UUID initiativeId, @AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        subscriptionService.subscribe(memberId, initiativeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/subscriptions/{initiativeId}")
    public ResponseEntity<Void> unsubscribe(@PathVariable UUID initiativeId, @AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        subscriptionService.unsubscribe(memberId, initiativeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/device-tokens")
    public ResponseEntity<Void> registerToken(@RequestBody DeviceTokenRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        subscriptionService.registerDeviceToken(memberId, request.token());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notifications")
    public List<NotificationView> getNotifications(@AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        return notificationService.getMyNotifications(memberId).stream()
                .map(n -> new NotificationView(n.getId(), n.getTitle(), n.getContent(), n.isRead(), n.getCreatedAt().toString()))
                .toList();
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        notificationService.markAsRead(id, memberId);
        return ResponseEntity.ok().build();
    }

    record DeviceTokenRequest(String token) {}
    record NotificationView(UUID id, String title, String content, boolean read, String createdAt) {}
}
