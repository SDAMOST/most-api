package pl.salezjanie.most.structure.infrastructure;

import org.springframework.stereotype.Repository;
import pl.salezjanie.most.structure.domain.OrganizationUnit;
import pl.salezjanie.most.structure.domain.OrganizationUnitRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter bridging the domain {@link OrganizationUnitRepository} to
 * Spring Data {@link JpaOrganizationUnitRepository}.
 */
@Repository
class OrganizationUnitRepositoryAdapter implements OrganizationUnitRepository {

    private final JpaOrganizationUnitRepository jpa;

    OrganizationUnitRepositoryAdapter(JpaOrganizationUnitRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OrganizationUnit save(OrganizationUnit unit) {
        return jpa.save(unit);
    }

    @Override
    public Optional<OrganizationUnit> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<OrganizationUnit> findAll() {
        return jpa.findAll();
    }

    @Override
    public List<OrganizationUnit> findAllByParentUnitId(UUID parentUnitId) {
        return jpa.findAllByParentUnitId(parentUnitId);
    }

    @Override
    public List<OrganizationUnit> findAllRoots() {
        return jpa.findAllByParentUnitIdIsNull();
    }

    @Override
    public List<pl.salezjanie.most.structure.domain.LeadershipRole> findActiveRoles(UUID memberId) {
        return jpa.findActiveRolesForMember(memberId);
    }
}
