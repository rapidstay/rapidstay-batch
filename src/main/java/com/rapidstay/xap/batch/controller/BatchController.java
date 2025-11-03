package com.rapidstay.xap.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobMap; // ✅ 등록된 모든 Job 자동 주입

    /**
     * ✅ 배치 실행 엔드포인트
     * 예: GET /run-job?name=CityDataCollector
     */
    @GetMapping("/run-job")
    public Map<String, Object> runJob(@RequestParam String name)
            throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {

        Map<String, Object> result = new HashMap<>();
        Job job = jobMap.get(name);

        if (job == null) {
            result.put("status", "SKIPPED");
            result.put("reason", name + " Job not registered");
            return result;
        }

        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(job, params);

            result.put("status", "STARTED");
            result.put("job", name);
            result.put("message", "🚀 Job started successfully");
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            return result;
        }
    }
}
