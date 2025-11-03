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

@Configuration
@RequiredArgsConstructor
public class CityDataJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CityDataCollector cityDataCollector;

    /**
     * ✅ AdminOpsController 에서 호출하는 Job 이름은 "CityDataCollector" 이므로
     *    JobBuilder 이름과 Bean 이름을 동일하게 맞춰준다.
     */
    @Bean(name = "CityDataCollector")
    public Job cityDataCollectorJob() {
        return new JobBuilder("CityDataCollector", jobRepository)
                .start(cityDataCollectorStep())
                .build();
    }

    @Bean
    public Step cityDataCollectorStep() {
        return new StepBuilder("cityDataCollectorStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("🏙️ Starting CityDataCollector...");
                    cityDataCollector.runBatch(); // ✅ 실제 수집 로직 호출
                    System.out.println("✅ CityDataCollector completed successfully!");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
