package pl.salezjanie.most.activities.application;

import pl.salezjanie.most.activities.domain.OccurrenceStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only view of an occurrence.
 */
public record OccurrenceView(
        UUID id,
        UUID initiativeId,
        String initiativeName,
        LocalDateTime scheduledStart,
        LocalDateTime scheduledEnd,
        OccurrenceStatus status,
        String rescheduleReason
) {
}
