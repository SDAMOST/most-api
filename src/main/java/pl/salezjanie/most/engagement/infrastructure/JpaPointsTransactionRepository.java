package pl.salezjanie.most.engagement.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.salezjanie.most.engagement.domain.PointsTransaction;

import java.util.List;
import java.util.UUID;

public interface JpaPointsTransactionRepository extends JpaRepository<PointsTransaction, UUID> {
    List<PointsTransaction> findByMemberId(UUID memberId);
}
