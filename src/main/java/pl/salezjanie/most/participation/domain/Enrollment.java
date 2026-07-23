package pl.salezjanie.most.participation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing a member's sign-up for a specific Occurrence.
 *
 * <p>Domain rule: no enrollment after the Occurrence has started (enforced in application service).
 */
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "occurrence_id", nullable = false)
    private UUID occurrenceId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column(name = "enrolled_at", updatable = false, nullable = false)
    private Instant enrolledAt;

    /** Required by JPA. */
    protected Enrollment() {
    }

    private Enrollment(UUID id, UUID occurrenceId, UUID memberId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.occurrenceId = Objects.requireNonNull(occurrenceId, "occurrenceId must not be null");
        this.memberId = Objects.requireNonNull(memberId, "memberId must not be null");
        this.status = EnrollmentStatus.ENROLLED;
        this.enrolledAt = Instant.now();
    }

    // ──────────────────────────────────────────────
    //  Factory method
    // ──────────────────────────────────────────────

    public static Enrollment create(UUID id, UUID occurrenceId, UUID memberId) {
        return new Enrollment(id, occurrenceId, memberId);
    }

    // ──────────────────────────────────────────────
    //  Business operations
    // ──────────────────────────────────────────────

    /**
     * Withdraws this enrollment. Only allowed when currently ENROLLED.
     *
     * @throws IllegalStateException if not in ENROLLED status
     */
    public void withdraw() {
        if (this.status != EnrollmentStatus.ENROLLED) {
            throw new IllegalStateException(
                    "Cannot withdraw enrollment in %s status (expected ENROLLED)".formatted(this.status));
        }
        this.status = EnrollmentStatus.WITHDRAWN;
    }

    /**
     * Cancels this enrollment (e.g. when the occurrence is cancelled).
     *
     * @throws IllegalStateException if already CANCELLED
     */
    public void cancel() {
        if (this.status == EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("Enrollment is already cancelled");
        }
        this.status = EnrollmentStatus.CANCELLED;
    }

    // ──────────────────────────────────────────────
    //  Accessors
    // ──────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public UUID getOccurrenceId() {
        return occurrenceId;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    // ──────────────────────────────────────────────
    //  equals / hashCode
    // ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Enrollment{id=%s, occurrenceId=%s, memberId=%s, status=%s}".formatted(id, occurrenceId, memberId, status);
    }
}
