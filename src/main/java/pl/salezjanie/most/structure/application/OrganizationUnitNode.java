package pl.salezjanie.most.structure.application;

import java.util.List;
import java.util.UUID;

/**
 * Tree node representing an organizational unit with its active leaders and children.
 */
public record OrganizationUnitNode(
        UUID id,
        String name,
        List<LeadershipAssignmentView> activeLeaders,
        List<OrganizationUnitNode> children
) {
}
