package io.hookline.pulse.ingest.model;

public record DeviceProfileDocument(
        String deviceId,
        String firmwareVersion,
        String region,
        String signalBand,
        boolean informEnabled) {
}
