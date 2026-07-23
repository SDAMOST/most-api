package pl.salezjanie.most.communication.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.salezjanie.most.communication.domain.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
}
