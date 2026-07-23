package pl.salezjanie.most.communication.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.communication.domain.DeviceToken;
import pl.salezjanie.most.communication.domain.Subscription;
import pl.salezjanie.most.communication.infrastructure.DeviceTokenRepository;
import pl.salezjanie.most.communication.infrastructure.SubscriptionRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, DeviceTokenRepository deviceTokenRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public void subscribe(UUID memberId, UUID initiativeId) {
        if (subscriptionRepository.findByMemberIdAndInitiativeId(memberId, initiativeId).isPresent()) {
            return; // Already subscribed
        }
        Subscription subscription = new Subscription(UUID.randomUUID(), memberId, initiativeId, Instant.now());
        subscriptionRepository.save(subscription);
        log.info("Member {} subscribed to initiative {}", memberId, initiativeId);
    }

    public void unsubscribe(UUID memberId, UUID initiativeId) {
        subscriptionRepository.deleteByMemberIdAndInitiativeId(memberId, initiativeId);
        log.info("Member {} unsubscribed from initiative {}", memberId, initiativeId);
    }

    public List<UUID> getMySubscriptions(UUID memberId) {
        return subscriptionRepository.findByMemberId(memberId).stream()
                .map(Subscription::getInitiativeId)
                .toList();
    }

    public void registerDeviceToken(UUID memberId, String token) {
        deviceTokenRepository.findByToken(token).ifPresentOrElse(
                existingToken -> {
                    if (!existingToken.getMemberId().equals(memberId)) {
                        // Token transferred to a new user on the same device?
                        // Simple approach: delete old, create new
                        deviceTokenRepository.delete(existingToken);
                        saveNewToken(memberId, token);
                    }
                },
                () -> saveNewToken(memberId, token)
        );
    }

    private void saveNewToken(UUID memberId, String token) {
        DeviceToken deviceToken = new DeviceToken(UUID.randomUUID(), memberId, token, Instant.now());
        deviceTokenRepository.save(deviceToken);
        log.info("Registered new device token for member {}", memberId);
    }
}
