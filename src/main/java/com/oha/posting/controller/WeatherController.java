package com.oha.posting.controller;

import com.oha.posting.config.response.ResponseObject;
import com.oha.posting.dto.weather.WeatherInsertRequest;
import com.oha.posting.dto.weather.WeatherUpdateRequest;
import com.oha.posting.dto.weather.WeatherCountSearchResponse;
import com.oha.posting.dto.weather.WeatherInsertResponse;
import com.oha.posting.dto.weather.WeatherSearchResponse;
import com.oha.posting.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Tag(name="동네 날씨", description = "동네 날씨 API")
@RequestMapping("/api/posting")
@RestController
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping ("/weather/count")
    @Operation(summary = "동네 날씨 개수 조회", description = """
                                                     **Status Code:**
                                                     - 200: 성공
                                                     - 500: 서버 오류
                                                     """)
    public ResponseObject<List<WeatherCountSearchResponse>> getWeatherCount(@Parameter(hidden = true) @RequestHeader(name = "Authorization") String token
                                                                          , @RequestParam(name = "regionCode") Long regionCode) throws Exception {
        return weatherService.getWeatherCount(token, regionCode);
    }

    @GetMapping ("/weather/my")
    @Operation(summary = "등록한 동네 날씨 조회", description = """
                                                     **Status Code:**
                                                     - 200: 성공
                                                     - 404: 동네 날씨 없음
                                                     - 500: 서버 오류
                                                     """)
    public ResponseObject<List<WeatherSearchResponse>> getMyWeather(@Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId) throws Exception {
        return weatherService.getMyWeather(userId);
    }

    @PostMapping("/weather")
    @Operation(summary = "동네 날씨 등록", description = """ 
                                                     **Status Code:**
                                                     - `201`: 성공
                                                     - 400: 데이터 오류
                                                     - 409: 중복 오류
                                                     - 500: 서버 오류
                                                     """)
    public ResponseObject<WeatherInsertResponse> insertWeather(@RequestBody @Validated WeatherInsertRequest dto
                                                             , @Parameter(hidden = true) @RequestHeader(name = "Authorization") String token
                                                             , @Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId
                                                             , HttpServletResponse httpServletResponse) throws Exception {
        return weatherService.insertWeather(dto, token, userId, httpServletResponse);
    }

    @PutMapping("/weather")
    @Operation(summary = "동네 날씨 수정", description = """
                                                     **Status Code:**
                                                     - 200: 성공
                                                     - 400: 데이터 오류
                                                     - 403: 권한 없음
                                                     - 404: 동네 날씨 없음
                                                     - 500: 서버 오류\n
                                                     """)
    public ResponseObject<?> updateWeather(@RequestBody @Validated WeatherUpdateRequest dto
                                         , @Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId
                                         , @Parameter(hidden = true) @RequestHeader(name = "Authorization") String token) throws Exception {
        return weatherService.updateWeather(dto, userId, token);
    }


    @DeleteMapping("/weather/{weatherId}")
    @Operation(summary = "동네 날씨 삭제", description = """
                                                     **Status Code:**
                                                     - 200: 성공
                                                     - 400: 데이터 오류
                                                     - 403: 권한 없음
                                                     - 404: 동네 날씨 없음
                                                     - 500: 서버 오류
                                                     """)
    public ResponseObject<?> deleteWeather(@Parameter(hidden = true) @RequestHeader(name = "x-user-id") Long userId
                                         , @PathVariable Long weatherId) throws Exception {
        return weatherService.deleteWeather(userId, weatherId);
    }
}
