package pl.salezjanie.most.participation.application;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only view of an attendance record.
 */
public record AttendanceView(
        UUID id,
        UUID occurrenceId,
        UUID memberId,
        Instant recordedAt
) {
}
