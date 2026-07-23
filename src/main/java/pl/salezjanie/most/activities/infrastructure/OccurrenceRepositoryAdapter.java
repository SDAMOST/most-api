package pl.salezjanie.most.activities.infrastructure;

import org.springframework.stereotype.Repository;
import pl.salezjanie.most.activities.domain.Occurrence;
import pl.salezjanie.most.activities.domain.OccurrenceRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class OccurrenceRepositoryAdapter implements OccurrenceRepository {

    private final JpaOccurrenceRepository jpa;

    OccurrenceRepositoryAdapter(JpaOccurrenceRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Occurrence save(Occurrence occurrence) {
        return jpa.save(occurrence);
    }

    @Override
    public List<Occurrence> saveAll(List<Occurrence> occurrences) {
        return jpa.saveAll(occurrences);
    }

    @Override
    public Optional<Occurrence> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Occurrence> findByScheduledStartBetween(LocalDateTime from, LocalDateTime to) {
        return jpa.findByScheduledStartBetween(from, to);
    }

    @Override
    public List<Occurrence> findByInitiativeId(UUID initiativeId) {
        return jpa.findByInitiativeId(initiativeId);
    }
}
