package pl.salezjanie.most.identity.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for {@link CommunityMember} aggregate.
 *
 * <p>Defined in the domain layer so the domain stays independent of
 * infrastructure concerns (Dependency Inversion).
 */
public interface CommunityMemberRepository {

    CommunityMember save(CommunityMember member);

    Optional<CommunityMember> findById(UUID id);

    Optional<CommunityMember> findByEmail(String email);

    boolean existsByEmail(String email);
}
