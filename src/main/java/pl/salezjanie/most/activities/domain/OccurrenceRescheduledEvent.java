package pl.salezjanie.most.activities.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record OccurrenceRescheduledEvent(
        UUID occurrenceId,
        UUID initiativeId,
        LocalDateTime oldStart,
        LocalDateTime newStart,
        String reason,
        Instant rescheduledAt
) {
}
