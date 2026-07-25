package pl.salezjanie.most.activities.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.activities.domain.Occurrence;
import pl.salezjanie.most.activities.domain.OccurrenceRepository;
import pl.salezjanie.most.activities.domain.OccurrenceStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
class AutoCompletionJob {

    private static final Logger log = LoggerFactory.getLogger(AutoCompletionJob.class);

    private final OccurrenceRepository occurrenceRepository;

    AutoCompletionJob(OccurrenceRepository occurrenceRepository) {
        this.occurrenceRepository = occurrenceRepository;
    }

    @Scheduled(cron = "0 0/5 * * * ?") // Every 5 minutes
    @Transactional
    public void autoCompleteOccurrences() {
        LocalDateTime now = LocalDateTime.now();
        List<Occurrence> pastOccurrences = occurrenceRepository.findByScheduledEndBeforeAndStatusIn(
                now,
                List.of(OccurrenceStatus.PLANNED, OccurrenceStatus.PUBLISHED)
        );

        int completedCount = 0;
        int cancelledCount = 0;

        for (Occurrence occurrence : pastOccurrences) {
            try {
                if (occurrence.getStatus() == OccurrenceStatus.PUBLISHED) {
                    occurrence.complete();
                    completedCount++;
                } else if (occurrence.getStatus() == OccurrenceStatus.PLANNED) {
                    occurrence.cancel();
                    cancelledCount++;
                }
            } catch (Exception e) {
                log.error("Failed to auto-process occurrence {}", occurrence.getId(), e);
            }
        }

        if (completedCount > 0 || cancelledCount > 0) {
            occurrenceRepository.saveAll(pastOccurrences);
            log.info("Auto-processed occurrences: {} completed, {} cancelled", completedCount, cancelledCount);
        }
    }
}
