package pl.salezjanie.most.identity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.salezjanie.most.identity.domain.CommunityMember;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA interface for {@link CommunityMember} persistence.
 *
 * <p>Package-private — not exposed outside the infrastructure layer.
 * External code uses {@link pl.salezjanie.most.identity.domain.CommunityMemberRepository}.
 */
interface JpaCommunityMemberRepository extends JpaRepository<CommunityMember, UUID> {

    Optional<CommunityMember> findByEmail(String email);

    boolean existsByEmail(String email);

    java.util.List<CommunityMember> findByStatus(pl.salezjanie.most.identity.domain.MemberStatus status);
}
