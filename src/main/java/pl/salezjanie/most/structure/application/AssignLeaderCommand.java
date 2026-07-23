package pl.salezjanie.most.structure.application;

import pl.salezjanie.most.structure.domain.LeadershipRole;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to assign a leader to an organizational unit.
 */
public record AssignLeaderCommand(
        UUID memberId,
        LeadershipRole role,
        LocalDate startDate,
        LocalDate endDate
) {
}
