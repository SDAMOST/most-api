package pl.salezjanie.most.activities.application;

import org.springframework.stereotype.Component;
import pl.salezjanie.most.activities.domain.Initiative;
import pl.salezjanie.most.activities.domain.Occurrence;
import pl.salezjanie.most.activities.domain.ScheduleRule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generates {@link Occurrence} entities from an {@link Initiative}'s schedule rules
 * for a given date range.
 *
 * <p>Implements the domain rule: "Generator tworzy Occurrence wg ScheduleRule."
 */
@Component
public class OccurrenceGenerator {

    /**
     * Generates occurrences for the given initiative within [from, to].
     *
     * @param initiative the initiative whose schedule rules to use
     * @param from       start of the generation range (inclusive)
     * @param to         end of the generation range (inclusive)
     * @return a list of new (unsaved) Occurrence entities
     */
    public List<Occurrence> generate(Initiative initiative, LocalDate from, LocalDate to) {
        List<Occurrence> occurrences = new ArrayList<>();

        for (ScheduleRule rule : initiative.getScheduleRules()) {
            // Find the first matching day on or after 'from'
            LocalDate current = from.with(java.time.temporal.TemporalAdjusters.nextOrSame(rule.getDayOfWeek()));

            while (!current.isAfter(to)) {
                if (rule.isEffectiveOn(current)) {
                    LocalDateTime start = current.atTime(rule.getStartTime());
                    LocalDateTime end = start.plus(rule.getDuration());

                    Occurrence occurrence = Occurrence.create(
                            UUID.randomUUID(),
                            initiative.getId(),
                            start,
                            end
                    );
                    occurrences.add(occurrence);
                }

                // Advance by the rule's step (1 week or 2 weeks)
                current = current.plusWeeks(rule.weekStep());
            }
        }

        return occurrences;
    }
}
