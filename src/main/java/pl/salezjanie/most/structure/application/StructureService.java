package pl.salezjanie.most.structure.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.structure.domain.LeadershipAssignment;
import pl.salezjanie.most.structure.domain.OrganizationUnit;
import pl.salezjanie.most.structure.domain.OrganizationUnitRepository;
import pl.salezjanie.most.structure.domain.Timeframe;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for the Structure bounded context.
 */
@Service
@Transactional(readOnly = true)
public class StructureService {

    private final OrganizationUnitRepository repository;

    public StructureService(OrganizationUnitRepository repository) {
        this.repository = repository;
    }

    // ──────────────────────────────────────────────
    //  Commands
    // ──────────────────────────────────────────────

    /**
     * Creates a new organizational unit.
     */
    @Transactional
    public OrganizationUnitNode createUnit(CreateUnitCommand command) {
        OrganizationUnit unit;

        if (command.parentUnitId() == null) {
            unit = OrganizationUnit.createRoot(UUID.randomUUID(), command.name());
        } else {
            // Verify the parent exists
            repository.findById(command.parentUnitId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Parent unit not found: " + command.parentUnitId()));

            unit = OrganizationUnit.createChild(UUID.randomUUID(), command.name(), command.parentUnitId());
        }

        OrganizationUnit saved = repository.save(unit);
        return toNode(saved, List.of());
    }

    /**
     * Assigns a leader to an organizational unit.
     */
    @Transactional
    public LeadershipAssignmentView assignLeader(UUID unitId, AssignLeaderCommand command) {
        OrganizationUnit unit = findOrThrow(unitId);

        Timeframe period = command.endDate() != null
                ? Timeframe.of(command.startDate(), command.endDate())
                : Timeframe.startingFrom(command.startDate());

        LeadershipAssignment assignment = unit.assignLeader(command.memberId(), command.role(), period);
        repository.save(unit);

        return toAssignmentView(assignment);
    }

    // ──────────────────────────────────────────────
    //  Queries
    // ──────────────────────────────────────────────

    /**
     * Returns the full organizational tree with active leaders at each node.
     */
    public List<OrganizationUnitNode> getTree() {
        List<OrganizationUnit> allUnits = repository.findAll();

        // Group units by their parent ID for efficient tree building
        Map<UUID, List<OrganizationUnit>> childrenByParent = allUnits.stream()
                .filter(u -> u.getParentUnitId() != null)
                .collect(Collectors.groupingBy(OrganizationUnit::getParentUnitId));

        // Start with root units and recursively build the tree
        return allUnits.stream()
                .filter(u -> u.getParentUnitId() == null)
                .map(root -> buildNode(root, childrenByParent))
                .toList();
    }

    // ──────────────────────────────────────────────
    //  Internal helpers
    // ──────────────────────────────────────────────

    private OrganizationUnit findOrThrow(UUID unitId) {
        return repository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));
    }

    private OrganizationUnitNode buildNode(OrganizationUnit unit,
                                           Map<UUID, List<OrganizationUnit>> childrenByParent) {
        List<OrganizationUnitNode> children = childrenByParent
                .getOrDefault(unit.getId(), List.of())
                .stream()
                .map(child -> buildNode(child, childrenByParent))
                .toList();

        return toNode(unit, children);
    }

    private static OrganizationUnitNode toNode(OrganizationUnit unit, List<OrganizationUnitNode> children) {
        List<LeadershipAssignmentView> activeLeaders = unit.getActiveAssignments().stream()
                .map(StructureService::toAssignmentView)
                .toList();

        return new OrganizationUnitNode(unit.getId(), unit.getName(), activeLeaders, children);
    }

    private static LeadershipAssignmentView toAssignmentView(LeadershipAssignment a) {
        return new LeadershipAssignmentView(
                a.getId(),
                a.getMemberId(),
                a.getRole(),
                a.getPeriod().getStartDate(),
                a.getPeriod().getEndDate()
        );
    }
}
