package io.hookline.pulse.ingest.model;

import java.math.BigDecimal;

public record LedgerEvaluationResult(
        String ledgerId,
        String evaluatedAt,
        BigDecimal amount,
        BigDecimal variance,
        boolean anomaly) {
}
