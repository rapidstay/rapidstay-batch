package com.rapidstay.xap.batch.job.hotelbeds;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * Step1: 서울(SEO) 기준 호텔 마스터 수집
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class HotelbedsCityCollector implements Tasklet {

    private final HotelbedsApiService apiService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("🏙️ [Step1] Fetching hotels for SEO...");
        JsonNode result = apiService.fetchHotelsByCity("SEO");

        if (result != null && result.has("hotels")) {
            int count = result.get("hotels").size();
            log.info("✅ Found {} hotels for SEO.", count);
        } else {
            log.warn("⚠️ No hotel data found for SEO.");
        }
        return RepeatStatus.FINISHED;
    }
}
