package com.rapidstay.xap.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CityBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job cityDataJob; // ✅ CityDataJobConfig에서 등록된 Job 주입

    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시 실행
    public void runCityDataJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(cityDataJob, params);
            System.out.println("🚀 CityDataJob launched successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
