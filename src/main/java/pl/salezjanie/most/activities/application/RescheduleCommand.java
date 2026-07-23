package pl.salezjanie.most.activities.application;

import java.time.LocalDateTime;

/**
 * Command to reschedule an occurrence.
 */
public record RescheduleCommand(
        LocalDateTime newStart,
        LocalDateTime newEnd,
        String reason
) {
}
