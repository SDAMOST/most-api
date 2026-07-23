package pl.salezjanie.most.activities.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.salezjanie.most.activities.domain.Initiative;

import java.util.UUID;

interface JpaInitiativeRepository extends JpaRepository<Initiative, UUID> {
}
