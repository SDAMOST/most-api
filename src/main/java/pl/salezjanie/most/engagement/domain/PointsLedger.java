package pl.salezjanie.most.engagement.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root for a member's points.
 * Calculates totals and enforces monthly caps.
 */
public class PointsLedger {

    private final UUID memberId;
    private final List<PointsTransaction> transactions;

    public PointsLedger(UUID memberId, List<PointsTransaction> transactions) {
        this.memberId = Objects.requireNonNull(memberId, "memberId must not be null");
        this.transactions = new ArrayList<>(transactions != null ? transactions : List.of());
    }

    /**
     * Attempts to add a new transaction. If the transaction exceeds the monthly cap for the unit,
     * the points are truncated so they don't exceed the cap.
     * If the resulting points are 0, the transaction is not added.
     *
     * @return the transaction that was added, or empty if 0 points were awarded.
     */
    public java.util.Optional<PointsTransaction> addTransaction(
            UUID occurrenceId, UUID unitId, int points, String reason, Integer monthlyPointsCap, Instant timestamp) {
        
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }

        int pointsToAward = points;

        if (monthlyPointsCap != null) {
            int currentMonthPointsForUnit = calculateMonthTotalForUnit(unitId, timestamp);
            int remainingCapacity = monthlyPointsCap - currentMonthPointsForUnit;

            if (remainingCapacity <= 0) {
                // Cap reached, cannot award any more points for this unit this month
                return java.util.Optional.empty();
            }

            if (pointsToAward > remainingCapacity) {
                pointsToAward = remainingCapacity;
            }
        }

        PointsTransaction transaction = new PointsTransaction(
                UUID.randomUUID(), memberId, occurrenceId, unitId, pointsToAward, reason, timestamp
        );
        transactions.add(transaction);
        return java.util.Optional.of(transaction);
    }

    private int calculateMonthTotalForUnit(UUID unitId, Instant timestamp) {
        YearMonth targetMonth = YearMonth.from(timestamp.atZone(ZoneId.systemDefault()));
        
        return transactions.stream()
                .filter(t -> t.getUnitId().equals(unitId))
                .filter(t -> YearMonth.from(t.getTimestamp().atZone(ZoneId.systemDefault())).equals(targetMonth))
                .mapToInt(PointsTransaction::getPoints)
                .sum();
    }

    public UUID getMemberId() {
        return memberId;
    }

    public int calculateTotalPoints() {
        return transactions.stream().mapToInt(PointsTransaction::getPoints).sum();
    }

    public List<PointsTransaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
}
