package com.rapidstay.xap.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.rapidstay.xap")
@EnableBatchProcessing
@EntityScan(basePackages = "com.rapidstay.xap.common.entity")
@EnableJpaRepositories(basePackages = "com.rapidstay.xap.common.repository")
public class BatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
