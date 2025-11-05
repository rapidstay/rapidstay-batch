package com.rapidstay.xap.batch.common.repository;

import com.rapidstay.xap.batch.common.entity.MasterHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterHotelRepository extends JpaRepository<MasterHotel, Long> {
}
