package pl.salezjanie.most.structure.application;

import pl.salezjanie.most.structure.domain.LeadershipRole;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Read-only view of a leadership assignment.
 */
public record LeadershipAssignmentView(
        UUID id,
        UUID memberId,
        LeadershipRole role,
        LocalDate startDate,
        LocalDate endDate
) {
}
