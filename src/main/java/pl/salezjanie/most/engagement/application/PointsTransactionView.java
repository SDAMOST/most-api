package pl.salezjanie.most.engagement.application;

import pl.salezjanie.most.engagement.domain.PointsTransaction;

import java.time.Instant;
import java.util.UUID;

public record PointsTransactionView(
        UUID id,
        UUID occurrenceId,
        UUID unitId,
        int points,
        String reason,
        Instant timestamp
) {
    public static PointsTransactionView from(PointsTransaction transaction) {
        return new PointsTransactionView(
                transaction.getId(),
                transaction.getOccurrenceId(),
                transaction.getUnitId(),
                transaction.getPoints(),
                transaction.getReason(),
                transaction.getTimestamp()
        );
    }
}
