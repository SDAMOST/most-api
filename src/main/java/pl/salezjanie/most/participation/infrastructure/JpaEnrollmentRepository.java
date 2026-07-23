package pl.salezjanie.most.participation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.salezjanie.most.participation.domain.Enrollment;

import java.util.List;
import java.util.UUID;

interface JpaEnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findByOccurrenceId(UUID occurrenceId);

    List<Enrollment> findByMemberId(UUID memberId);

    boolean existsByOccurrenceIdAndMemberId(UUID occurrenceId, UUID memberId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e " +
           "WHERE e.occurrenceId = :occurrenceId AND e.memberId = :memberId AND e.status = 'ENROLLED'")
    boolean existsActiveByOccurrenceIdAndMemberId(UUID occurrenceId, UUID memberId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.occurrenceId = :occurrenceId AND e.status = 'ENROLLED'")
    long countActiveByOccurrenceId(UUID occurrenceId);
}
