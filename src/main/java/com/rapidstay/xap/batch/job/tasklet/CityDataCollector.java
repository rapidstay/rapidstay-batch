package com.rapidstay.xap.batch.job.tasklet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidstay.xap.batch.common.dto.CityDTO;
import com.rapidstay.xap.batch.common.entity.CityInsight;
import com.rapidstay.xap.batch.common.repository.CityInsightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CityDataCollector {

    private final CityInsightRepository cityInsightRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, CityDTO> redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${opentripmap.apikey:}")
    private String otmApiKey;

    @Value("${nominatim.email:rapidstay@example.com}")
    private String nominatimEmail;

    /**
     * ✅ 임시 Force Update 모드
     * - 좌표가 있어도 무조건 다시 호출하여 덮어쓴다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runBatch() {
        try {
            List<CityInsight> cityList = cityInsightRepository.findAll();
            if (cityList.isEmpty()) {
                System.out.println("⚠️ 등록된 도시가 없습니다. 먼저 CityMasterSeedJob 실행 필요.");
                return;
            }

            List<CityInsight> updatedEntities = new ArrayList<>();

            for (CityInsight city : cityList) {
                String cityName = city.getCityName();
                String country = (city.getCountry() != null && !city.getCountry().isBlank())
                        ? city.getCountry()
                        : "Korea";

                System.out.println("📍 좌표 재조회: " + cityName + " (" + country + ")");
                double[] coords = fetchCoordinates(cityName, country);
                city.setLat(coords[0]);
                city.setLon(coords[1]);

                if (city.getCityNameKr() == null || city.getCityNameKr().isBlank()) {
                    city.setCityNameKr(guessKoreanName(cityName));
                }

                updatedEntities.add(city);

                if (otmApiKey == null || otmApiKey.isBlank()) {
                    try { Thread.sleep(1100); } catch (InterruptedException ignored) {}
                }
            }

            if (!updatedEntities.isEmpty()) {
                cityInsightRepository.saveAll(updatedEntities);
                System.out.println("💾 DB 갱신 완료 — " + updatedEntities.size() + "건");
            }

            if (redisTemplate != null) {
                cacheToRedis(cityList);
            }

            System.out.println("✅ [Force] 도시 좌표 전체 재수집 완료 — 총 " + cityList.size() + "건");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Redis 캐싱 */
    private void cacheToRedis(List<CityInsight> cityList) {
        try {
            for (CityInsight e : cityList) {
                String keyName = (e.getCityName() != null) ? e.getCityName().toLowerCase() : "unknown";
                CityDTO dto = CityDTO.builder()
                        .id(e.getId())
                        .cityName(e.getCityName())
                        .cityNameKr(e.getCityNameKr())
                        .country(e.getCountry())
                        .lat(e.getLat())
                        .lon(e.getLon())
                        .error(null)
                        .build();
                redisTemplate.opsForValue().set("city:" + keyName, dto, Duration.ofHours(24));
            }

            String json = objectMapper.writeValueAsString(cityList);
            redisTemplate.getConnectionFactory()
                    .getConnection()
                    .stringCommands()
                    .set("city:list".getBytes(StandardCharsets.UTF_8), json.getBytes(StandardCharsets.UTF_8));

            System.out.println("🧠 Redis city:list 저장 완료 (" + cityList.size() + "건)");
        } catch (Exception ex) {
            System.err.println("⚠️ Redis 캐싱 실패: " + ex.getMessage());
        }
    }

    /** 좌표 조회 */
    private double[] fetchCoordinates(String cityName, String country) {
        if (cityName == null || cityName.isBlank()) return new double[]{0.0, 0.0};

        if (otmApiKey != null && !otmApiKey.isBlank()) {
            try {
                String query = URLEncoder.encode(cityName + " " + country, StandardCharsets.UTF_8);
                String url = "https://api.opentripmap.com/0.1/en/places/geoname?name=" + query + "&apikey=" + otmApiKey;
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response != null && response.has("lat") && response.has("lon")) {
                    return new double[]{response.get("lat").asDouble(), response.get("lon").asDouble()};
                }
            } catch (Exception e) {
                System.err.println("⚠️ OTM 조회 실패: " + cityName + " (" + e.getMessage() + ")");
            }
        }

        try {
            String q = URLEncoder.encode(cityName + ", " + country, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" + q;
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "RapidStay-Batch/1.0 (" + nominatimEmail + ")");
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> req = new HttpEntity<>(headers);

            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, req, String.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                JsonNode arr = objectMapper.readTree(res.getBody());
                if (arr.isArray() && arr.size() > 0) {
                    JsonNode first = arr.get(0);
                    double lat = Double.parseDouble(first.get("lat").asText());
                    double lon = Double.parseDouble(first.get("lon").asText());
                    return new double[]{lat, lon};
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Nominatim 조회 실패: " + cityName + " (" + e.getMessage() + ")");
        }

        return new double[]{0.0, 0.0};
    }

    private String guessKoreanName(String original) {
        if (original == null) return "";
        return switch (original.toLowerCase()) {
            case "seoul" -> "서울";
            case "busan" -> "부산";
            case "incheon" -> "인천";
            case "jeju" -> "제주";
            default -> original;
        };
    }
}
