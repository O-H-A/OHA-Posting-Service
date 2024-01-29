package com.oha.posting.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherInsertResponse {

    @Schema(description = "동네날씨ID", example = "1")
    private Long weatherId;
}
