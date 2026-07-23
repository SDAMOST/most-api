package pl.salezjanie.most.engagement.infrastructure;

import org.springframework.stereotype.Repository;
import pl.salezjanie.most.engagement.domain.PointsLedger;
import pl.salezjanie.most.engagement.domain.PointsLedgerRepository;
import pl.salezjanie.most.engagement.domain.PointsTransaction;

import java.util.List;
import java.util.UUID;

@Repository
public class PointsLedgerRepositoryAdapter implements PointsLedgerRepository {

    private final JpaPointsTransactionRepository jpa;

    public PointsLedgerRepositoryAdapter(JpaPointsTransactionRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PointsLedger findByMemberId(UUID memberId) {
        List<PointsTransaction> transactions = jpa.findByMemberId(memberId);
        return new PointsLedger(memberId, transactions);
    }

    @Override
    public void save(PointsTransaction transaction) {
        jpa.save(transaction);
    }
}
