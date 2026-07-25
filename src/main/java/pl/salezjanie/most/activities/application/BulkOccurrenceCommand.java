package pl.salezjanie.most.activities.application;

import java.time.LocalDateTime;

public record BulkOccurrenceCommand(LocalDateTime scheduledStart, LocalDateTime scheduledEnd) {}
