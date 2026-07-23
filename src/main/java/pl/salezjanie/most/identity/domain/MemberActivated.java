package pl.salezjanie.most.identity.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event emitted when a {@link CommunityMember} transitions to {@link MemberStatus#ACTIVE}.
 */
public record MemberActivated(UUID memberId, Instant occurredAt) {
}
