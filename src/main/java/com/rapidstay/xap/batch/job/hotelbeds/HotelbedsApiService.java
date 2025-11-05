package com.rapidstay.xap.batch.job.hotelbeds;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

/**
 * Hotelbeds API 호출 및 JSON 응답 처리
 */
@Slf4j
@Service
public class HotelbedsApiService {

    @Value("${hotelbeds.api-key}")
    private String apiKey;

    @Value("${hotelbeds.secret}")
    private String secret;

    @Value("${hotelbeds.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode fetchHotelsByCity(String destinationCode) {
        long timestamp = Instant.now().getEpochSecond();
        String signature = HotelbedsAuthUtil.generateSignature(apiKey, secret, timestamp);

        String url = baseUrl + "/hotels?destinationCode=" + destinationCode + "&from=1&to=10";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Api-Key", apiKey);
        headers.set("X-Signature", signature);
        headers.set("Accept", "application/json");

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("✅ Hotelbeds API success for city: {}", destinationCode);
                return mapper.readTree(response.getBody());
            }
            log.error("❌ Hotelbeds API failed with status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("❌ Error while calling Hotelbeds API", e);
        }
        return null;
    }
}
