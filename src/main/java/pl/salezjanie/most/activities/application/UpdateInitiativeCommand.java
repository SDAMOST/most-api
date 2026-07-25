package pl.salezjanie.most.activities.application;

import java.util.List;

public record UpdateInitiativeCommand(
        String name,
        String description,
        int defaultPoints,
        List<AddScheduleRuleCommand> scheduleRules
) {
}
