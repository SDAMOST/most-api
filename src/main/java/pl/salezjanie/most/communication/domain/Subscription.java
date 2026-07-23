package pl.salezjanie.most.communication.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a member's opt-in subscription to receive notifications for a specific initiative.
 */
@Entity
@Table(name = "subscriptions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "initiative_id"})
})
public class Subscription {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "member_id", updatable = false, nullable = false)
    private UUID memberId;

    @Column(name = "initiative_id", updatable = false, nullable = false)
    private UUID initiativeId;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    protected Subscription() {
    }

    public Subscription(UUID id, UUID memberId, UUID initiativeId, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.memberId = Objects.requireNonNull(memberId);
        this.initiativeId = Objects.requireNonNull(initiativeId);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public UUID getInitiativeId() {
        return initiativeId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subscription that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
