package pl.salezjanie.most.participation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.activities.domain.Occurrence;
import pl.salezjanie.most.activities.domain.OccurrenceRepository;
import pl.salezjanie.most.activities.domain.OccurrenceStatus;
import pl.salezjanie.most.participation.domain.Enrollment;
import pl.salezjanie.most.participation.domain.EnrollmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service for managing enrollments.
 *
 * <p>Enforces cross-context rules:
 * <ul>
 *   <li>No enrollment after the Occurrence has started.</li>
 *   <li>No duplicate enrollment for the same member + occurrence.</li>
 *   <li>Capacity limit when set on the Occurrence.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final OccurrenceRepository occurrenceRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             OccurrenceRepository occurrenceRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.occurrenceRepository = occurrenceRepository;
    }

    // ──────────────────────────────────────────────
    //  Commands
    // ──────────────────────────────────────────────

    @Transactional
    public EnrollmentView enroll(EnrollCommand command) {
        Occurrence occurrence = findOccurrenceOrThrow(command.occurrenceId());

        // Rule: no enrollment after occurrence has started
        if (occurrence.getScheduledStart().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot enroll after the occurrence has started");
        }

        // Rule: occurrence must be published to accept enrollments
        if (occurrence.getStatus() != OccurrenceStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Cannot enroll in an occurrence with status %s (expected PUBLISHED)".formatted(occurrence.getStatus()));
        }

        // Rule: no duplicate active enrollment
        if (enrollmentRepository.existsActiveByOccurrenceIdAndMemberId(command.occurrenceId(), command.memberId())) {
            throw new IllegalStateException("Member is already enrolled in this occurrence");
        }

        // Rule: capacity limit
        long currentCount = enrollmentRepository.countActiveByOccurrenceId(command.occurrenceId());
        if (!occurrence.hasCapacityFor(currentCount)) {
            throw new IllegalStateException("Occurrence has reached its enrollment capacity");
        }

        Enrollment enrollment = Enrollment.create(UUID.randomUUID(), command.occurrenceId(), command.memberId());
        return toView(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public EnrollmentView withdraw(UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));
        enrollment.withdraw();
        return toView(enrollmentRepository.save(enrollment));
    }

    // ──────────────────────────────────────────────
    //  Queries
    // ──────────────────────────────────────────────

    public List<EnrollmentView> findByOccurrence(UUID occurrenceId) {
        return enrollmentRepository.findByOccurrenceId(occurrenceId).stream()
                .map(EnrollmentService::toView)
                .toList();
    }

    public List<EnrollmentView> findByMember(UUID memberId) {
        return enrollmentRepository.findByMemberId(memberId).stream()
                .map(EnrollmentService::toView)
                .toList();
    }

    // ──────────────────────────────────────────────
    //  Internal
    // ──────────────────────────────────────────────

    private Occurrence findOccurrenceOrThrow(UUID occurrenceId) {
        return occurrenceRepository.findById(occurrenceId)
                .orElseThrow(() -> new IllegalArgumentException("Occurrence not found: " + occurrenceId));
    }

    private static EnrollmentView toView(Enrollment e) {
        return new EnrollmentView(
                e.getId(),
                e.getOccurrenceId(),
                e.getMemberId(),
                e.getStatus(),
                e.getEnrolledAt()
        );
    }
}
