package pl.salezjanie.most.activities.infrastructure;

import org.springframework.stereotype.Repository;
import pl.salezjanie.most.activities.domain.Initiative;
import pl.salezjanie.most.activities.domain.InitiativeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class InitiativeRepositoryAdapter implements InitiativeRepository {

    private final JpaInitiativeRepository jpa;

    InitiativeRepositoryAdapter(JpaInitiativeRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Initiative save(Initiative initiative) {
        return jpa.save(initiative);
    }

    @Override
    public Optional<Initiative> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Initiative> findAll() {
        return jpa.findAll();
    }
}
