package com.rapidstay.xap.batch.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "master_hotel")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Hotelbeds 고유 코드 */
    @Column(name = "hotel_code", length = 50, nullable = false)
    private String hotelCode;

    /** 호텔 이름 */
    @Column(name = "name", length = 300)
    private String name;

    /** 카테고리(성급 등) */
    @Column(name = "category", length = 100)
    private String category;

    /** 국가 코드 */
    @Column(name = "country_code", length = 10)
    private String countryCode;

    /** 목적지 코드(예: SEO) */
    @Column(name = "destination_code", length = 10)
    private String destinationCode;

    /** 위도 */
    @Column(name = "latitude")
    private Double latitude;

    /** 경도 */
    @Column(name = "longitude")
    private Double longitude;

    /** 주소 */
    @Column(name = "address", length = 500)
    private String address;

    /** 데이터 생성 시간 */
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
}
