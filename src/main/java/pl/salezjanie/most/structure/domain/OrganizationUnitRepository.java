package pl.salezjanie.most.structure.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for the {@link OrganizationUnit} aggregate.
 */
public interface OrganizationUnitRepository {

    OrganizationUnit save(OrganizationUnit unit);

    Optional<OrganizationUnit> findById(UUID id);

    List<OrganizationUnit> findAll();

    List<OrganizationUnit> findAllByParentUnitId(UUID parentUnitId);

    /**
     * Returns all root-level units (Przęsła).
     */
    List<OrganizationUnit> findAllRoots();
}
