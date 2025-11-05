package com.rapidstay.xap.batch.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "master_city")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 국가 참조 ID (nullable) */
    @Column(name = "country_id")
    private Long countryId;

    /** Hotelbeds destinationCode */
    @Column(name = "city_code", nullable = false, length = 50, unique = true)
    private String cityCode;

    /** 도시 영문 이름 */
    @Column(name = "city_name_en", nullable = false, length = 200)
    private String cityNameEn;

    /** 도시 한글 이름 */
    @Column(name = "city_name_kr", length = 200)
    private String cityNameKr;

    /** 위도 */
    @Column(name = "lat")
    private Double lat;

    /** 경도 */
    @Column(name = "lon")
    private Double lon;

    /** 지역 구분 (도시, 광역시 등) */
    @Column(name = "region_type", length = 50)
    private String regionType;

    /** 상세 설명 */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    /** 활성 여부 */
    @Column(name = "is_active")
    private Boolean isActive;

    /** 생성일시 */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 수정일시 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Hotelbeds 내부 코드 */
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    /** 국가 코드 */
    @Column(name = "country_code", length = 10)
    private String countryCode;

    /** ISO 코드 */
    @Column(name = "iso_code", length = 10)
    private String isoCode;

    /** 원문 이름 (기타용) */
    @Column(name = "name", length = 200)
    private String name;

    /** 도시 이름 (기타 지역명) */
    @Column(name = "city_name", length = 200)
    private String cityName;

    /** city_id 외래키 (추가된 필드) */
    @Column(name = "city_id")
    private Long cityId; // 이 필드를 추가
}
