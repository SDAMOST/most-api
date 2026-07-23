package pl.salezjanie.most.communication.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Stores the FCM device token for a member.
 */
@Entity
@Table(name = "device_tokens")
public class DeviceToken {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "member_id", updatable = false, nullable = false)
    private UUID memberId;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    protected DeviceToken() {
    }

    public DeviceToken(UUID id, UUID memberId, String token, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.memberId = Objects.requireNonNull(memberId);
        this.token = Objects.requireNonNull(token);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public String getToken() {
        return token;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceToken that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
