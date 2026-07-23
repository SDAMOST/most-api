package pl.salezjanie.most.engagement.application;

import pl.salezjanie.most.engagement.domain.PointsLedger;

import java.util.List;
import java.util.UUID;

public record PointsLedgerView(
        UUID memberId,
        int totalPoints,
        List<PointsTransactionView> transactions
) {
    public static PointsLedgerView from(PointsLedger ledger) {
        return new PointsLedgerView(
                ledger.getMemberId(),
                ledger.calculateTotalPoints(),
                ledger.getTransactions().stream()
                        .map(PointsTransactionView::from)
                        .toList()
        );
    }
}
