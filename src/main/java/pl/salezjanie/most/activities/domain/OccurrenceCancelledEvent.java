package pl.salezjanie.most.activities.domain;

import java.time.Instant;
import java.util.UUID;

public record OccurrenceCancelledEvent(
        UUID occurrenceId,
        UUID initiativeId,
        Instant cancelledAt
) {
}
