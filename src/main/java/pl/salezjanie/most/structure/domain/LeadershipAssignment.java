package pl.salezjanie.most.structure.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a time-bound leadership role within an {@link OrganizationUnit}.
 *
 * <p>This entity is part of the {@link OrganizationUnit} aggregate and should not
 * be accessed independently.
 */
@Entity
@Table(name = "leadership_assignments")
public class LeadershipAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Reference to the CommunityMember from the Identity context.
     * We use a UUID rather than a direct JPA relationship to maintain
     * bounded context separation.
     */
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private LeadershipRole role;

    @Embedded
    private Timeframe period;

    /** Required by JPA. */
    protected LeadershipAssignment() {
    }

    LeadershipAssignment(UUID memberId, LeadershipRole role, Timeframe period) {
        this.memberId = Objects.requireNonNull(memberId, "memberId must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.period = Objects.requireNonNull(period, "period must not be null");
    }

    public UUID getId() {
        return id;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public LeadershipRole getRole() {
        return role;
    }

    public Timeframe getPeriod() {
        return period;
    }

    /**
     * Ends this assignment on the given date.
     */
    void endOn(java.time.LocalDate endDate) {
        this.period = Timeframe.of(this.period.getStartDate(), endDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeadershipAssignment that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
