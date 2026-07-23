package pl.salezjanie.most.activities.application;

import pl.salezjanie.most.activities.domain.RecurrenceType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command to add a schedule rule to an initiative.
 */
public record AddScheduleRuleCommand(
        RecurrenceType recurrenceType,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        int durationMinutes,
        LocalDate effectiveFrom,
        LocalDate effectiveUntil
) {
}
