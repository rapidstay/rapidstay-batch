package com.rapidstay.xap.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityJson {
    private String name;       // 구 버전 호환
    private String city_kr;    // 한글 도시명
    private String city_en;    // 영문 도시명
    private String country;    // 국가명
    private Long regionId;     // optional
    private List<String> tags; // 태그 (관광지 등)
}
