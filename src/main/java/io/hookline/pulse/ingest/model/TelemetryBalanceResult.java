package io.hookline.pulse.ingest.model;

public record TelemetryBalanceResult(
        String clusterId,
        String completedAt,
        double variance,
        String action) {
}
