package pl.salezjanie.most.participation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.salezjanie.most.participation.domain.Attendance;

import java.util.List;
import java.util.UUID;

interface JpaAttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findByOccurrenceId(UUID occurrenceId);

    boolean existsByOccurrenceIdAndMemberId(UUID occurrenceId, UUID memberId);
}
