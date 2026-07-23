package pl.salezjanie.most.participation.application;

import java.util.List;
import java.util.UUID;

/**
 * Command to record attendance for multiple members at an occurrence (leader bulk action).
 */
public record RecordAttendanceCommand(UUID occurrenceId, List<UUID> memberIds) {
}
