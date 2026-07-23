package pl.salezjanie.most.identity.infrastructure;

import org.springframework.stereotype.Repository;
import pl.salezjanie.most.identity.domain.CommunityMember;
import pl.salezjanie.most.identity.domain.CommunityMemberRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that bridges the domain {@link CommunityMemberRepository} contract
 * to the Spring Data {@link JpaCommunityMemberRepository} implementation.
 */
@Repository
class CommunityMemberRepositoryAdapter implements CommunityMemberRepository {

    private final JpaCommunityMemberRepository jpa;

    CommunityMemberRepositoryAdapter(JpaCommunityMemberRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CommunityMember save(CommunityMember member) {
        return jpa.save(member);
    }

    @Override
    public Optional<CommunityMember> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<CommunityMember> findByEmail(String email) {
        return jpa.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }
}
