package pl.salezjanie.most.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Aggregate Root for the Identity bounded context.
 *
 * <p>Represents a person who belongs to the MOST community.
 * This is not an anemic entity — business rules live here.
 */
@Entity
@Table(name = "community_members")
public class CommunityMember {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_role", nullable = false, length = 20)
    private SystemRole systemRole;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Domain events raised during the current unit-of-work, not persisted.
     */
    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    /** Required by JPA. */
    protected CommunityMember() {
    }

    private CommunityMember(UUID id, String passwordHash, String displayName, String email) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
        this.displayName = requireNonBlank(displayName, "displayName");
        this.email = requireNonBlank(email, "email");
        this.status = MemberStatus.PENDING;
        this.systemRole = SystemRole.USER;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // ──────────────────────────────────────────────
    //  Factory method
    // ──────────────────────────────────────────────

    /**
     * Registers a new community member in {@link MemberStatus#PENDING} state.
     */
    public static CommunityMember register(UUID id, String passwordHash, String displayName, String email) {
        return new CommunityMember(id, passwordHash, displayName, email);
    }

    // ──────────────────────────────────────────────
    //  Business operations
    // ──────────────────────────────────────────────

    /**
     * Activates this member. Emits a {@link MemberActivated} domain event.
     *
     * @throws IllegalStateException if the member is not in {@link MemberStatus#PENDING} status
     */
    public void activate() {
        if (this.status != MemberStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING members can be activated. Current status: " + this.status);
        }
        this.status = MemberStatus.ACTIVE;
        this.updatedAt = Instant.now();
        domainEvents.add(new MemberActivated(this.id, this.updatedAt));
    }

    /**
     * Suspends an active member.
     *
     * @throws IllegalStateException if the member is not in {@link MemberStatus#ACTIVE} status
     */
    public void suspend() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only ACTIVE members can be suspended. Current status: " + this.status);
        }
        this.status = MemberStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    /**
     * Reinstates a suspended member back to active.
     *
     * @throws IllegalStateException if the member is not in {@link MemberStatus#SUSPENDED} status
     */
    public void reinstate() {
        if (this.status != MemberStatus.SUSPENDED) {
            throw new IllegalStateException(
                    "Only SUSPENDED members can be reinstated. Current status: " + this.status);
        }
        this.status = MemberStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    // ──────────────────────────────────────────────
    //  Domain events
    // ──────────────────────────────────────────────

    /**
     * Returns an unmodifiable view of domain events raised since last clear.
     */
    @DomainEvents
    public List<Object> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clears accumulated domain events (call after publishing).
     */
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    // ──────────────────────────────────────────────
    //  Accessors
    // ──────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public SystemRole getSystemRole() {
        return systemRole;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ──────────────────────────────────────────────
    //  equals / hashCode — based on identity (id)
    // ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityMember that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CommunityMember{id=%s, displayName='%s', status=%s}".formatted(id, displayName, status);
    }

    // ──────────────────────────────────────────────
    //  Internal helpers
    // ──────────────────────────────────────────────

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
