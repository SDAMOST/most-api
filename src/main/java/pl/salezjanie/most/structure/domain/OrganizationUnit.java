package pl.salezjanie.most.structure.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root for the Structure bounded context.
 *
 * <p>Represents a branch or sub-branch of the MOST community (Przęsło / Podprzęsło).
 * Manages its own collection of {@link LeadershipAssignment}s.
 */
@Entity
@Table(name = "organization_units")
public class OrganizationUnit {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Parent unit ID for building the tree. {@code null} for root-level units (Przęsła).
     */
    @Column(name = "parent_unit_id")
    private UUID parentUnitId;

    /**
     * Monthly points cap for this unit. Null means unlimited.
     */
    @Column(name = "monthly_points_cap")
    private Integer monthlyPointsCap;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "unit_id", nullable = false)
    @OrderBy("start_date ASC")
    private List<LeadershipAssignment> assignments = new ArrayList<>();

    /** Required by JPA. */
    protected OrganizationUnit() {
    }

    private OrganizationUnit(UUID id, String name, UUID parentUnitId, Integer monthlyPointsCap) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
        this.parentUnitId = parentUnitId;
        this.monthlyPointsCap = monthlyPointsCap;
    }

    // ──────────────────────────────────────────────
    //  Factory methods
    // ──────────────────────────────────────────────

    /**
     * Creates a root-level unit (e.g. a Przęsło). Default cap is 4.
     */
    public static OrganizationUnit createRoot(UUID id, String name) {
        return new OrganizationUnit(id, name, null, 4);
    }

    /**
     * Creates a child unit (e.g. a Podprzęsło) under the given parent.
     */
    public static OrganizationUnit createChild(UUID id, String name, UUID parentUnitId) {
        Objects.requireNonNull(parentUnitId, "parentUnitId must not be null for a child unit");
        return new OrganizationUnit(id, name, parentUnitId, null);
    }

    /**
     * Updates the monthly points cap.
     */
    public void setMonthlyPointsCap(Integer cap) {
        this.monthlyPointsCap = cap;
    }

    // ──────────────────────────────────────────────
    //  Business operations
    // ──────────────────────────────────────────────

    /**
     * Assigns a leader to this unit for the given period.
     */
    public LeadershipAssignment assignLeader(UUID memberId, LeadershipRole role, Timeframe period) {
        // Check for overlapping active assignment for the same member and role
        boolean hasOverlap = assignments.stream()
                .filter(a -> a.getMemberId().equals(memberId) && a.getRole() == role)
                .anyMatch(a -> a.getPeriod().isActiveOn(period.getStartDate()));

        if (hasOverlap) {
            throw new IllegalStateException(
                    "Member %s already has an active %s assignment in this unit".formatted(memberId, role));
        }

        LeadershipAssignment assignment = new LeadershipAssignment(memberId, role, period);
        assignments.add(assignment);
        return assignment;
    }

    /**
     * Revokes (ends) a leadership assignment as of today.
     */
    public void revokeAssignment(UUID assignmentId) {
        LeadershipAssignment assignment = assignments.stream()
                .filter(a -> a.getId().equals(assignmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assignment not found: " + assignmentId));

        assignment.endOn(LocalDate.now());
    }

    /**
     * Returns all currently active leadership assignments.
     */
    public List<LeadershipAssignment> getActiveAssignments() {
        LocalDate today = LocalDate.now();
        return assignments.stream()
                .filter(a -> a.getPeriod().isActiveOn(today))
                .toList();
    }

    // ──────────────────────────────────────────────
    //  Accessors
    // ──────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getParentUnitId() {
        return parentUnitId;
    }

    public Integer getMonthlyPointsCap() {
        return monthlyPointsCap;
    }

    public List<LeadershipAssignment> getAssignments() {
        return Collections.unmodifiableList(assignments);
    }

    // ──────────────────────────────────────────────
    //  equals / hashCode
    // ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganizationUnit that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "OrganizationUnit{id=%s, name='%s'}".formatted(id, name);
    }
}
