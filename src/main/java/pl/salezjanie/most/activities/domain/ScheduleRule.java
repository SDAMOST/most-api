package pl.salezjanie.most.activities.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity defining a recurrence pattern for generating {@link Occurrence}s.
 *
 * <p>Part of the {@link Initiative} aggregate.
 */
@Entity
@Table(name = "schedule_rules")
public class ScheduleRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 20)
    private RecurrenceType recurrenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** Duration in minutes, stored as an integer for simplicity. */
    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_until")
    private LocalDate effectiveUntil;

    /** Required by JPA. */
    protected ScheduleRule() {
    }

    ScheduleRule(RecurrenceType recurrenceType, DayOfWeek dayOfWeek, LocalTime startTime,
                 Duration duration, LocalDate effectiveFrom, LocalDate effectiveUntil) {
        this.recurrenceType = Objects.requireNonNull(recurrenceType, "recurrenceType must not be null");
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
        this.startTime = Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        this.durationMinutes = (int) duration.toMinutes();
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom must not be null");
        this.effectiveUntil = effectiveUntil;

        if (effectiveUntil != null && effectiveUntil.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("effectiveUntil must not be before effectiveFrom");
        }
    }

    /**
     * Returns {@code true} if this rule is active on the given date.
     */
    public boolean isEffectiveOn(LocalDate date) {
        if (date.isBefore(effectiveFrom)) {
            return false;
        }
        return effectiveUntil == null || !date.isAfter(effectiveUntil);
    }

    /**
     * Returns the step in weeks between occurrences.
     */
    public int weekStep() {
        return recurrenceType == RecurrenceType.BIWEEKLY ? 2 : 1;
    }

    public UUID getId() {
        return id;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return Duration.ofMinutes(durationMinutes);
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveUntil() {
        return effectiveUntil;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleRule that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
