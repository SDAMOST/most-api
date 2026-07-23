package pl.salezjanie.most.participation.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event emitted when a member's attendance is recorded.
 */
public record AttendanceRecordedEvent(
        UUID attendanceId,
        UUID occurrenceId,
        UUID memberId,
        Instant recordedAt
) {
}
