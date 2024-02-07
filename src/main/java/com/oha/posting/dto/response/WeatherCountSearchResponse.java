package com.oha.posting.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherCountSearchResponse {

    @Schema(description = "날씨공통코드", example = "WTHR_PARTLY_CLOUDY")
    private String weatherCode;
    @Schema(description = "날씨이름", example = "구름 많음")
    private String weatherName;
    @Schema(description = "개수", example = "4")
    private Long count;
}
