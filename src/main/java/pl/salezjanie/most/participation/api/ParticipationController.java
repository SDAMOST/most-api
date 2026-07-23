package pl.salezjanie.most.participation.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.salezjanie.most.participation.application.AttendanceService;
import pl.salezjanie.most.participation.application.AttendanceView;
import pl.salezjanie.most.participation.application.EnrollCommand;
import pl.salezjanie.most.participation.application.EnrollmentService;
import pl.salezjanie.most.participation.application.EnrollmentView;
import pl.salezjanie.most.participation.application.RecordAttendanceCommand;

import java.util.List;
import java.util.UUID;

/**
 * REST API for the Participation bounded context (Enrollments &amp; Attendance).
 */
@RestController
@RequestMapping("/api")
class ParticipationController {

    private final EnrollmentService enrollmentService;
    private final AttendanceService attendanceService;

    ParticipationController(EnrollmentService enrollmentService, AttendanceService attendanceService) {
        this.enrollmentService = enrollmentService;
        this.attendanceService = attendanceService;
    }

    // ── Enrollments ────────────────────────────────

    @PostMapping("/occurrences/{occurrenceId}/enrollments")
    ResponseEntity<EnrollmentView> enroll(@PathVariable UUID occurrenceId,
                                          @AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        EnrollCommand command = new EnrollCommand(occurrenceId, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(command));
    }

    @DeleteMapping("/enrollments/{id}")
    EnrollmentView withdraw(@PathVariable UUID id) {
        return enrollmentService.withdraw(id);
    }

    @GetMapping("/occurrences/{occurrenceId}/enrollments")
    List<EnrollmentView> listEnrollments(@PathVariable UUID occurrenceId) {
        return enrollmentService.findByOccurrence(occurrenceId);
    }

    // ── Attendance ─────────────────────────────────

    @PostMapping("/occurrences/{occurrenceId}/attendance")
    ResponseEntity<List<AttendanceView>> recordAttendance(@PathVariable UUID occurrenceId,
                                                          @RequestBody RecordAttendanceCommand command) {
        RecordAttendanceCommand fullCommand = new RecordAttendanceCommand(occurrenceId, command.memberIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.recordBulk(fullCommand));
    }

    @PostMapping("/occurrences/{occurrenceId}/attendance/me")
    ResponseEntity<AttendanceView> recordSelfAttendance(@PathVariable UUID occurrenceId,
                                                        @AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.recordSelf(occurrenceId, memberId));
    }

    @GetMapping("/occurrences/{occurrenceId}/attendance")
    List<AttendanceView> listAttendance(@PathVariable UUID occurrenceId) {
        return attendanceService.findByOccurrence(occurrenceId);
    }
}
