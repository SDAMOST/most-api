package pl.salezjanie.most.activities.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for the {@link Initiative} aggregate.
 */
public interface InitiativeRepository {

    Initiative save(Initiative initiative);

    Optional<Initiative> findById(UUID id);

    List<Initiative> findAllById(Iterable<UUID> ids);

    List<Initiative> findAll();

    void delete(Initiative initiative);
}
