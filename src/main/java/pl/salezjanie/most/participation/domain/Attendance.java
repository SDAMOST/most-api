package pl.salezjanie.most.participation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing confirmed presence of a member at an Occurrence.
 *
 * <p>Attendance can be recorded without a preceding enrollment
 * (e.g. for initiatives like "sprzątanie" or "schola").
 */
@Entity
@Table(name = "attendances")
public class Attendance {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "occurrence_id", nullable = false)
    private UUID occurrenceId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    /** Required by JPA. */
    protected Attendance() {
    }

    private Attendance(UUID id, UUID occurrenceId, UUID memberId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.occurrenceId = Objects.requireNonNull(occurrenceId, "occurrenceId must not be null");
        this.memberId = Objects.requireNonNull(memberId, "memberId must not be null");
        this.recordedAt = Instant.now();
        
        domainEvents.add(new AttendanceRecordedEvent(this.id, this.occurrenceId, this.memberId, this.recordedAt));
    }

    // ──────────────────────────────────────────────
    //  Factory method
    // ──────────────────────────────────────────────

    public static Attendance record(UUID id, UUID occurrenceId, UUID memberId) {
        return new Attendance(id, occurrenceId, memberId);
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

    public Instant getRecordedAt() {
        return recordedAt;
    }

    @DomainEvents
    public List<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ──────────────────────────────────────────────
    //  equals / hashCode
    // ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attendance that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Attendance{id=%s, occurrenceId=%s, memberId=%s}".formatted(id, occurrenceId, memberId);
    }
}
