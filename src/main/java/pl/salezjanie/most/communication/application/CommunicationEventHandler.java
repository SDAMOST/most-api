package pl.salezjanie.most.communication.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.salezjanie.most.activities.domain.Initiative;
import pl.salezjanie.most.activities.domain.InitiativeRepository;
import pl.salezjanie.most.activities.domain.OccurrenceCancelledEvent;
import pl.salezjanie.most.activities.domain.OccurrencePublishedEvent;
import pl.salezjanie.most.activities.domain.OccurrenceRescheduledEvent;
import pl.salezjanie.most.communication.domain.DeviceToken;
import pl.salezjanie.most.communication.domain.Notification;
import pl.salezjanie.most.communication.domain.PushNotificationSender;
import pl.salezjanie.most.communication.domain.Subscription;
import pl.salezjanie.most.communication.infrastructure.DeviceTokenRepository;
import pl.salezjanie.most.communication.infrastructure.NotificationRepository;
import pl.salezjanie.most.communication.infrastructure.SubscriptionRepository;
import pl.salezjanie.most.participation.domain.Enrollment;
import pl.salezjanie.most.participation.domain.EnrollmentRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CommunicationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(CommunicationEventHandler.class);

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationSender pushSender;
    private final EnrollmentRepository enrollmentRepository;
    private final InitiativeRepository initiativeRepository;

    public CommunicationEventHandler(SubscriptionRepository subscriptionRepository,
                                     NotificationRepository notificationRepository,
                                     DeviceTokenRepository deviceTokenRepository,
                                     PushNotificationSender pushSender,
                                     EnrollmentRepository enrollmentRepository,
                                     InitiativeRepository initiativeRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.notificationRepository = notificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.pushSender = pushSender;
        this.enrollmentRepository = enrollmentRepository;
        this.initiativeRepository = initiativeRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePublished(OccurrencePublishedEvent event) {
        Initiative initiative = initiativeRepository.findById(event.initiativeId()).orElseThrow();
        List<UUID> memberIds = subscriptionRepository.findByInitiativeId(event.initiativeId())
                .stream()
                .map(Subscription::getMemberId)
                .toList();

        String title = "New Occurrence Published";
        String body = "A new occurrence for " + initiative.getName() + " is now available for enrollment.";
        
        createAndSend(memberIds, title, body);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCancelled(OccurrenceCancelledEvent event) {
        Initiative initiative = initiativeRepository.findById(event.initiativeId()).orElseThrow();
        Set<UUID> memberIds = getSubscribersAndEnrolled(event.occurrenceId(), event.initiativeId());

        String title = "Occurrence Cancelled";
        String body = "An occurrence for " + initiative.getName() + " has been cancelled.";
        
        createAndSend(memberIds.stream().toList(), title, body);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRescheduled(OccurrenceRescheduledEvent event) {
        Initiative initiative = initiativeRepository.findById(event.initiativeId()).orElseThrow();
        Set<UUID> memberIds = getSubscribersAndEnrolled(event.occurrenceId(), event.initiativeId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String newStartStr = event.newStart().format(formatter);
        
        String title = "Occurrence Rescheduled";
        String body = "An occurrence for " + initiative.getName() + " has been moved to " + newStartStr + ". Reason: " + event.reason();
        
        createAndSend(memberIds.stream().toList(), title, body);
    }

    private Set<UUID> getSubscribersAndEnrolled(UUID occurrenceId, UUID initiativeId) {
        Set<UUID> targetMembers = new HashSet<>();
        
        subscriptionRepository.findByInitiativeId(initiativeId).forEach(sub -> targetMembers.add(sub.getMemberId()));
        
        enrollmentRepository.findByOccurrenceId(occurrenceId).stream()
                .filter(e -> e.getStatus() == pl.salezjanie.most.participation.domain.EnrollmentStatus.ENROLLED)
                .forEach(e -> targetMembers.add(e.getMemberId()));
                
        return targetMembers;
    }

    private void createAndSend(List<UUID> memberIds, String title, String body) {
        if (memberIds.isEmpty()) return;

        Instant now = Instant.now();
        List<Notification> notifications = memberIds.stream()
                .map(mId -> new Notification(UUID.randomUUID(), mId, title, body, now))
                .toList();
                
        notificationRepository.saveAll(notifications);
        
        List<String> tokens = deviceTokenRepository.findByMemberIdIn(memberIds)
                .stream()
                .map(DeviceToken::getToken)
                .toList();
                
        pushSender.sendPushNotification(tokens, title, body);
        log.info("Sent notifications '{}' to {} members.", title, memberIds.size());
    }
}
