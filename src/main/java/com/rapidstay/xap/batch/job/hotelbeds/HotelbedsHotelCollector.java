package com.rapidstay.xap.batch.job.hotelbeds;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Step2: master_hotel 테이블에 삽입
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class HotelbedsHotelCollector implements Tasklet {

    private final HotelbedsApiService apiService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("🏨 [Step2] Saving Hotelbeds data to master_hotel...");
        JsonNode result = apiService.fetchHotelsByCity("SEO");

        if (result == null || !result.has("hotels")) {
            log.warn("⚠️ No hotel data found for SEO.");
            return RepeatStatus.FINISHED;
        }

        for (JsonNode hotel : result.get("hotels")) {
            try {
                String code = hotel.path("code").asText();
                String name = hotel.path("name").path("content").asText();
                String address = hotel.path("address").path("content").asText();
                double lat = hotel.path("coordinates").path("latitude").asDouble();
                double lon = hotel.path("coordinates").path("longitude").asDouble();

                jdbcTemplate.update("""
                    INSERT INTO master_hotel (hotel_code, name, address, latitude, longitude, source)
                    VALUES (?, ?, ?, ?, ?, 'HOTELBEDS')
                    ON CONFLICT (hotel_code)
                    DO UPDATE SET name=?, address=?, latitude=?, longitude=?;
                    """,
                        code, name, address, lat, lon,
                        name, address, lat, lon
                );

                log.info("✔️ Inserted: {} - {}", code, name);
            } catch (Exception e) {
                log.error("❌ Insert failed", e);
            }
        }
        return RepeatStatus.FINISHED;
    }
}
