package pl.salezjanie.most.engagement.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.engagement.domain.PointsLedger;
import pl.salezjanie.most.engagement.domain.PointsLedgerRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class EngagementService {

    private static final Logger log = LoggerFactory.getLogger(EngagementService.class);

    private final PointsLedgerRepository ledgerRepository;

    public EngagementService(PointsLedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    public void awardPoints(UUID memberId, UUID occurrenceId, UUID unitId, int points, Integer monthlyPointsCap, String reason, Instant timestamp) {
        PointsLedger ledger = ledgerRepository.findByMemberId(memberId);
        
        ledger.addTransaction(occurrenceId, unitId, points, reason, monthlyPointsCap, timestamp).ifPresentOrElse(
                transaction -> {
                    ledgerRepository.save(transaction);
                    log.info("Awarded {} points to member {} for occurrence {} (reason: {})", 
                            transaction.getPoints(), memberId, occurrenceId, reason);
                },
                () -> log.info("Points capped for member {} for occurrence {} in unit {}", 
                        memberId, occurrenceId, unitId)
        );
    }

    @Transactional(readOnly = true)
    public PointsLedgerView getMyPoints(UUID memberId) {
        return PointsLedgerView.from(ledgerRepository.findByMemberId(memberId));
    }
}
