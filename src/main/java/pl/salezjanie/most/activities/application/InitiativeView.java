package pl.salezjanie.most.activities.application;

import java.util.List;
import java.util.UUID;

/**
 * Read-only view of an initiative.
 */
public record InitiativeView(
        UUID id,
        String name,
        String description,
        UUID ownerUnitId,
        List<ScheduleRuleView> scheduleRules
) {
}
