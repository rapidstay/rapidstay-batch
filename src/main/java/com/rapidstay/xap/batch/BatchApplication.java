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
        "com.rapidstay.xap.common"   // ✅ common 모듈(@Service, @Configuration 등 포함)
})
@EntityScan(basePackages = "com.rapidstay.xap.batch.common.entity")  // ✅ Entity 인식
@EnableJpaRepositories(basePackages = "com.rapidstay.xap.batch.common.repository")  // ✅ Repository 인식
@EnableBatchProcessing
public class BatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
