package pl.salezjanie.most.participation.infrastructure;

import org.springframework.stereotype.Repository;
import pl.salezjanie.most.participation.domain.Enrollment;
import pl.salezjanie.most.participation.domain.EnrollmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class EnrollmentRepositoryAdapter implements EnrollmentRepository {

    private final JpaEnrollmentRepository jpa;

    EnrollmentRepositoryAdapter(JpaEnrollmentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        return jpa.save(enrollment);
    }

    @Override
    public Optional<Enrollment> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Enrollment> findByOccurrenceId(UUID occurrenceId) {
        return jpa.findByOccurrenceId(occurrenceId);
    }

    @Override
    public List<Enrollment> findByMemberId(UUID memberId) {
        return jpa.findByMemberId(memberId);
    }

    @Override
    public boolean existsByOccurrenceIdAndMemberId(UUID occurrenceId, UUID memberId) {
        return jpa.existsByOccurrenceIdAndMemberId(occurrenceId, memberId);
    }

    @Override
    public boolean existsActiveByOccurrenceIdAndMemberId(UUID occurrenceId, UUID memberId) {
        return jpa.existsActiveByOccurrenceIdAndMemberId(occurrenceId, memberId);
    }

    @Override
    public long countActiveByOccurrenceId(UUID occurrenceId) {
        return jpa.countActiveByOccurrenceId(occurrenceId);
    }
}
