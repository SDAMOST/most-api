package pl.salezjanie.most.identity.domain;

/**
 * Lifecycle status of a {@link CommunityMember}.
 *
 * <p>Flow: {@code PENDING → ACTIVE → SUSPENDED} (or back to ACTIVE).
 */
public enum MemberStatus {

    /** Account created but not yet confirmed by an administrator. */
    PENDING,

    /** Fully activated member — can participate in activities. */
    ACTIVE,

    /** Temporarily suspended — cannot enroll or attend. */
    SUSPENDED
}
