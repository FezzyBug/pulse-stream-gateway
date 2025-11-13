package io.hookline.pulse.ingest.model;

import jakarta.validation.constraints.NotBlank;

public record SignalRequest(
        @NotBlank String assetId,
        @NotBlank String sessionId,
        @NotBlank String codec,
        String envelope,
        String schemaVersion,
        String routeHint,
        String stage) {

    public SignalRequest {
        if (schemaVersion == null || schemaVersion.isBlank()) {
            schemaVersion = "1.0";
        }
        if (routeHint == null || routeHint.isBlank()) {
            routeHint = "edge-east";
        }
        if (stage == null || stage.isBlank()) {
            stage = "edge";
        }
    }
}
