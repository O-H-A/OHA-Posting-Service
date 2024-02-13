package com.oha.posting.repository;

import com.oha.posting.dto.response.WeatherCountSearchResponse;

import java.sql.Date;
import java.util.List;

public interface WeatherRepositoryCustom {

    List<WeatherCountSearchResponse> searchWeatherCount(List<Long> regionCodes, int dayParts, Date currentDate);
}
