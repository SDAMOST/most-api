package pl.salezjanie.most.identity.application;

import java.util.UUID;

/**
 * Interface defining how the Identity context retrieves user roles.
 * Implemented by infrastructure layer using other bounded contexts (e.g. Structure).
 */
public interface UserRoleProvider {

    /**
     * Retrieves the structural authorities for the given member.
     */
    java.util.Set<String> getAuthorities(UUID memberId);
}
