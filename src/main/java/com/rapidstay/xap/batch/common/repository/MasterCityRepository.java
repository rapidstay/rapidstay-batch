package com.rapidstay.xap.batch.common.repository;

import com.rapidstay.xap.batch.common.entity.MasterCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterCityRepository extends JpaRepository<MasterCity, Long> {
    // city_code 기준으로 MasterCity 조회
    MasterCity findByCityCode(String cityCode);
}