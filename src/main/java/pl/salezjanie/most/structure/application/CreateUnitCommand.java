package pl.salezjanie.most.structure.application;

import java.util.UUID;

/**
 * Command to create a new organizational unit.
 */
public record CreateUnitCommand(
        String name,
        UUID parentUnitId
) {
}
