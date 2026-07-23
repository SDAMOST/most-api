package pl.salezjanie.most.activities.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing a recurring program or activity (e.g. "Lectio", "Rajdy").
 *
 * <p>Owned by a Podprzęsło (referenced via {@code ownerUnitId}).
 */
@Entity
@Table(name = "initiatives")
public class Initiative {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "default_points", nullable = false)
    private int defaultPoints;

    /**
     * Reference to the owning OrganizationUnit (Podprzęsło) from the Structure context.
     */
    @Column(name = "owner_unit_id", nullable = false)
    private UUID ownerUnitId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "initiative_id", nullable = false)
    private List<ScheduleRule> scheduleRules = new ArrayList<>();

    /** Required by JPA. */
    protected Initiative() {
    }

    private Initiative(UUID id, String name, String description, UUID ownerUnitId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
        this.description = description;
        this.ownerUnitId = Objects.requireNonNull(ownerUnitId, "ownerUnitId must not be null");
        this.defaultPoints = 1;
    }

    // ──────────────────────────────────────────────
    //  Factory method
    // ──────────────────────────────────────────────

    public static Initiative create(UUID id, String name, String description, UUID ownerUnitId) {
        return new Initiative(id, name, description, ownerUnitId);
    }

    // ──────────────────────────────────────────────
    //  Business operations
    // ──────────────────────────────────────────────

    /**
     * Adds a schedule rule defining when occurrences should be generated.
     */
    public ScheduleRule addScheduleRule(RecurrenceType recurrenceType, DayOfWeek dayOfWeek,
                                        LocalTime startTime, Duration duration,
                                        LocalDate effectiveFrom, LocalDate effectiveUntil) {
        ScheduleRule rule = new ScheduleRule(recurrenceType, dayOfWeek, startTime,
                duration, effectiveFrom, effectiveUntil);
        scheduleRules.add(rule);
        return rule;
    }

    /**
     * Removes a schedule rule by its ID.
     */
    public void removeScheduleRule(UUID ruleId) {
        boolean removed = scheduleRules.removeIf(r -> r.getId().equals(ruleId));
        if (!removed) {
            throw new IllegalArgumentException("Schedule rule not found: " + ruleId);
        }
    }

    // ──────────────────────────────────────────────
    //  Accessors
    // ──────────────────────────────────────────────

    public void setDefaultPoints(int defaultPoints) {
        if (defaultPoints < 0) {
            throw new IllegalArgumentException("defaultPoints cannot be negative");
        }
        this.defaultPoints = defaultPoints;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getOwnerUnitId() {
        return ownerUnitId;
    }

    public int getDefaultPoints() {
        return defaultPoints;
    }

    public List<ScheduleRule> getScheduleRules() {
        return Collections.unmodifiableList(scheduleRules);
    }

    // ──────────────────────────────────────────────
    //  equals / hashCode
    // ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Initiative that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Initiative{id=%s, name='%s'}".formatted(id, name);
    }
}
