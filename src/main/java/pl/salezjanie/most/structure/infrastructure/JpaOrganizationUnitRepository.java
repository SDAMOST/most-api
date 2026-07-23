package pl.salezjanie.most.structure.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.salezjanie.most.structure.domain.OrganizationUnit;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA interface for {@link OrganizationUnit} persistence.
 */
interface JpaOrganizationUnitRepository extends JpaRepository<OrganizationUnit, UUID> {

    List<OrganizationUnit> findAllByParentUnitId(UUID parentUnitId);

    List<OrganizationUnit> findAllByParentUnitIdIsNull();
}
