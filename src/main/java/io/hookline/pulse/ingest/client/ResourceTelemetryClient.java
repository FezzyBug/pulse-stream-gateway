package io.hookline.pulse.ingest.client;

import io.hookline.pulse.ingest.model.TelemetryBalanceResult;
import io.hookline.pulse.ingest.support.TransactionContextFilter;
import io.hookline.pulse.ingest.support.TransactionContextHolder;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Component
public class ResourceTelemetryClient {

    private static final Logger logger = LoggerFactory.getLogger(ResourceTelemetryClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ResourceTelemetryClient(RestTemplate restTemplate,
            @Value("${hookline.services.resource-telemetry}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public TelemetryBalanceResult balanceCluster(String clusterId, double varianceTarget, double currentLoad) {
        String url = baseUrl + "/telemetry/balance";
        logger.info("######### Dispatching telemetry balance cluster={} target={} currentLoad={}",
                clusterId, varianceTarget, currentLoad);
        Map<String, Object> body = Map.of(
                "clusterId", clusterId,
                "varianceTarget", varianceTarget,
                "currentLoad", currentLoad);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(TransactionContextFilter.TRANSACTION_HEADER, TransactionContextHolder.getTransactionId());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<TelemetryBalanceResult> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, TelemetryBalanceResult.class);
            return response.getBody();
        } catch (RestClientException ex) {
            throw new ResponseStatusException(BAD_GATEWAY, "Resource Telemetry unavailable", ex);
        }
    }
}
