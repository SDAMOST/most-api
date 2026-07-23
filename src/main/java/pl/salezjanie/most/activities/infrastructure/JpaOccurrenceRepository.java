package pl.salezjanie.most.activities.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.salezjanie.most.activities.domain.Occurrence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

interface JpaOccurrenceRepository extends JpaRepository<Occurrence, UUID> {

    List<Occurrence> findByScheduledStartBetween(LocalDateTime from, LocalDateTime to);

    List<Occurrence> findByInitiativeId(UUID initiativeId);
}
