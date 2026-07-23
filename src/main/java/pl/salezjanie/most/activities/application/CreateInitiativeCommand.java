package pl.salezjanie.most.activities.application;

import java.util.UUID;

/**
 * Command to create a new initiative.
 */
public record CreateInitiativeCommand(
        String name,
        String description,
        UUID ownerUnitId
) {
}
