package pl.salezjanie.most.participation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.activities.domain.Occurrence;
import pl.salezjanie.most.activities.domain.OccurrenceRepository;
import pl.salezjanie.most.activities.domain.OccurrenceStatus;
import pl.salezjanie.most.participation.domain.Attendance;
import pl.salezjanie.most.participation.domain.AttendanceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Application service for managing attendance records.
 *
 * <p>Attendance does not require a preceding enrollment — some initiatives
 * (e.g. sprzątanie, schola) allow attendance without prior sign-up.
 */
@Service
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final OccurrenceRepository occurrenceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             OccurrenceRepository occurrenceRepository) {
        this.attendanceRepository = attendanceRepository;
        this.occurrenceRepository = occurrenceRepository;
    }

    // ──────────────────────────────────────────────
    //  Commands
    // ──────────────────────────────────────────────

    /**
     * Records attendance for multiple members at once (leader bulk action).
     */
    @Transactional
    public List<AttendanceView> recordBulk(RecordAttendanceCommand command) {
        Occurrence occurrence = findOccurrenceOrThrow(command.occurrenceId());
        validateOccurrenceForAttendance(occurrence);

        List<Attendance> records = new ArrayList<>();
        for (UUID memberId : command.memberIds()) {
            if (attendanceRepository.existsByOccurrenceIdAndMemberId(command.occurrenceId(), memberId)) {
                continue; // Skip already-recorded attendance (idempotent)
            }
            records.add(Attendance.record(UUID.randomUUID(), command.occurrenceId(), memberId));
        }

        return attendanceRepository.saveAll(records).stream()
                .map(AttendanceService::toView)
                .toList();
    }

    /**
     * Records attendance for a single member (self-report).
     */
    @Transactional
    public AttendanceView recordSelf(UUID occurrenceId, UUID memberId) {
        Occurrence occurrence = findOccurrenceOrThrow(occurrenceId);
        validateOccurrenceForAttendance(occurrence);

        if (attendanceRepository.existsByOccurrenceIdAndMemberId(occurrenceId, memberId)) {
            throw new IllegalStateException("Attendance already recorded for this member");
        }

        Attendance attendance = Attendance.record(UUID.randomUUID(), occurrenceId, memberId);
        return toView(attendanceRepository.save(attendance));
    }

    // ──────────────────────────────────────────────
    //  Queries
    // ──────────────────────────────────────────────

    public List<AttendanceView> findByOccurrence(UUID occurrenceId) {
        return attendanceRepository.findByOccurrenceId(occurrenceId).stream()
                .map(AttendanceService::toView)
                .toList();
    }

    // ──────────────────────────────────────────────
    //  Internal
    // ──────────────────────────────────────────────

    private Occurrence findOccurrenceOrThrow(UUID occurrenceId) {
        return occurrenceRepository.findById(occurrenceId)
                .orElseThrow(() -> new IllegalArgumentException("Occurrence not found: " + occurrenceId));
    }

    private void validateOccurrenceForAttendance(Occurrence occurrence) {
        if (occurrence.getStatus() != OccurrenceStatus.COMPLETED
                && occurrence.getStatus() != OccurrenceStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Cannot record attendance for occurrence in %s status".formatted(occurrence.getStatus()));
        }
    }

    private static AttendanceView toView(Attendance a) {
        return new AttendanceView(
                a.getId(),
                a.getOccurrenceId(),
                a.getMemberId(),
                a.getRecordedAt()
        );
    }
}
