package pl.salezjanie.most.participation.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for the {@link Enrollment} aggregate.
 */
public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    Optional<Enrollment> findById(UUID id);

    List<Enrollment> findByOccurrenceId(UUID occurrenceId);

    List<Enrollment> findByMemberId(UUID memberId);

    boolean existsByOccurrenceIdAndMemberId(UUID occurrenceId, UUID memberId);

    /**
     * Checks whether an active (ENROLLED) enrollment exists for the given occurrence and member.
     */
    boolean existsActiveByOccurrenceIdAndMemberId(UUID occurrenceId, UUID memberId);

    /**
     * Counts active enrollments (status = ENROLLED) for a given occurrence.
     */
    long countActiveByOccurrenceId(UUID occurrenceId);
}
