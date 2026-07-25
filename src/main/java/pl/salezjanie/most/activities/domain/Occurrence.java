package pl.salezjanie.most.activities.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing a concrete realization of an {@link Initiative}
 * on a specific date and time.
 *
 * <p>Lifecycle: {@code PLANNED → PUBLISHED → COMPLETED / CANCELLED}.
 */
@Entity
@Table(name = "occurrences")
public class Occurrence {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "initiative_id", nullable = false)
    private UUID initiativeId;

    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private LocalDateTime scheduledEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OccurrenceStatus status;

    /** Maximum number of enrollments. Null means unlimited. */
    @Column(name = "capacity")
    private Integer capacity;

    @ElementCollection
    @CollectionTable(name = "occurrence_reschedule_log", joinColumns = @JoinColumn(name = "occurrence_id"))
    @OrderBy("rescheduled_at ASC")
    private List<RescheduleEntry> rescheduleLog = new ArrayList<>();

    @Transient
    private final transient List<Object> domainEvents = new ArrayList<>();

    /** Required by JPA. */
    protected Occurrence() {
    }

    private Occurrence(UUID id, UUID initiativeId, LocalDateTime scheduledStart, LocalDateTime scheduledEnd,
                       Integer capacity) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.initiativeId = Objects.requireNonNull(initiativeId, "initiativeId must not be null");
        this.scheduledStart = Objects.requireNonNull(scheduledStart, "scheduledStart must not be null");
        this.scheduledEnd = Objects.requireNonNull(scheduledEnd, "scheduledEnd must not be null");

        if (!scheduledEnd.isAfter(scheduledStart)) {
            throw new IllegalArgumentException("scheduledEnd must be after scheduledStart");
        }
        if (capacity != null && capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }

        this.status = OccurrenceStatus.PLANNED;
        this.capacity = capacity;
    }

    // ──────────────────────────────────────────────
    //  Factory method
    // ──────────────────────────────────────────────

    public static Occurrence create(UUID id, UUID initiativeId,
                                    LocalDateTime scheduledStart, LocalDateTime scheduledEnd) {
        return new Occurrence(id, initiativeId, scheduledStart, scheduledEnd, null);
    }

    public static Occurrence createWithCapacity(UUID id, UUID initiativeId,
                                               LocalDateTime scheduledStart, LocalDateTime scheduledEnd,
                                               Integer capacity) {
        return new Occurrence(id, initiativeId, scheduledStart, scheduledEnd, capacity);
    }

    // ──────────────────────────────────────────────
    //  Lifecycle transitions
    // ──────────────────────────────────────────────

    /**
     * Makes this occurrence visible to participants.
     */
    public void publish() {
        requireStatus(OccurrenceStatus.PLANNED, "publish");
        if (this.scheduledStart.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot publish an occurrence that has already started");
        }
        this.status = OccurrenceStatus.PUBLISHED;
        domainEvents.add(new OccurrencePublishedEvent(this.id, this.initiativeId, Instant.now()));
    }

    /**
     * Marks this occurrence as successfully completed.
     */
    public void complete() {
        requireStatus(OccurrenceStatus.PUBLISHED, "complete");
        this.status = OccurrenceStatus.COMPLETED;
    }

    /**
     * Cancels this occurrence. Allowed from PLANNED or PUBLISHED.
     */
    public void cancel() {
        if (this.status == OccurrenceStatus.COMPLETED || this.status == OccurrenceStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot cancel an occurrence in %s status".formatted(this.status));
        }
        this.status = OccurrenceStatus.CANCELLED;
        domainEvents.add(new OccurrenceCancelledEvent(this.id, this.initiativeId, Instant.now()));
    }

    /**
     * Changes the scheduled start time and logs the change with a reason.
     *
     * <p>Implements the domain rule: reschedule writes a log entry (old date, new date, reason).
     */
    public void reschedule(LocalDateTime newStart, LocalDateTime newEnd, String reason) {
        if (this.status == OccurrenceStatus.COMPLETED || this.status == OccurrenceStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot reschedule an occurrence in %s status".formatted(this.status));
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reschedule reason must not be blank");
        }
        if (newStart.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot reschedule to a past date");
        }
        if (!newEnd.isAfter(newStart)) {
            throw new IllegalArgumentException("newEnd must be after newStart");
        }

        rescheduleLog.add(new RescheduleEntry(this.scheduledStart, newStart, reason));
        
        LocalDateTime oldStart = this.scheduledStart;
        this.scheduledStart = newStart;
        this.scheduledEnd = newEnd;
        
        domainEvents.add(new OccurrenceRescheduledEvent(this.id, this.initiativeId, oldStart, newStart, reason, Instant.now()));
    }

    // ──────────────────────────────────────────────
    //  Accessors
    // ──────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public UUID getInitiativeId() {
        return initiativeId;
    }

    public LocalDateTime getScheduledStart() {
        return scheduledStart;
    }

    public LocalDateTime getScheduledEnd() {
        return scheduledEnd;
    }

    public OccurrenceStatus getStatus() {
        return status;
    }

    /**
     * Returns the capacity limit, or null if unlimited.
     */
    public Integer getCapacity() {
        return capacity;
    }

    /**
     * Checks whether additional enrollments can be accepted given the current count.
     */
    public boolean hasCapacityFor(long currentEnrollmentCount) {
        return capacity == null || currentEnrollmentCount < capacity;
    }

    public List<RescheduleEntry> getRescheduleLog() {
        return Collections.unmodifiableList(rescheduleLog);
    }

    // ──────────────────────────────────────────────
    //  equals / hashCode
    // ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Occurrence that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Occurrence{id=%s, start=%s, status=%s}".formatted(id, scheduledStart, status);
    }

    // ──────────────────────────────────────────────
    //  Domain Events
    // ──────────────────────────────────────────────

    @DomainEvents
    public List<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ──────────────────────────────────────────────
    //  Internal
    // ──────────────────────────────────────────────

    private void requireStatus(OccurrenceStatus expected, String action) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Cannot %s an occurrence in %s status (expected %s)".formatted(action, this.status, expected));
        }
    }
}
