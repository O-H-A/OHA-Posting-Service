package com.oha.posting.repository;

import com.oha.posting.dto.response.WeatherCountSearchResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

import static com.oha.posting.entity.QCommonCode.commonCode;
import static com.oha.posting.entity.QWeather.weather;

@RequiredArgsConstructor
@Repository
public class WeatherRepositoryImpl implements WeatherRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<WeatherCountSearchResponse> searchWeatherCount(Long regionCode, int dayParts, Date currentDate) {
        return queryFactory
                .select(Projections.constructor(WeatherCountSearchResponse.class
                        , commonCode.code
                        , weather.weatherCommonCode.count()
                        ))
                .from(commonCode)
                .leftJoin(weather)
                .on(commonCode.code.eq(weather.weatherCommonCode.code)
                        .and(weather.regionCode.eq(regionCode))
                        .and(weather.dayParts.eq(dayParts))
                        .and(weather.weatherDt.eq((currentDate)))
                )
                .where(commonCode.type.eq("WEATHER"))
                .groupBy(commonCode.code)
                .orderBy(weather.weatherCommonCode.count().desc())
                .fetch();
    }
}
