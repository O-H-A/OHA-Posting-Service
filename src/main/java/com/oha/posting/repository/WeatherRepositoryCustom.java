package com.oha.posting.repository;

import com.oha.posting.dto.weather.WeatherCountSearchResponse;
import com.oha.posting.dto.weather.WeatherSearchResponse;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface WeatherRepositoryCustom {

    List<WeatherCountSearchResponse> searchWeatherCount(List<Long> regionCodes, int dayParts, Date currentDate);

    Optional<WeatherSearchResponse> searchWeather(Long userId, Long regionCode, int dayParts, Date currentDate);
}
