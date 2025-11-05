package com.rapidstay.xap.batch.job.seed;

import com.rapidstay.xap.batch.common.entity.CityInsight;
import com.rapidstay.xap.batch.common.repository.CityInsightRepository;
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

import java.util.List;

/**
 * ✅ CityMasterSeedJobConfig
 * - city_insight 테이블에 기본 도시 목록(서울, 도쿄, 방콕 등)을 초기 삽입
 * - CityDataCollector 실행 전 항상 먼저 수행해야 함
 */
@Configuration
@RequiredArgsConstructor
public class CityMasterSeedJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CityInsightRepository cityInsightRepository;

    @Bean(name = "cityMasterSeedJob")
    public Job cityMasterSeedJob() {
        return new JobBuilder("cityMasterSeedJob", jobRepository)
                .start(cityMasterSeedStep())
                .build();
    }

    @Bean
    public Step cityMasterSeedStep() {
        return new StepBuilder("cityMasterSeedStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    System.out.println("🌍 Starting CityMasterSeedJob...");

                    // 기본 시드 목록 정의
                    List<CityInsight> seeds = List.of(
                            CityInsight.builder().cityName("Seoul").country("Korea").cityNameKr("서울").build(),
                            CityInsight.builder().cityName("Tokyo").country("Japan").cityNameKr("도쿄").build(),
                            CityInsight.builder().cityName("Bangkok").country("Thailand").cityNameKr("방콕").build(),
                            CityInsight.builder().cityName("Singapore").country("Singapore").cityNameKr("싱가포르").build(),
                            CityInsight.builder().cityName("New York").country("USA").cityNameKr("뉴욕").build(),
                            CityInsight.builder().cityName("London").country("UK").cityNameKr("런던").build(),
                            CityInsight.builder().cityName("Paris").country("France").cityNameKr("파리").build()
                    );

                    // 중복 방지용 체크
                    long beforeCount = cityInsightRepository.count();
                    if (beforeCount > 0) {
                        System.out.println("⚠️ 기존 데이터 존재 (" + beforeCount + "건) — 시드 추가 스킵");
                        return RepeatStatus.FINISHED;
                    }

                    cityInsightRepository.saveAll(seeds);
                    System.out.println("✅ 기본 도시 마스터 시드 완료 (" + seeds.size() + "건 삽입)");

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
