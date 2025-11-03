package com.rapidstay.xap.batch.job.tasklet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidstay.xap.common.dto.CityDTO;
import com.rapidstay.xap.common.entity.CityInsight;
import com.rapidstay.xap.common.repository.CityInsightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
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
    private final RestTemplate restTemplate = new RestTemplate(); // 외부 호출용

    @Value("${opentripmap.apikey:}")
    private String otmApiKey;

    @Value("${nominatim.email:rapidstay@example.com}")
    private String nominatimEmail;

    /**
     * 1. DB(city_insight)에서 도시 목록 조회
     * 2. 좌표 없거나 갱신 필요한 항목만 외부 API 호출
     * 3. DB 저장 및 Redis 캐싱
     */
    @Transactional
    public void runBatch() {
        try {
            List<CityInsight> cityList = cityInsightRepository.findAll();
            if (cityList.isEmpty()) {
                System.out.println("⚠️ 등록된 도시가 없습니다. 어드민에서 도시를 추가하세요.");
                return;
            }

            List<CityInsight> updatedEntities = new ArrayList<>();

            for (CityInsight city : cityList) {
                Double lat = city.getLat();
                Double lon = city.getLon();
                boolean needsGeo = (lat == null || lon == null || lat == 0.0 || lon == 0.0);

                // 이미 좌표 있으면 건너뛰기
                if (!needsGeo) {
                    System.out.println("⏩ Skip: " + city.getCityName() + " (이미 좌표 있음)");
                    continue;
                }

                String cityName = city.getCityName();
                String cityNameKr = city.getCityNameKr() != null ? city.getCityNameKr() : cityName;
                String country = (city.getCountry() != null && !city.getCountry().isBlank())
                        ? city.getCountry()
                        : "Korea";

                double[] coords = fetchCoordinates(cityName, country);
                city.setLat(coords[0]);
                city.setLon(coords[1]);

                // 한글명이 비어 있으면 Nominatim에서 가져온 display_name 일부로 보충
                if (city.getCityNameKr() == null || city.getCityNameKr().isBlank()) {
                    city.setCityNameKr(guessKoreanName(cityNameKr));
                }

                updatedEntities.add(city);

                if (otmApiKey == null || otmApiKey.isBlank()) {
                    try { Thread.sleep(1100); } catch (InterruptedException ignored) {}
                }
            }

            // 2) DB 저장
            if (!updatedEntities.isEmpty()) {
                cityInsightRepository.saveAll(updatedEntities);
                System.out.println("💾 DB 갱신 완료 — " + updatedEntities.size() + "건");
            }

            // 3) Redis 캐시 업데이트
            if (redisTemplate != null) {
                // 개별 도시 캐싱
                for (CityInsight e : cityList) {
                    String keyName = (e.getCityName() != null) ? e.getCityName().toLowerCase() : "unknown";

                    String airports = e.getAirports();
                    String attractions = e.getAttractions();

                    CityDTO dto = CityDTO.builder()
                            .id(e.getId())
                            .cityName(e.getCityName())
                            .cityNameKr(e.getCityNameKr())
                            .country(e.getCountry())
                            .airports(
                                    (airports == null || airports.isBlank())
                                            ? List.of()
                                            : Arrays.asList(airports.split(","))
                            )
                            .attractions(
                                    (attractions == null || attractions.isBlank())
                                            ? List.of()
                                            : Arrays.asList(attractions.split(","))
                            )
                            .lat(e.getLat())
                            .lon(e.getLon())
                            .error(null)
                            .build();

                    redisTemplate.opsForValue().set("city:" + keyName, dto, Duration.ofHours(24));
                }

                // ✅ 전체 도시 리스트 캐싱
                try {
                    List<CityDTO> dtoList = cityList.stream()
                            .map(e -> CityDTO.builder()
                                    .id(e.getId())
                                    .cityName(e.getCityName())
                                    .cityNameKr(e.getCityNameKr())
                                    .country(e.getCountry())
                                    .airports(e.getAirports() == null || e.getAirports().isBlank()
                                            ? List.of() : Arrays.asList(e.getAirports().split(",")))
                                    .attractions(e.getAttractions() == null || e.getAttractions().isBlank()
                                            ? List.of() : Arrays.asList(e.getAttractions().split(",")))
                                    .lat(e.getLat())
                                    .lon(e.getLon())
                                    .error(null)
                                    .build())
                            .toList();

                    String json = objectMapper.writeValueAsString(dtoList);
                    redisTemplate.getConnectionFactory()
                            .getConnection()
                            .stringCommands()
                            .set("city:list".getBytes(StandardCharsets.UTF_8), json.getBytes(StandardCharsets.UTF_8));

                    System.out.println("🧠 Redis city:list 저장 완료 (" + dtoList.size() + "건)");
                } catch (Exception ex) {
                    System.err.println("⚠️ Redis city:list 저장 실패: " + ex.getMessage());
                }
            }

            System.out.println("✅ 도시 데이터 배치 완료 — 총 " + cityList.size() + "건 처리됨");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 좌표 조회 */
    private double[] fetchCoordinates(String cityName, String country) {
        if (cityName == null || cityName.isBlank()) return new double[]{0.0, 0.0};

        // 1) OpenTripMap
        if (otmApiKey != null && !otmApiKey.isBlank()) {
            try {
                String query = URLEncoder.encode(cityName + " " + country, StandardCharsets.UTF_8);
                String url = "https://api.opentripmap.com/0.1/en/places/geoname?name=" + query + "&apikey=" + otmApiKey;
                JsonNode response = restTemplate.getForObject(url, JsonNode.class);
                if (response != null && response.has("lat") && response.has("lon")) {
                    return new double[]{response.get("lat").asDouble(), response.get("lon").asDouble()};
                }
            } catch (Exception e) {
                System.err.println("⚠️ OTM 좌표 조회 실패: " + cityName + " (" + e.getMessage() + ")");
            }
        }

        // 2) Nominatim
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
            System.err.println("⚠️ Nominatim 좌표 조회 실패: " + cityName + " (" + e.getMessage() + ")");
        }

        return new double[]{0.0, 0.0};
    }

    /** 한글명 추론 보조 (단순 fallback용) */
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
