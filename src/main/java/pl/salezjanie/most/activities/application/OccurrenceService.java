package pl.salezjanie.most.activities.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.activities.domain.Initiative;
import pl.salezjanie.most.activities.domain.InitiativeRepository;
import pl.salezjanie.most.activities.domain.Occurrence;
import pl.salezjanie.most.activities.domain.OccurrenceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service for managing occurrences and the calendar.
 */
@Service
@Transactional(readOnly = true)
public class OccurrenceService {

    private final OccurrenceRepository occurrenceRepository;
    private final InitiativeRepository initiativeRepository;
    private final OccurrenceGenerator generator;

    public OccurrenceService(OccurrenceRepository occurrenceRepository,
                             InitiativeRepository initiativeRepository,
                             OccurrenceGenerator generator) {
        this.occurrenceRepository = occurrenceRepository;
        this.initiativeRepository = initiativeRepository;
        this.generator = generator;
    }

    // ──────────────────────────────────────────────
    //  Commands
    // ──────────────────────────────────────────────

    /**
     * Generates occurrences for a given initiative within the specified date range.
     */
    @Transactional
    public List<OccurrenceView> generateForInitiative(UUID initiativeId, LocalDate from, LocalDate to) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new IllegalArgumentException("Initiative not found: " + initiativeId));

        List<Occurrence> generated = generator.generate(initiative, from, to);
        List<Occurrence> saved = occurrenceRepository.saveAll(generated);

        return saved.stream()
                .map(o -> toView(o, initiative.getName()))
                .toList();
    }

    @Transactional
    public OccurrenceView publish(UUID occurrenceId) {
        Occurrence occurrence = findOrThrow(occurrenceId);
        occurrence.publish();
        return toViewWithName(occurrenceRepository.save(occurrence));
    }

    @Transactional
    public OccurrenceView complete(UUID occurrenceId) {
        Occurrence occurrence = findOrThrow(occurrenceId);
        occurrence.complete();
        return toViewWithName(occurrenceRepository.save(occurrence));
    }

    @Transactional
    public OccurrenceView cancel(UUID occurrenceId) {
        Occurrence occurrence = findOrThrow(occurrenceId);
        occurrence.cancel();
        return toViewWithName(occurrenceRepository.save(occurrence));
    }

    @Transactional
    public OccurrenceView reschedule(UUID occurrenceId, RescheduleCommand command) {
        Occurrence occurrence = findOrThrow(occurrenceId);
        occurrence.reschedule(command.newStart(), command.newEnd(), command.reason());
        return toViewWithName(occurrenceRepository.save(occurrence));
    }

    // ──────────────────────────────────────────────
    //  Queries
    // ──────────────────────────────────────────────

    /**
     * Calendar view — returns all occurrences in a date range with initiative names.
     */
    public List<OccurrenceView> findInRange(LocalDate from, LocalDate to) {
        LocalDateTime fromStart = from.atStartOfDay();
        LocalDateTime toEnd = to.plusDays(1).atStartOfDay();

        List<Occurrence> occurrences = occurrenceRepository.findByScheduledStartBetween(fromStart, toEnd);

        // Batch-load initiative names to avoid N+1
        List<UUID> initiativeIds = occurrences.stream()
                .map(Occurrence::getInitiativeId)
                .distinct()
                .toList();

        Map<UUID, String> nameById = initiativeRepository.findAll().stream()
                .filter(i -> initiativeIds.contains(i.getId()))
                .collect(Collectors.toMap(Initiative::getId, Initiative::getName));

        return occurrences.stream()
                .map(o -> toView(o, nameById.getOrDefault(o.getInitiativeId(), "Unknown")))
                .toList();
    }

    // ──────────────────────────────────────────────
    //  Internal
    // ──────────────────────────────────────────────

    private Occurrence findOrThrow(UUID id) {
        return occurrenceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Occurrence not found: " + id));
    }

    private OccurrenceView toViewWithName(Occurrence o) {
        String name = initiativeRepository.findById(o.getInitiativeId())
                .map(Initiative::getName)
                .orElse("Unknown");
        return toView(o, name);
    }

    private static OccurrenceView toView(Occurrence o, String initiativeName) {
        return new OccurrenceView(
                o.getId(),
                o.getInitiativeId(),
                initiativeName,
                o.getScheduledStart(),
                o.getScheduledEnd(),
                o.getStatus()
        );
    }
}
