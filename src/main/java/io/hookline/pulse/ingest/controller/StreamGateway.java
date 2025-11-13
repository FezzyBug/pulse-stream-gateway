package io.hookline.pulse.ingest.controller;

import io.hookline.pulse.ingest.model.SignalRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(@RequestBody @Valid SignalRequest request) {
        logger.info("######### Signal handshake accepted for asset={} session={} codec={}",
                request.assetId(), request.sessionId(), request.codec());
        logger.info("######### Signal normalization running envelope={} codec={} version={}",
                request.envelope(), request.codec(), request.schemaVersion());
        logger.info("######### Signal dispatch completed route={} stage={}",
                request.routeHint(), request.stage());

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", request.sessionId());
        response.put("receivedAt", Instant.now().toString());
        response.put("status", "INGESTED");
        response.put("stage", request.stage());

        return ResponseEntity.accepted().body(response);
    }
}
