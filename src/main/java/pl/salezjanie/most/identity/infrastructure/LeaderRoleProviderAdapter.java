package pl.salezjanie.most.identity.infrastructure;

import org.springframework.stereotype.Component;
import pl.salezjanie.most.identity.application.UserRoleProvider;
import pl.salezjanie.most.structure.application.StructureService;

import java.util.UUID;

/**
 * Implementation of {@link UserRoleProvider} that uses the Structure bounded context
 * to determine if a member is a leader.
 */
@Component
class LeaderRoleProviderAdapter implements UserRoleProvider {

    private final StructureService structureService;

    LeaderRoleProviderAdapter(StructureService structureService) {
        this.structureService = structureService;
    }

    @Override
    public java.util.Set<String> getAuthorities(UUID memberId) {
        java.util.Set<String> authorities = new java.util.HashSet<>();
        var roles = structureService.getRoles(memberId);
        
        for (var role : roles) {
            if (role.name().equals("SEKSTET")) {
                authorities.add("VERIFY_USERS");
                authorities.add("MANAGE_GLOBAL_EVENTS");
            } else if (role.name().equals("PRZESLOWY") || role.name().equals("PODPRZESLOWY")) {
                authorities.add("MANAGE_UNIT_EVENTS");
            }
        }
        return authorities;
    }
}
