package com.oha.posting.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WeatherInsertRequest {

    @Schema(description = "행정구역코드", example = "4215032000")
    @NotNull
    private Long hcode;

    @Schema(description = "날씨공통코드", example = "WTHR_CLOUDY")
    @NotBlank
    private String weatherCode;
}
