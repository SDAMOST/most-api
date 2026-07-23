package pl.salezjanie.most.communication.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.salezjanie.most.communication.domain.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByMemberId(UUID memberId);
    List<Subscription> findByInitiativeId(UUID initiativeId);
    Optional<Subscription> findByMemberIdAndInitiativeId(UUID memberId, UUID initiativeId);
    void deleteByMemberIdAndInitiativeId(UUID memberId, UUID initiativeId);
}
