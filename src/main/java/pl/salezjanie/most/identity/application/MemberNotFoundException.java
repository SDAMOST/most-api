package pl.salezjanie.most.identity.application;

/**
 * Thrown when a requested {@link pl.salezjanie.most.identity.domain.CommunityMember}
 * cannot be found.
 */
public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String message) {
        super(message);
    }
}
