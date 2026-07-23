package pl.salezjanie.most.activities.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.activities.domain.Initiative;
import pl.salezjanie.most.activities.domain.InitiativeRepository;
import pl.salezjanie.most.activities.domain.ScheduleRule;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Application service for managing initiatives and their schedule rules.
 */
@Service
@Transactional(readOnly = true)
public class InitiativeService {

    private final InitiativeRepository repository;

    public InitiativeService(InitiativeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public InitiativeView create(CreateInitiativeCommand command) {
        Initiative initiative = Initiative.create(
                UUID.randomUUID(),
                command.name(),
                command.description(),
                command.ownerUnitId()
        );
        return toView(repository.save(initiative));
    }

    @Transactional
    public InitiativeView addScheduleRule(UUID initiativeId, AddScheduleRuleCommand command) {
        Initiative initiative = findOrThrow(initiativeId);

        initiative.addScheduleRule(
                command.recurrenceType(),
                command.dayOfWeek(),
                command.startTime(),
                Duration.ofMinutes(command.durationMinutes()),
                command.effectiveFrom(),
                command.effectiveUntil()
        );

        return toView(repository.save(initiative));
    }

    public List<InitiativeView> findAll() {
        return repository.findAll().stream().map(InitiativeService::toView).toList();
    }

    public InitiativeView findById(UUID id) {
        return toView(findOrThrow(id));
    }

    private Initiative findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Initiative not found: " + id));
    }

    private static InitiativeView toView(Initiative i) {
        List<ScheduleRuleView> rules = i.getScheduleRules().stream()
                .map(InitiativeService::toRuleView)
                .toList();

        return new InitiativeView(i.getId(), i.getName(), i.getDescription(), i.getOwnerUnitId(), rules);
    }

    private static ScheduleRuleView toRuleView(ScheduleRule r) {
        return new ScheduleRuleView(
                r.getId(),
                r.getRecurrenceType(),
                r.getDayOfWeek(),
                r.getStartTime(),
                (int) r.getDuration().toMinutes(),
                r.getEffectiveFrom(),
                r.getEffectiveUntil()
        );
    }
}
