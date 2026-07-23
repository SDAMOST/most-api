package pl.salezjanie.most.engagement.domain;

import java.util.UUID;

public interface PointsLedgerRepository {
    PointsLedger findByMemberId(UUID memberId);
    void save(PointsTransaction transaction);
}
