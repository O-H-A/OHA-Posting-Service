package com.oha.posting.repository;

import com.oha.posting.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, Long>, WeatherRepositoryCustom {

    Optional<Weather> findByUserIdAndDayPartsAndWeatherDt(Long userId, Integer dayParts, Date weatherDate);
}
