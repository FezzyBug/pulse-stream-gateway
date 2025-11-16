package io.hookline.pulse.ingest.controller;

import io.hookline.pulse.ingest.client.ProfileDirectoryClient;
import io.hookline.pulse.ingest.client.ResourceTelemetryClient;
import io.hookline.pulse.ingest.client.SettlementEngineClient;
import io.hookline.pulse.ingest.model.DeviceProfileDocument;
import io.hookline.pulse.ingest.model.LedgerEvaluationResult;
import io.hookline.pulse.ingest.model.SignalIngestResponse;
import io.hookline.pulse.ingest.model.SignalRequest;
import io.hookline.pulse.ingest.model.TelemetryBalanceResult;
import io.hookline.pulse.ingest.support.TransactionContextHolder;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/signals")
@Validated
public class StreamGateway {

    private static final Logger logger = LoggerFactory.getLogger(StreamGateway.class);

    private final ProfileDirectoryClient profileDirectoryClient;
    private final SettlementEngineClient settlementEngineClient;
    private final ResourceTelemetryClient resourceTelemetryClient;

    public StreamGateway(
            ProfileDirectoryClient profileDirectoryClient,
            SettlementEngineClient settlementEngineClient,
            ResourceTelemetryClient resourceTelemetryClient) {
        this.profileDirectoryClient = profileDirectoryClient;
        this.settlementEngineClient = settlementEngineClient;
        this.resourceTelemetryClient = resourceTelemetryClient;
    }

    @PostMapping("/ingest")
    public ResponseEntity<SignalIngestResponse> ingest(@RequestBody @Valid SignalRequest request) {
        logger.info("######### Signal handshake accepted for asset={} session={} codec={}",
                request.assetId(), request.sessionId(), request.codec());
        logger.info("######### Signal normalization running envelope={} codec={} version={}",
                request.envelope(), request.codec(), request.schemaVersion());

        DeviceProfileDocument profile = profileDirectoryClient.fetchProfile(request.assetId());
        logger.info("######### Signal enrichment completed signalBand={} region={}",
                profile.signalBand(), profile.region());

        BigDecimal evaluatedAmount = deriveLedgerAmount(request.assetId(), request.sessionId());
        LedgerEvaluationResult ledgerEvaluation = settlementEngineClient.evaluate(
                buildLedgerId(request.assetId()),
                evaluatedAmount,
                request.stage());

        double varianceTarget = deriveVarianceTarget(request.assetId());
        double currentLoad = deriveCurrentLoad(request.routeHint(), varianceTarget);
        TelemetryBalanceResult telemetryBalance = resourceTelemetryClient.balanceCluster(
                profile.region(), varianceTarget, currentLoad);
        logger.info("######### Signal dispatch completed route={} stage={}",
                request.routeHint(), request.stage());

        SignalIngestResponse response = new SignalIngestResponse(
                TransactionContextHolder.getTransactionId(),
                "INGESTED",
                request.sessionId(),
                profile,
                ledgerEvaluation,
                telemetryBalance);

        return ResponseEntity.accepted().body(response);
    }

    private String buildLedgerId(String assetId) {
        return assetId + "-ledger";
    }

    private BigDecimal deriveLedgerAmount(String assetId, String sessionId) {
        long seed = Math.abs((long) assetId.hashCode() + sessionId.hashCode());
        double base = 25 + (seed % 9) * 7.25;
        return BigDecimal.valueOf(base).setScale(2, RoundingMode.HALF_UP);
    }

    private double deriveVarianceTarget(String assetId) {
        double normalized = (Math.abs(assetId.hashCode()) % 30) / 100.0;
        return 0.5 + normalized;
    }

    private double deriveCurrentLoad(String routeHint, double varianceTarget) {
        double swing = (Math.abs(routeHint.hashCode()) % 15) / 100.0;
        double computed = varianceTarget + swing;
        return Math.min(0.95, computed);
    }
}
