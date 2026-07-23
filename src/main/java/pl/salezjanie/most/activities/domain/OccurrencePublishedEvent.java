package pl.salezjanie.most.activities.domain;

import java.time.Instant;
import java.util.UUID;

public record OccurrencePublishedEvent(
        UUID occurrenceId,
        UUID initiativeId,
        Instant publishedAt
) {
}
