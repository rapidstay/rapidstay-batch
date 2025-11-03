package com.rapidstay.xap.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.rapidstay.xap.batch",
        "com.rapidstay.xap.batch.common"
})
@EntityScan(basePackages = "com.rapidstay.xap.batch.common.entity")
@EnableJpaRepositories(basePackages = "com.rapidstay.xap.batch.common.repository")
@EnableBatchProcessing
public class BatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
