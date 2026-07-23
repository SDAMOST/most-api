package pl.salezjanie.most.engagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a single point grant for a member.
 */
@Entity
@Table(name = "points_transactions")
public class PointsTransaction {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "member_id", updatable = false, nullable = false)
    private UUID memberId;

    @Column(name = "occurrence_id", updatable = false)
    private UUID occurrenceId;

    @Column(name = "unit_id", updatable = false, nullable = false)
    private UUID unitId;

    @Column(name = "points", updatable = false, nullable = false)
    private int points;

    @Column(name = "reason", updatable = false, nullable = false)
    private String reason;

    @Column(name = "timestamp", updatable = false, nullable = false)
    private Instant timestamp;

    /** Required by JPA. */
    protected PointsTransaction() {
    }

    public PointsTransaction(UUID id, UUID memberId, UUID occurrenceId, UUID unitId, int points, String reason, Instant timestamp) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.memberId = Objects.requireNonNull(memberId, "memberId must not be null");
        this.occurrenceId = occurrenceId;
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.points = points;
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        this.reason = reason;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    public UUID getId() {
        return id;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public UUID getOccurrenceId() {
        return occurrenceId;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public int getPoints() {
        return points;
    }

    public String getReason() {
        return reason;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointsTransaction that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
