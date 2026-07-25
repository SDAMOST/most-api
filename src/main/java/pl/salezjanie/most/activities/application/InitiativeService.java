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
    private final pl.salezjanie.most.activities.domain.OccurrenceRepository occurrenceRepository;

    public InitiativeService(InitiativeRepository repository, pl.salezjanie.most.activities.domain.OccurrenceRepository occurrenceRepository) {
        this.repository = repository;
        this.occurrenceRepository = occurrenceRepository;
    }

    @Transactional
    public InitiativeView create(CreateInitiativeCommand command) {
        Initiative initiative = Initiative.create(
                UUID.randomUUID(),
                command.name(),
                command.description(),
                command.ownerUnitId(),
                command.requiresEnrollment()
        );
        return toView(repository.save(initiative));
    }

    @Transactional
    public InitiativeView update(UUID id, UpdateInitiativeCommand command) {
        Initiative initiative = findOrThrow(id);
        boolean requiresEnrollmentChanged = initiative.isRequiresEnrollment() != command.requiresEnrollment();
        
        initiative.update(command.name(), command.description(), command.defaultPoints(), command.requiresEnrollment());

        // Update schedule rules (simple replacement strategy)
        // First, clear all existing rules by finding their IDs and removing them
        List<UUID> existingRuleIds = initiative.getScheduleRules().stream()
                .map(ScheduleRule::getId)
                .toList();
        for (UUID ruleId : existingRuleIds) {
            initiative.removeScheduleRule(ruleId);
        }

        // Add new rules
        if (command.scheduleRules() != null) {
            for (AddScheduleRuleCommand ruleCommand : command.scheduleRules()) {
                initiative.addScheduleRule(
                        ruleCommand.recurrenceType(),
                        ruleCommand.dayOfWeek(),
                        ruleCommand.startTime(),
                        Duration.ofMinutes(ruleCommand.durationMinutes()),
                        ruleCommand.effectiveFrom(),
                        ruleCommand.effectiveUntil()
                );
            }
        }

        if (requiresEnrollmentChanged) {
            List<pl.salezjanie.most.activities.domain.Occurrence> occurrences = occurrenceRepository.findByInitiativeId(id);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (pl.salezjanie.most.activities.domain.Occurrence occ : occurrences) {
                if (occ.getScheduledStart().isAfter(now)) {
                    occ.setRequiresEnrollment(command.requiresEnrollment());
                }
            }
            occurrenceRepository.saveAll(occurrences);
        }

        return toView(repository.save(initiative));
    }

    @Transactional
    public void delete(UUID id) {
        Initiative initiative = findOrThrow(id);
        occurrenceRepository.deleteByInitiativeId(id);
        repository.delete(initiative);
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

        return new InitiativeView(i.getId(), i.getName(), i.getDescription(), i.getOwnerUnitId(), i.isRequiresEnrollment(), rules);
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
