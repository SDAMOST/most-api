package pl.salezjanie.most.participation.infrastructure;

import org.springframework.stereotype.Repository;
import pl.salezjanie.most.participation.domain.Attendance;
import pl.salezjanie.most.participation.domain.AttendanceRepository;

import java.util.List;
import java.util.UUID;

@Repository
class AttendanceRepositoryAdapter implements AttendanceRepository {

    private final JpaAttendanceRepository jpa;

    AttendanceRepositoryAdapter(JpaAttendanceRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Attendance save(Attendance attendance) {
        return jpa.save(attendance);
    }

    @Override
    public List<Attendance> saveAll(List<Attendance> attendances) {
        return jpa.saveAll(attendances);
    }

    @Override
    public List<Attendance> findByOccurrenceId(UUID occurrenceId) {
        return jpa.findByOccurrenceId(occurrenceId);
    }

    @Override
    public boolean existsByOccurrenceIdAndMemberId(UUID occurrenceId, UUID memberId) {
        return jpa.existsByOccurrenceIdAndMemberId(occurrenceId, memberId);
    }
}
