package pl.salezjanie.most.participation.domain;

import java.util.List;
import java.util.UUID;

/**
 * Domain repository contract for the {@link Attendance} aggregate.
 */
public interface AttendanceRepository {

    Attendance save(Attendance attendance);

    List<Attendance> saveAll(List<Attendance> attendances);

    List<Attendance> findByOccurrenceId(UUID occurrenceId);

    boolean existsByOccurrenceIdAndMemberId(UUID occurrenceId, UUID memberId);
}
