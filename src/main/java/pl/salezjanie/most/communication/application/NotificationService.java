package pl.salezjanie.most.communication.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.communication.domain.Notification;
import pl.salezjanie.most.communication.infrastructure.NotificationRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications(UUID memberId) {
        return notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    public void markAsRead(UUID notificationId, UUID memberId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getMemberId().equals(memberId)) {
                notification.markAsRead();
                notificationRepository.save(notification);
            }
        });
    }
}
