package pl.salezjanie.most.participation.application;

import pl.salezjanie.most.participation.domain.EnrollmentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only view of an enrollment.
 */
public record EnrollmentView(
        UUID id,
        UUID occurrenceId,
        UUID memberId,
        EnrollmentStatus status,
        Instant enrolledAt
) {
}
