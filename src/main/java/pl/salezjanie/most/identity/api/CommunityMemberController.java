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
    @PutMapping("/{memberId}/activate")
    MemberProfile activate(@PathVariable UUID memberId) {
        return service.activate(memberId);
    }
}
