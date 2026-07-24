package pl.salezjanie.most.identity.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.identity.domain.CommunityMember;
import pl.salezjanie.most.identity.domain.CommunityMemberRepository;

import java.util.UUID;

/**
 * Application service orchestrating use-cases for the Identity bounded context.
 */
@Service
@Transactional(readOnly = true)
public class CommunityMemberService {

    private final CommunityMemberRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CommunityMemberService(CommunityMemberRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new community member.
     */
    @Transactional
    public MemberProfile register(RegisterMemberCommand command) {
        if (repository.existsByEmail(command.email())) {
            throw new IllegalArgumentException(
                    "A member with email '%s' already exists".formatted(command.email()));
        }

        CommunityMember member = CommunityMember.register(
                UUID.randomUUID(),
                passwordEncoder.encode(command.password()),
                command.displayName(),
                command.email()
        );

        return toProfile(repository.save(member));
    }

    /**
     * Activates a pending member.
     */
    @Transactional
    public MemberProfile activate(UUID memberId) {
        CommunityMember member = findOrThrow(memberId);
        member.activate();
        member.clearDomainEvents();
        return toProfile(repository.save(member));
    }

    /**
     * Returns the profile for a given member ID.
     */
    public MemberProfile getProfile(UUID memberId) {
        return toProfile(findOrThrow(memberId));
    }

    /**
     * Returns a list of members with PENDING status.
     */
    public java.util.List<MemberProfile> getPendingMembers() {
        return repository.findByStatus(pl.salezjanie.most.identity.domain.MemberStatus.PENDING)
                .stream()
                .map(CommunityMemberService::toProfile)
                .toList();
    }

    private CommunityMember findOrThrow(UUID memberId) {
        return repository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(
                        "Member not found: " + memberId));
    }

    private static MemberProfile toProfile(CommunityMember member) {
        return new MemberProfile(
                member.getId(),
                member.getDisplayName(),
                member.getEmail(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }
}
