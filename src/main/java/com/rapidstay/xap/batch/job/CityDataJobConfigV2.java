package com.rapidstay.xap.batch.job;

import com.rapidstay.xap.batch.job.tasklet.CityDataCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ✅ CityDataJobConfigV2
 * - 기존 Job과 완전히 독립된 Force Update 버전
 */
@Configuration
@RequiredArgsConstructor
public class CityDataJobConfigV2 {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CityDataCollector cityDataCollector;

    @Bean(name = "cityDataCollectorV2Job")
    public Job cityDataCollectorV2Job() {
        return new JobBuilder("cityDataCollectorV2Job", jobRepository)
                .start(cityDataCollectorV2Step())
                .build();
    }

    @Bean
    public Step cityDataCollectorV2Step() {
        return new StepBuilder("cityDataCollectorV2Step", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("🏙️ [V2] CityDataCollector Force Update 시작");
                    cityDataCollector.runBatch(); // V2 Collector는 이미 Force 모드
                    System.out.println("✅ [V2] CityDataCollector Force Update 완료");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
