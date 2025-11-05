package com.rapidstay.xap.batch.job.hotelbeds;

import com.rapidstay.xap.batch.job.tasklet.HotelbedsMasterSeedTasklet; // ✅ 원래 경로로 복원
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
}
