package com.oha.posting.repository;

import com.oha.posting.dto.weather.WeatherCountSearchResponse;
import com.oha.posting.dto.weather.WeatherSearchResponse;

import java.sql.Date;
import java.util.List;

public interface WeatherRepositoryCustom {

    List<WeatherCountSearchResponse> searchWeatherCount(List<Long> regionCodes, int dayParts, Date currentDate);

    List<WeatherSearchResponse> searchWeather(Long userId, int dayParts, Date currentDate);
}
