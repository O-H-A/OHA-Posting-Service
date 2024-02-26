package com.oha.posting.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherSearchResponse {
    private Long weatherId;
    private Long userId;
    private Long regionCode;
    private String weatherCode;
    private String weatherName;
    private Integer dayParts;
    private String weatherDt;
}
