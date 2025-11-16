package io.hookline.pulse.ingest.client;

import io.hookline.pulse.ingest.model.LedgerEvaluationResult;
import io.hookline.pulse.ingest.support.TransactionContextFilter;
import io.hookline.pulse.ingest.support.TransactionContextHolder;
import java.math.BigDecimal;
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
public class SettlementEngineClient {

    private static final Logger logger = LoggerFactory.getLogger(SettlementEngineClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SettlementEngineClient(RestTemplate restTemplate,
            @Value("${hookline.services.settlement-engine}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public LedgerEvaluationResult evaluate(String ledgerId, BigDecimal amount, String scenario) {
        String url = baseUrl + "/ledger/evaluate";
        logger.info("######### Dispatching settlement evaluation ledger={} amount={}", ledgerId, amount);
        Map<String, Object> body = Map.of(
                "ledgerId", ledgerId,
                "amount", amount,
                "scenario", scenario);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(TransactionContextFilter.TRANSACTION_HEADER, TransactionContextHolder.getTransactionId());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<LedgerEvaluationResult> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, LedgerEvaluationResult.class);
            return response.getBody();
        } catch (RestClientException ex) {
            throw new ResponseStatusException(BAD_GATEWAY, "Settlement Engine unavailable", ex);
        }
    }
}
