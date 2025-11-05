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
public class HotelbedsJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final HotelbedsMasterSeedTasklet hotelbedsMasterSeedTasklet;

    /**
     * ✅ Step1 테스트용 (서울 기준)
     * Bean 이름 중복 방지를 위해 V2 suffix 적용
     */
    @Bean(name = "hotelbedsMasterSeedJobV2")
    public Job hotelbedsMasterSeedJobV2() {
        return new JobBuilder("hotelbedsMasterSeedJobV2", jobRepository)
                .start(hotelbedsMasterSeedStepV2())
                .build();
    }

    @Bean(name = "hotelbedsMasterSeedStepV2")
    public Step hotelbedsMasterSeedStepV2() {
        return new StepBuilder("hotelbedsMasterSeedStepV2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("🏨 [Step1] Hotelbeds Master Seed V2 (서울 기준) 시작");
                    hotelbedsMasterSeedTasklet.run(); // ✅ 서울 기준 테스트
                    System.out.println("✅ [Step1] Hotelbeds Master Seed V2 완료");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
