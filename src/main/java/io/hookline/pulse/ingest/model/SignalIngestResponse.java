package io.hookline.pulse.ingest.model;

public record SignalIngestResponse(
        String transactionId,
        String status,
        String sessionId,
        DeviceProfileDocument profile,
        LedgerEvaluationResult ledgerEvaluation,
        TelemetryBalanceResult telemetryBalance) {
}
