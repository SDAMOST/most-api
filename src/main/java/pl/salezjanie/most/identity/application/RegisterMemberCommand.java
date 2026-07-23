package pl.salezjanie.most.identity.application;

import java.util.UUID;

/**
 * Command to register a new community member.
 */
public record RegisterMemberCommand(
        String password,
        String displayName,
        String email
) {
}
