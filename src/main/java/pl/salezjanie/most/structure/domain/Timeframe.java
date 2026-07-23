package pl.salezjanie.most.structure.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object representing a time-bound period for a {@link LeadershipAssignment}.
 *
 * <p>An open-ended assignment has {@code endDate == null}.
 */
@Embeddable
public class Timeframe {

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** Required by JPA. */
    protected Timeframe() {
    }

    private Timeframe(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "startDate must not be null");
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Creates a bounded timeframe.
     */
    public static Timeframe of(LocalDate startDate, LocalDate endDate) {
        return new Timeframe(startDate, endDate);
    }

    /**
     * Creates an open-ended timeframe (no end date).
     */
    public static Timeframe startingFrom(LocalDate startDate) {
        return new Timeframe(startDate, null);
    }

    /**
     * Returns {@code true} if the given date falls within this timeframe.
     */
    public boolean isActiveOn(LocalDate date) {
        if (date.isBefore(startDate)) {
            return false;
        }
        return endDate == null || !date.isAfter(endDate);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Timeframe that)) return false;
        return Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return "Timeframe{%s → %s}".formatted(startDate, endDate != null ? endDate : "∞");
    }
}
