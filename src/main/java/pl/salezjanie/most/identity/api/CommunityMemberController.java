package pl.salezjanie.most.identity.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.salezjanie.most.identity.application.CommunityMemberService;
import pl.salezjanie.most.identity.application.MemberProfile;

import java.util.UUID;

/**
 * REST API for the Identity bounded context.
 * Requires Authentication.
 */
@RestController
@RequestMapping("/api/members")
class CommunityMemberController {

    private final CommunityMemberService service;

    CommunityMemberController(CommunityMemberService service) {
        this.service = service;
    }

    /**
     * Get member profile by ID.
     */
    @GetMapping("/{memberId}")
    MemberProfile getProfile(@PathVariable UUID memberId) {
        return service.getProfile(memberId);
    }

    /**
     * Activate a pending member.
     */
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('VERIFY_USERS')")
    @PutMapping("/{memberId}/activate")
    MemberProfile activate(@PathVariable UUID memberId) {
        return service.activate(memberId);
    }

    /**
     * Get list of members, optionally filtered by status.
     */
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('VERIFY_USERS')")
    @GetMapping
    java.util.List<MemberProfile> getMembers(@org.springframework.web.bind.annotation.RequestParam(required = false) String status) {
        if ("PENDING".equalsIgnoreCase(status)) {
            return service.getPendingMembers();
        }
        // Return empty or all? For now, we only support PENDING.
        return java.util.List.of();
    }
}
