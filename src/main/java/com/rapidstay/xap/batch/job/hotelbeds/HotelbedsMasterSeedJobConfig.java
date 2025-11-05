package com.rapidstay.xap.batch.job.hotelbeds;

import com.rapidstay.xap.batch.job.tasklet.HotelbedsMasterSeedTasklet;
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
public class HotelbedsMasterSeedJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final HotelbedsMasterSeedTasklet tasklet;

    @Bean(name = "hotelbedsMasterSeedJob")
    public Job hotelbedsMasterSeedJob() {
        return new JobBuilder("hotelbedsMasterSeedJob", jobRepository)
                .start(hotelbedsMasterSeedStep())
                .build();
    }

    @Bean
    public Step hotelbedsMasterSeedStep() {
        return new StepBuilder("hotelbedsMasterSeedStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("🏨 [Step1] Hotelbeds Master Seed 시작");
                    tasklet.run();
                    System.out.println("✅ [Step1] Hotelbeds Master Seed 완료");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
