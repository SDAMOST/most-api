package pl.salezjanie.most.activities.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Value Object recording a single reschedule event on an {@link Occurrence}.
 *
 * <p>Implements the domain rule: "Zmiana terminu zapisuje log (stara data, nowa data, powód)."
 */
@Embeddable
public class RescheduleEntry {

    @Column(name = "old_start", nullable = false)
    private LocalDateTime oldStart;

    @Column(name = "new_start", nullable = false)
    private LocalDateTime newStart;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "rescheduled_at", nullable = false)
    private Instant rescheduledAt;

    /** Required by JPA. */
    protected RescheduleEntry() {
    }

    public RescheduleEntry(LocalDateTime oldStart, LocalDateTime newStart, String reason) {
        this.oldStart = oldStart;
        this.newStart = newStart;
        this.reason = reason;
        this.rescheduledAt = Instant.now();
    }

    public LocalDateTime getOldStart() {
        return oldStart;
    }

    public LocalDateTime getNewStart() {
        return newStart;
    }

    public String getReason() {
        return reason;
    }

    public Instant getRescheduledAt() {
        return rescheduledAt;
    }
}
