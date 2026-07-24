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

    @org.springframework.data.jpa.repository.Query("SELECT a.role FROM OrganizationUnit u JOIN u.assignments a WHERE a.memberId = :memberId AND a.period.startDate <= CURRENT_DATE AND (a.period.endDate IS NULL OR a.period.endDate >= CURRENT_DATE)")
    List<pl.salezjanie.most.structure.domain.LeadershipRole> findActiveRolesForMember(@org.springframework.data.repository.query.Param("memberId") UUID memberId);
}
