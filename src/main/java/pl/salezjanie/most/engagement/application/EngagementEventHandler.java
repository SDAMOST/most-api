package pl.salezjanie.most.engagement.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.salezjanie.most.activities.domain.Initiative;
import pl.salezjanie.most.activities.domain.InitiativeRepository;
import pl.salezjanie.most.activities.domain.Occurrence;
import pl.salezjanie.most.activities.domain.OccurrenceRepository;
import pl.salezjanie.most.participation.domain.AttendanceRecordedEvent;
import pl.salezjanie.most.structure.domain.OrganizationUnit;
import pl.salezjanie.most.structure.domain.OrganizationUnitRepository;

@Component
public class EngagementEventHandler {

    private static final Logger log = LoggerFactory.getLogger(EngagementEventHandler.class);

    private final EngagementService engagementService;
    private final OccurrenceRepository occurrenceRepository;
    private final InitiativeRepository initiativeRepository;
    private final OrganizationUnitRepository unitRepository;

    public EngagementEventHandler(EngagementService engagementService,
                                  OccurrenceRepository occurrenceRepository,
                                  InitiativeRepository initiativeRepository,
                                  OrganizationUnitRepository unitRepository) {
        this.engagementService = engagementService;
        this.occurrenceRepository = occurrenceRepository;
        this.initiativeRepository = initiativeRepository;
        this.unitRepository = unitRepository;
    }

    /**
     * Listens to AttendanceRecordedEvent to automatically award points.
     * Uses BEFORE_COMMIT to ensure points are saved in the same transaction as the attendance.
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional
    public void handle(AttendanceRecordedEvent event) {
        log.debug("Processing AttendanceRecordedEvent for member {} and occurrence {}", event.memberId(), event.occurrenceId());

        Occurrence occurrence = occurrenceRepository.findById(event.occurrenceId())
                .orElseThrow(() -> new IllegalStateException("Occurrence not found: " + event.occurrenceId()));

        Initiative initiative = initiativeRepository.findById(occurrence.getInitiativeId())
                .orElseThrow(() -> new IllegalStateException("Initiative not found: " + occurrence.getInitiativeId()));

        OrganizationUnit unit = unitRepository.findById(initiative.getOwnerUnitId())
                .orElseThrow(() -> new IllegalStateException("Unit not found: " + initiative.getOwnerUnitId()));

        int points = initiative.getDefaultPoints();
        Integer cap = unit.getMonthlyPointsCap();
        String reason = "Attendance at " + initiative.getName();

        engagementService.awardPoints(
                event.memberId(),
                event.occurrenceId(),
                unit.getId(),
                points,
                cap,
                reason,
                event.recordedAt()
        );
    }
}
