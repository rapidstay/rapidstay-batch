package com.rapidstay.xap.batch.job.tasklet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidstay.xap.batch.common.entity.MasterCity;
import com.rapidstay.xap.batch.common.repository.MasterCityRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HotelbedsMasterSeedTasklet {

    private final MasterCityRepository masterCityRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${hotelbeds.apiKey:}")
    private String apiKey;

    @Value("${hotelbeds.sharedSecret:}")
    private String sharedSecret;

    @Value("${hotelbeds.baseUrl:https://api.test.hotelbeds.com/hotel-content-api/1.0}")
    private String baseUrl;

    /**
     * ✅ 전체 수집용 - 안전한 100단위 페이지 루프 버전
     */
    @Transactional
    public void run() {
        try {
            System.out.println("🌍 [Step1] Hotelbeds 전체 도시 목록 수집 시작 (안정형 모드)");

            int countTotal = 0;
            // from~to 구간을 100단위로 끊어서 호출
            for (int from = 1; from <= 1000; from += 100) {
                int to = from + 99;
                String endpoint = baseUrl + "/locations/destinations?fields=all&language=ENG&from=" + from + "&to=" + to;

                HttpHeaders headers = new HttpHeaders();
                headers.set("Api-Key", apiKey);
                headers.set("X-Signature", buildSignature());
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));

                HttpEntity<Void> req = new HttpEntity<>(headers);
                ResponseEntity<String> res = restTemplate.exchange(endpoint, HttpMethod.GET, req, String.class);

                if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                    System.err.println("❌ Hotelbeds API 응답 실패: " + res.getStatusCode());
                    continue;
                }

                JsonNode root = objectMapper.readTree(res.getBody());
                JsonNode destinations = root.path("destinations");

                if (!destinations.isArray()) {
                    System.err.println("⚠️ destinations 노드가 비어 있음 (from=" + from + ")");
                    continue;
                }

                int countBatch = 0;
                for (JsonNode node : destinations) {
                    String code = node.path("code").asText("");
                    String name = node.path("name").path("content").asText("");
                    String countryCode = node.path("countryCode").asText("");
                    String isoCode = node.path("isoCode").asText("");

                    // ✅ 필수값 누락 시 스킵
                    if (code.isBlank() || name.isBlank()) {
                        System.out.println("⚠️ Skip invalid record: code=" + code + ", name=" + name);
                        continue;
                    }

                    // `city_id`를 가져오는 로직
                    Long cityId = getCityIdByCityCode(code); // `city_code`로 `city_id` 찾기

                    MasterCity city = MasterCity.builder()
                            .cityCode(code)
                            .cityNameEn(name)
                            .countryCode(countryCode)
                            .isoCode(isoCode)
                            .code(code)
                            .cityId(cityId)  // `city_id` 외래키 연결
                            .isActive(true)
                            .build();

                    // `master_hotel` 테이블에 데이터 저장
                    masterCityRepository.save(city);
                    countBatch++;
                }

                countTotal += countBatch;
                System.out.println("💾 구간 저장 완료 — from=" + from + ", to=" + to + ", count=" + countBatch);

                // ✅ 호출 간격 제한 (1.5초 대기)
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {}
            }

            System.out.println("✅ [완료] master_city 총 " + countTotal + "건 저장 완료");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** city_code로 city_id를 찾는 메서드 */
    private Long getCityIdByCityCode(String cityCode) {
        // master_city에서 city_code 기준으로 city_id 조회
        MasterCity city = masterCityRepository.findByCityCode(cityCode);
        return (city != null) ? city.getId() : null;  // city가 없다면 null 반환
    }

    /**
     * ✅ 단일 도시 코드 테스트용
     */
    @Transactional
    public void runSeed(String cityCode) {
        try {
            System.out.println("🌍 Hotelbeds Master Seed (도시코드: " + cityCode + ") 시작");

            String endpoint = baseUrl + "/locations/destinations/" + cityCode + "?fields=all&language=ENG";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Api-Key", apiKey);
            headers.set("X-Signature", buildSignature());
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Void> req = new HttpEntity<>(headers);
            ResponseEntity<String> res = restTemplate.exchange(endpoint, HttpMethod.GET, req, String.class);

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                System.err.println("❌ Hotelbeds API 응답 실패: " + res.getStatusCode());
                return;
            }

            JsonNode node = objectMapper.readTree(res.getBody());
            String code = node.path("code").asText("");
            String name = node.path("name").path("content").asText("");
            String isoCode = node.path("isoCode").asText("");
            String countryCode = node.path("countryCode").asText("");

            if (code.isBlank()) {
                code = cityCode;
            }

            if (code.isBlank() || name.isBlank()) {
                System.out.println("⚠️ Skip invalid single record: code=" + code + ", name=" + name);
                return;
            }

            MasterCity city = MasterCity.builder()
                    .cityCode(code)
                    .cityNameEn(name)
                    .countryCode(countryCode)
                    .isoCode(isoCode)
                    .code(code)
                    .isActive(true)
                    .build();

            masterCityRepository.save(city);
            System.out.println("💾 master_city 저장 완료 — " + city.getCityNameEn());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** ✅ Hotelbeds 인증 시그니처 생성 */
    private String buildSignature() {
        long timestamp = System.currentTimeMillis() / 1000L;
        String toSign = apiKey + sharedSecret + timestamp;
        return DigestUtils.sha256Hex(toSign);
    }
}
