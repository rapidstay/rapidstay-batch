package com.rapidstay.xap.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CityBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job cityDataCollectorV2Job;

    public CityBatchScheduler(JobLauncher jobLauncher,
                              @Qualifier("cityDataCollectorV2Job") Job cityDataCollectorV2Job) {
        this.jobLauncher = jobLauncher;
        this.cityDataCollectorV2Job = cityDataCollectorV2Job;
    }

    @Scheduled(cron = "0 0 2 * * *") // 새벽 2시 실행
    public void runCityJob() throws Exception {
        jobLauncher.run(cityDataCollectorV2Job, new JobParameters());
    }
}
