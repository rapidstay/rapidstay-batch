package com.rapidstay.xap.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig extends DefaultBatchConfiguration {

    private final DataSource dataSource;

    public BatchConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ✅ Spring Boot 3.3.x 기준, protected -> public
    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }

    // ✅ createTransactionManager() 대신 getTransactionManager() 로 돌아감
    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}
