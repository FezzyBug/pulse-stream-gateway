package io.hookline.pulse.ingest.client;

import io.hookline.pulse.ingest.model.DeviceProfileDocument;
import io.hookline.pulse.ingest.support.TransactionContextFilter;
import io.hookline.pulse.ingest.support.TransactionContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Component
public class ProfileDirectoryClient {

    private static final Logger logger = LoggerFactory.getLogger(ProfileDirectoryClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ProfileDirectoryClient(RestTemplate restTemplate,
            @Value("${hookline.services.profile-directory}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public DeviceProfileDocument fetchProfile(String assetId) {
        String url = baseUrl + "/profiles/" + assetId;
        logger.info("######### Dispatching device profile lookup for asset={}", assetId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(TransactionContextFilter.TRANSACTION_HEADER, TransactionContextHolder.getTransactionId());
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<DeviceProfileDocument> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, DeviceProfileDocument.class);
            return response.getBody();
        } catch (RestClientException ex) {
            throw new ResponseStatusException(BAD_GATEWAY, "Profile Directory unavailable", ex);
        }
    }
}
