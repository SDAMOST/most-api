package pl.salezjanie.most.participation.application;

import java.util.UUID;

/**
 * Command to enroll a member in an occurrence.
 */
public record EnrollCommand(UUID occurrenceId, UUID memberId) {
}
