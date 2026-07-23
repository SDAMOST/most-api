package pl.salezjanie.most.identity.application;

import pl.salezjanie.most.identity.domain.MemberStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only projection of a {@link pl.salezjanie.most.identity.domain.CommunityMember}.
 */
public record MemberProfile(
        UUID id,
        String displayName,
        String email,
        MemberStatus status,
        Instant createdAt
) {
}
