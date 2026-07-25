package pl.salezjanie.most.activities.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for the {@link Occurrence} aggregate.
 */
public interface OccurrenceRepository {

    Occurrence save(Occurrence occurrence);

    List<Occurrence> saveAll(List<Occurrence> occurrences);

    Optional<Occurrence> findById(UUID id);

    /**
     * Finds all occurrences whose scheduled start falls within the given range.
     */
    List<Occurrence> findByScheduledStartBetween(LocalDateTime from, LocalDateTime to);

    /**
     * Finds all occurrences for a given initiative.
     */
    List<Occurrence> findByInitiativeId(UUID initiativeId);

    List<Occurrence> findByScheduledEndBeforeAndStatusIn(LocalDateTime before, List<OccurrenceStatus> statuses);

    /**
     * Deletes all occurrences for a given initiative.
     */
    void deleteByInitiativeId(UUID initiativeId);
}
