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

    @Schema(description = "날씨공통코드", example = "WTHR_MOSTLY_CLOUDY")
    @NotBlank
    private String weatherCode;
}
