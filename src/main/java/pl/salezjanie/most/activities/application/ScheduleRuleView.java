package pl.salezjanie.most.activities.application;

import pl.salezjanie.most.activities.domain.RecurrenceType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Read-only view of a schedule rule.
 */
public record ScheduleRuleView(
        UUID id,
        RecurrenceType recurrenceType,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        int durationMinutes,
        LocalDate effectiveFrom,
        LocalDate effectiveUntil
) {
}
