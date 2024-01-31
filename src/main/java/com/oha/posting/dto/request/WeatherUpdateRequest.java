package com.oha.posting.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WeatherUpdateRequest {

    @Schema(description = "동네날씨ID", example = "1")
    @NotNull
    private Long weatherId;

    @Schema(description = "행정구역코드", example = "4215032000")
    @NotNull
    private Long regionCode;

    @Schema(description = "날씨공통코드", example = "WTHR_MOSTLY_CLOUDY")
    @NotBlank
    private String weatherCode;
}
