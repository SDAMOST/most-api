package pl.salezjanie.most.activities.api;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.salezjanie.most.activities.application.AddScheduleRuleCommand;
import pl.salezjanie.most.activities.application.BulkOccurrenceCommand;
import pl.salezjanie.most.activities.application.CreateInitiativeCommand;
import pl.salezjanie.most.activities.application.InitiativeService;
import pl.salezjanie.most.activities.application.InitiativeView;
import pl.salezjanie.most.activities.application.OccurrenceService;
import pl.salezjanie.most.activities.application.OccurrenceView;
import pl.salezjanie.most.activities.application.RescheduleCommand;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST API for the Activities bounded context.
 */
@RestController
@RequestMapping("/api")
class ActivitiesController {

    private final InitiativeService initiativeService;
    private final OccurrenceService occurrenceService;

    ActivitiesController(InitiativeService initiativeService, OccurrenceService occurrenceService) {
        this.initiativeService = initiativeService;
        this.occurrenceService = occurrenceService;
    }

    // ── Initiatives ────────────────────────────────

    @PostMapping("/initiatives")
    ResponseEntity<InitiativeView> createInitiative(@RequestBody CreateInitiativeCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(initiativeService.create(command));
    }

    @GetMapping("/initiatives")
    List<InitiativeView> listInitiatives() {
        return initiativeService.findAll();
    }

    @GetMapping("/initiatives/{id}")
    InitiativeView getInitiative(@PathVariable UUID id) {
        return initiativeService.findById(id);
    }

    @PutMapping("/initiatives/{id}")
    InitiativeView updateInitiative(@PathVariable UUID id, @RequestBody pl.salezjanie.most.activities.application.UpdateInitiativeCommand command) {
        return initiativeService.update(id, command);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/initiatives/{id}")
    ResponseEntity<Void> deleteInitiative(@PathVariable UUID id) {
        initiativeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initiatives/{id}/schedule-rules")
    ResponseEntity<InitiativeView> addScheduleRule(@PathVariable UUID id,
                                                    @RequestBody AddScheduleRuleCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(initiativeService.addScheduleRule(id, command));
    }

    @PostMapping("/initiatives/{id}/generate")
    ResponseEntity<List<OccurrenceView>> generateOccurrences(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.status(HttpStatus.CREATED).body(occurrenceService.generateForInitiative(id, from, to));
    }

    @PostMapping("/initiatives/{id}/occurrences/bulk")
    ResponseEntity<List<OccurrenceView>> createBulkOccurrences(
            @PathVariable UUID id,
            @RequestBody List<BulkOccurrenceCommand> commands) {
        return ResponseEntity.status(HttpStatus.CREATED).body(occurrenceService.createBulk(id, commands));
    }

    // ── Occurrences (Calendar) ─────────────────────

    @GetMapping("/occurrences")
    List<OccurrenceView> calendarView(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return occurrenceService.findInRange(from, to);
    }

    @PutMapping("/occurrences/{id}/publish")
    OccurrenceView publish(@PathVariable UUID id) {
        return occurrenceService.publish(id);
    }

    @PutMapping("/occurrences/{id}/complete")
    OccurrenceView complete(@PathVariable UUID id) {
        return occurrenceService.complete(id);
    }

    @PutMapping("/occurrences/{id}/cancel")
    OccurrenceView cancel(@PathVariable UUID id) {
        return occurrenceService.cancel(id);
    }

    @PutMapping("/occurrences/{id}/reschedule")
    OccurrenceView reschedule(@PathVariable UUID id, @RequestBody RescheduleCommand command) {
        return occurrenceService.reschedule(id, command);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/occurrences/{id}")
    ResponseEntity<Void> deleteOccurrence(@PathVariable UUID id) {
        occurrenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
